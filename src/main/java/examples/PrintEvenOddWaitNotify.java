package examples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Data;

public class PrintEvenOddWaitNotify {

    public static void main(String[] args) throws InterruptedException {

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        CountDownLatch latch = new CountDownLatch(2);
        AtomicBoolean currentRoundFlag = new AtomicBoolean(true);

        Thread evenThread = new Thread(new Printer(list, true, latch, currentRoundFlag), "even-thread");
        Thread oddThread = new Thread(new Printer(list, false, latch, currentRoundFlag), "odd-thread");
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

    @Data
    static class Printer implements Runnable {
        private final List<Integer> list;
        private final boolean flag;
        private final AtomicBoolean currentRoundFlag;
        private final CountDownLatch latch;

        public Printer(List<Integer> list, boolean flag, CountDownLatch latch, AtomicBoolean currentRoundFlag) {
            this.list = list;
            this.flag = flag;
            this.latch = latch;
            this.currentRoundFlag = currentRoundFlag;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (list) {
                        while (currentRoundFlag.get() != flag) {
                            list.wait();
                        }
                        if (!list.isEmpty()) {
                            System.out.printf("Thread: [%s] printing: %s %n", threadName, list.remove(0));
                            currentRoundFlag.set(!flag);
                            Thread.sleep(2 * 1000);
                        } else {
                            latch.countDown();
                        }
                        list.notify();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.printf("Thread: [%s] is interrupted, hence finishing %n", threadName);
        }
    }
}
