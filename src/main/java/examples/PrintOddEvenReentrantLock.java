package examples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import lombok.AllArgsConstructor;

public class PrintOddEvenReentrantLock {

    @AllArgsConstructor
    private static class OddEvenPrinterTask implements Runnable {
        private final List<Integer> data;
        private final boolean flag;
        private final CountDownLatch latch;
        private final AtomicBoolean currentRoundFlag;
        private ReentrantLock lock;
        private Condition condition;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    lock.lockInterruptibly();
                    while (currentRoundFlag.get() != flag) {
                        condition.await();
                    }

                    if (!data.isEmpty()) {
                        System.out.printf("Thread: [%s] printing: %s %n",
                                Thread.currentThread().getName(), data.remove(0));
                    } else {
                        latch.countDown();
                    }
                    currentRoundFlag.set(!flag);
                    condition.signalAll();
                } catch (InterruptedException ex) {
                    System.out.printf("Thread: [%s] interrupted. %n", Thread.currentThread().getName());
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
            System.out.printf("Thread: [%s] is interrupted and finishing %n", Thread.currentThread().getName());
        }
    }

    public static void main(String[] args) throws InterruptedException {

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        CountDownLatch latch = new CountDownLatch(2);
        AtomicBoolean currentRoundFlag = new AtomicBoolean(true);
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        Thread evenThread = new Thread(new OddEvenPrinterTask(list, true, latch, currentRoundFlag, lock, condition),
                "even-thread");
        Thread oddThread = new Thread(new OddEvenPrinterTask(list, false, latch, currentRoundFlag, lock, condition),
                "odd-thread");
        evenThread.start();
        oddThread.start();

        latch.await();
        Thread.sleep(10 * 1000);
        oddThread.interrupt();
        evenThread.interrupt();
        oddThread.join();
        evenThread.join();
        System.out.println("Exiting");
    }
}
