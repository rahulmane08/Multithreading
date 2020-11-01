package examples.pc;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionPoolUsingReentrantLock {
    private final List<Connection> pool;
    private final int maxCapacity;
    private final Lock lock;
    private final Condition isPoolEmpty, isPoolFull;

    public ConnectionPoolUsingReentrantLock(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.pool = new ArrayList<>(maxCapacity);
        this.lock = new ReentrantLock();
        this.isPoolEmpty = lock.newCondition();
        this.isPoolFull = lock.newCondition();
    }

    public Connection getConnection() {
        try {
            lock.lockInterruptibly();
            while (this.pool.size() == 0) {
                isPoolEmpty.await();
            }
            Thread.sleep(2 * 1000); // slow consumer
            Connection connection = this.pool.remove(0);
            System.out.printf("Consumer thread: [%s] got the connection: [%s]%n",
                    Thread.currentThread().getName(), connection);
            isPoolFull.signalAll(); // notify the producer threads waiting on queueFull condition
            return connection;
        } catch (InterruptedException ex) {
            System.out.printf("Consumer thread: [%s] interrupted while getting connection%n",
                    Thread.currentThread().getName());
            return null;
        } finally {
            lock.unlock();
        }
    }

    public void addConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            lock.lockInterruptibly();
            while (pool.size() == maxCapacity) {
                isPoolFull.await();
            }
            pool.add(connection);
            System.out.printf("Producer thread: [%s] added the connection: [%s]%n", Thread.currentThread().getName(), connection);
            isPoolEmpty.signalAll(); // notify the threads waiting on emptyPool condition.
        } catch (InterruptedException e) {
            System.out.printf("Producer thread: [%s] interrupted while adding connection%n",
                    Thread.currentThread().getName());
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ConnectionPoolUsingReentrantLock connectionPool = new ConnectionPoolUsingReentrantLock(5);

        // add 5 connections
        for (int i = 0; i < 5; i++) {
            connectionPool.addConnection(new Connection("connection-" + i));
        }

        ExecutorService service = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            Runnable r =() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    connectionPool.getConnection();
                }
            };
            service.submit(r);
        }

        // add next 5 connections, but main thread will block due to queue full
        for (int i = 5; i < 10; i++) {
            connectionPool.addConnection(new Connection("connection-" + i));
            Thread.sleep(2000);
        }
        service.shutdown();
        service.awaitTermination(30, TimeUnit.SECONDS);
        service.shutdownNow();
        System.out.println("main thread exiting");
    }

}
