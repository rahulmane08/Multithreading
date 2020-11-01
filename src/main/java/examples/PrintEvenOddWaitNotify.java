package examples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.AllArgsConstructor;
import lombok.Data;

public class PrintEvenOddWaitNotify {

    public static void main(String[] args) throws InterruptedException {

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        CountDownLatch latch = new CountDownLatch(2);
        AtomicBoolean currentRoundFlag = new AtomicBoolean(true);

        Thread evenThread = new Thread(new OddEvenPrinterTask(list, true, latch, currentRoundFlag), "even-thread");
        Thread oddThread = new Thread(new OddEvenPrinterTask(list, false, latch, currentRoundFlag), "odd-thread");
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
    @AllArgsConstructor
    static class OddEvenPrinterTask implements Runnable {
        private final List<Integer> data;
        private final boolean flag;
        private final CountDownLatch latch;
        private final AtomicBoolean currentRoundFlag;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (data) {
                    try {
                        while (flag != currentRoundFlag.get()) {
                            data.wait();
                        }

                        if (!data.isEmpty()) {
                            System.out.printf("Thread: [%s] printing: %s %n",
                                    Thread.currentThread().getName(), data.remove(0));
                        } else {
                            latch.countDown();
                        }
                        currentRoundFlag.set(!flag);
                        data.notifyAll();
                    } catch (Exception ex) {
                        System.out.printf("Thread: [%s] interrupted. %n", Thread.currentThread().getName());
                        Thread.currentThread().interrupt();
                    }
                }
            }
            System.out.printf("Thread: [%s] is interrupted and finishing %n", Thread.currentThread().getName());
        }
    }
}
