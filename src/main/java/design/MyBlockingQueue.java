package design;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue<T> {
    private final Queue<T> queue;
    private final int maxCapacity;
    private final Lock lock;
    private final Condition isFull;
    private final Condition isEmpty;

    public MyBlockingQueue(int capacity) {
        maxCapacity = capacity;
        this.queue = new ArrayDeque<>(maxCapacity);
        this.lock  = new ReentrantLock(true);
        this.isFull = lock.newCondition();
        this.isEmpty  = lock.newCondition();
    }

    public void put(T element) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == maxCapacity) {
                isFull.await();
            }
            boolean isAdded = queue.offer(element);
            if (isAdded) {
                isEmpty.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {
        lock.lock();
        try {
            T element;
            while (queue.size() == 0) {
                isEmpty.await();
            }
            element = queue.poll();
            isFull.signalAll();
            return element;
        } finally {
            lock.unlock();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        final MyBlockingQueue<String> queue = new MyBlockingQueue<String>(10);
        queue.put("ELEMENT");

        Runnable consumer = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String element = queue.take();
                    System.out.println(Thread.currentThread().getName() + " takes " + element);
                    Thread.sleep(2000);
                    queue.put(element);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        };

        Thread consumer1 = new Thread(consumer, "consumer1");
        Thread consumer2 = new Thread(consumer, "consumer2");
        consumer1.start();
        consumer2.start();
        Thread.sleep(20 * 1000);
        consumer1.interrupt();
        consumer2.interrupt();
        consumer1.join();
        consumer2.join();
    }
}
