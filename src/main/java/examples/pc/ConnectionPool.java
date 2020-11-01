package examples.pc;

import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {

    private final List<Connection> pool;
    private final int maxCapacity;

    public ConnectionPool(int maxCapacity) {
        this.pool = new ArrayList<>(maxCapacity);
        this.maxCapacity = maxCapacity;
    }

    public static void main(String[] args) throws InterruptedException {
        ConnectionPool connectionPool = new ConnectionPool(5);

        // add 5 connections
        for (int i = 0; i < 5; i++) {
            connectionPool.addConnection(new Connection("connection-" + i));
        }

        Thread thread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                connectionPool.getConnection();
            }
        }, "worker-thread-1");

        // add next 5 connections, but main thread will block due to queue full
        thread.start();
        for (int i = 5; i < 10; i++) {
            connectionPool.addConnection(new Connection("connection-" + i));
        }

        thread.join();

        System.out.println("main thread exiting");
    }

    public Connection getConnection() {
        synchronized (this) {
            try {
                while (this.pool.size() == 0) { // spin wait on empty queue condition
                    System.out.printf("Connection pool is empty, consumer thread: [%s] is waiting%n",
                            Thread.currentThread().getName());
                    wait();
                }
                Thread.sleep(2 * 1000); // slow consumer
                Connection connection = this.pool.remove(0);
                System.out.printf("Consumer thread: [%s] got the connection: [%s]%n",
                        Thread.currentThread().getName(), connection);
                notifyAll(); // notify the threads
                return connection;
            } catch (InterruptedException ex) {
                System.out.printf("Consumer thread: [%s] interrupted while getting connection%n",
                        Thread.currentThread().getName());
                return null;
            }
        }
    }

    public void addConnection(Connection connection) throws InterruptedException {
        if (connection == null) {
            return;
        }

        synchronized (this) {
            try {
                while (this.pool.size() == maxCapacity) { // spin wait on queue full condition.
                    System.out.printf("Connection pool is full, producer thread: [%s] is waiting%n", Thread.currentThread().getName());
                    wait();
                }
                this.pool.add(connection);
                System.out.printf("Producer thread: [%s] added the connection: [%s]%n", Thread.currentThread().getName(), connection);
                notifyAll(); // notify the threads
            } catch (InterruptedException ex) {
                System.out.printf("Producer thread: [%s] interrupted while adding connection%n",
                        Thread.currentThread().getName());
            }
        }
    }
}
