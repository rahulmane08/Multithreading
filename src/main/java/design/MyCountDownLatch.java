package design;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyCountDownLatch {

    private volatile int count = 0;
    private final Map<String, Long> workers = new HashMap<>();

    public MyCountDownLatch(int count) {
        this.count = count;
    }

    public static void main(String[] args) throws InterruptedException {
        int noOfThreads = 5;
        MyCountDownLatch masterLatch = new MyCountDownLatch(1);
        MyCountDownLatch slaveLatch = new MyCountDownLatch(noOfThreads);
        Runnable r = () -> {
            try {
                masterLatch.await();
                Thread.sleep(2 * 1000);
                System.out.println(Thread.currentThread().getName() + " finished");
                slaveLatch.countDown();
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " interrupted");
            }
        };

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < noOfThreads; i++) {
            threads.add(new Thread(r));
        }

        threads.forEach(Thread::start);

        Thread.sleep(5 * 1000);
        masterLatch.countDown();
        slaveLatch.await();
        System.out.println("Exiting");
    }

    private void decrementCount() {
        --count;
    }

    /*public boolean await() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }



        return true;
    }*/

    public void await() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        synchronized (this) {
            while (count != 0) {
                wait();
            }
            notifyAll();
        }
    }

    public void countDown() {
        synchronized (this) {
            decrementCount();
            if (count == 0) {
                System.out.printf("Thread: [%s] broke the latch %n", Thread.currentThread().getName());
                notifyAll();
            }
        }
    }
}
