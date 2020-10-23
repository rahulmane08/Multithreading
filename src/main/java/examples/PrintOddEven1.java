package examples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PrintOddEven1 {

    public static void main(String[] args) throws InterruptedException {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }

        ReentrantLock lock = new ReentrantLock();
        Condition isEven = lock.newCondition();
        Condition isOdd = lock.newCondition();
        AtomicInteger index = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(2);

        Thread oddThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    lock.lockInterruptibly();
                    if (index.get() % 2 == 0) {
                        isOdd.await();
                    }

                    if (index.get() >= list.size()) {
                        System.out.printf("Thread [%s] has nothing to process %n", Thread.currentThread().getName());
                        Thread.sleep(2 * 1000);
                        latch.countDown();
                        continue;
                    }

                    Thread.sleep(2 * 1000);
                    System.out.printf("Thread [%s] printing: %d%n",
                            Thread.currentThread().getName(), list.get(index.getAndIncrement()));
                    isEven.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
            System.out.printf("Thread [%s] finishing%n", Thread.currentThread().getName());
        }, "odd-thread");

        Thread evenThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    lock.lockInterruptibly();
                    if (index.get() % 2 == 1) {
                        isEven.await();
                    }

                    if (index.get() >= list.size()) {
                        System.out.printf("Thread [%s] has nothing to process %n", Thread.currentThread().getName());
                        Thread.sleep(2 * 1000);
                        latch.countDown();
                        continue;
                    }

                    Thread.sleep(2 * 1000);
                    System.out.printf("Thread [%s] printing: %d%n",
                            Thread.currentThread().getName(), list.get(index.getAndIncrement()));
                    isOdd.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
            System.out.printf("Thread [%s] finishing%n", Thread.currentThread().getName());
        }, "even-thread");

        oddThread.start();
        evenThread.start();

        latch.await();

        System.out.println("main thread resumed");
        Thread.sleep(10 * 1000);
        oddThread.interrupt();
        evenThread.interrupt();
        oddThread.join();
        evenThread.join();
        System.out.println("Exiting");
    }
}
