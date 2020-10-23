package examples;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import lombok.AllArgsConstructor;
import lombok.Data;

public class PrintEvenOdd {

    public static void main(String[] args) throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        CountDownLatch latch = new CountDownLatch(2);
        Task task = new Task(new int[]{1, 2, 3, 4, 5, 6, 7});
        OddEvenThread evenThread = new OddEvenThread(task, true, latch, lock);
        OddEvenThread oddThread = new OddEvenThread(task, false, latch, lock);
        evenThread.start(); // even thread
        oddThread.start(); // odd thread
        latch.await();
        evenThread.interrupt();
        oddThread.interrupt();
        evenThread.join();
        oddThread.join();
        System.out.println("Exiting");
    }

    @Data
    static class Task {
        private final int[] arr;
        private boolean flag = true; // startwith even
        private int index = 0;

        public Task(int[] arr) {
            this.arr = arr;
        }

        private boolean isFinished() {
            return this.index >= this.arr.length;
        }

        private int getNext() {
            return arr[index++];
        }
    }

    @AllArgsConstructor
    @Data
    static
    class OddEvenThread extends Thread {
        private final Task task;
        private final boolean even;
        private final CountDownLatch latch;
        private final ReentrantLock reentrantLock;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    reentrantLock.lockInterruptibly();
                    if (task.isFinished()) {
                        System.out.println("Array exhausted, task finished");
                        latch.countDown();
                        break;
                    }
                    if (task.isFlag() == this.even) {
                        System.out.printf("Current thread: %s prints arr[%d]=%d%n",
                                Thread.currentThread().getName(), task.getIndex(), task.getNext());
                        task.setFlag(!task.isFlag());
                    }
                } catch (InterruptedException e) {
                    System.out.println(Thread.currentThread().getName() + " interrupted and is finishing");
                    Thread.currentThread().interrupt();
                } finally {
                    reentrantLock.unlock();
                }
            }
        }
    }
}
