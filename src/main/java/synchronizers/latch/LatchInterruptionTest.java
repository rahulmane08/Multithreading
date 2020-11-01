package synchronizers.latch;

import java.util.concurrent.CountDownLatch;

public class LatchInterruptionTest {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1) {
            @Override
            public String toString() {
                return String.format("[latch count = %s]", getCount());
            }
        };

        Thread t = new Thread(() -> {
            try {
                System.out.printf("Thread: %s waiting on latch %n", Thread.currentThread().getName());
                latch.await();
            } catch (InterruptedException e) {
                System.out.printf("Thread: %s is interrupted %n", Thread.currentThread().getName());
            }
        }, "worker");

        t.start();
        Thread.sleep(5000);
        System.out.println("latch = " + latch);
        t.interrupt();
        Thread.sleep(3000);
        latch.countDown();
        t.join();
        System.out.println("exiting");
    }
}
