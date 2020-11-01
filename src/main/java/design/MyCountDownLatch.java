package design;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MyCountDownLatch {

    private int count = 0;

    public MyCountDownLatch(int count) {
        this.count = count;
    }

    public void await() throws InterruptedException {
        throwIfInterrupted();
        synchronized (this) {
            if (count != 0) {
                wait();
            }
            throwIfInterrupted();
            notifyAll();
        }
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        throwIfInterrupted();
        synchronized (this) {
            if (count != 0) {
                wait(unit.toMillis(timeout));
            }
            throwIfInterrupted();
            return count == 0;
        }
    }

    private void throwIfInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    public void countDown() {
        synchronized (this) {
            if (--count == 0) {
                System.out.printf("Thread: [%s] broke the latch %n", Thread.currentThread().getName());
                notifyAll();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        testAwait();
        //testTimedAwait(2, TimeUnit.SECONDS);
        System.out.println("Exiting");
    }

    private static void testTimedAwait(long timeout, TimeUnit timeUnit) throws InterruptedException {
        int noOfThreads = 5;
        MyCountDownLatch masterLatch = new MyCountDownLatch(1);
        MyCountDownLatch slaveLatch = new MyCountDownLatch(noOfThreads);
        Runnable r = () -> {
            try {
                boolean await = masterLatch.await(timeout, timeUnit);
                System.out.printf("Thread: [%s] timeout on waiting for master latch = %s %n",
                        Thread.currentThread().getName(), await);
                Thread.sleep(2 * 1000);
                System.out.printf("Thread: [%s] finished %n", Thread.currentThread().getName());
                slaveLatch.countDown();
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " interrupted");
            }
        };

        executeThreads(noOfThreads, masterLatch, slaveLatch, r);
    }

    private static void testAwait() throws InterruptedException {
        int noOfThreads = 5;
        MyCountDownLatch masterLatch = new MyCountDownLatch(1);
        MyCountDownLatch slaveLatch = new MyCountDownLatch(noOfThreads);
        Runnable r = () -> {
            try {
                System.out.printf("Thread: [%s] waiting for master latch%n", Thread.currentThread().getName());
                masterLatch.await();
                Thread.sleep(2 * 1000);
                System.out.printf("Thread [%s] finished %n", Thread.currentThread().getName());
                slaveLatch.countDown();
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " interrupted");
            }
        };

        executeThreads(noOfThreads, masterLatch, slaveLatch, r);
    }

    private static void executeThreads(int noOfThreads,
                                       MyCountDownLatch masterLatch,
                                       MyCountDownLatch slaveLatch,
                                       Runnable r) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < noOfThreads; i++) {
            threads.add(new Thread(r));
        }

        threads.forEach(Thread::start);

        Thread.sleep(10 * 1000);
        masterLatch.countDown();
        slaveLatch.await();
        System.out.printf("Thread [%s] finished %n", Thread.currentThread().getName());
    }
}
