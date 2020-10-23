package synchronizers.semaphore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import examples.Connection;

public class ConnectionPoolUsingSemaphore {
    private final List<examples.Connection> pool;
    private final Semaphore semaphore;

    public ConnectionPoolUsingSemaphore(int maxCapacity) {
        this.semaphore = new Semaphore(maxCapacity);
        this.pool = new ArrayList<>();
        for (int i = 0; i < maxCapacity; i++) {
            this.pool.add(new examples.Connection("connection-" + i));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ConnectionPoolUsingSemaphore pool = new ConnectionPoolUsingSemaphore(5);

        List<Connection> acquiredConnections = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            acquiredConnections.add(pool.getConnection());
        }

        Thread thread = new Thread(() -> {
            List<Connection> acquiredConnections1 = new ArrayList<>();
            try {
                for (int i = 0; i < 10; i++) {
                    acquiredConnections1.add(pool.getConnection());
                }
                Thread.sleep(5 * 1000);
                acquiredConnections1.forEach(pool::addConnection);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "worker-thread-1");

        // add next 5 connections, but main thread will block due to queue full
        thread.start();
        Thread.sleep(5 * 1000);
        acquiredConnections.forEach(pool::addConnection);
        thread.join();
        System.out.println("main thread exiting");
    }

    public examples.Connection getConnection() {
        examples.Connection connection = null;
        try {
            semaphore.acquire();
            Thread.sleep(2 * 1000);
            connection = pool.remove(0);
            System.out.printf("Consumer thread: [%s] got the connection: [%s]%n",
                    Thread.currentThread().getName(), connection);
            return connection;
        } catch (InterruptedException e) {
            System.out.printf("Consumer thread: [%s] interrupted while getting connection%n",
                    Thread.currentThread().getName());
            return null;
        } finally {
            if (connection == null) {
                semaphore.release();
            }
        }
    }

    public void addConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        this.pool.add(connection);
        System.out.printf("Producer thread: [%s] added the connection: [%s]%n",
                Thread.currentThread().getName(), connection);
        semaphore.release();
    }
}
