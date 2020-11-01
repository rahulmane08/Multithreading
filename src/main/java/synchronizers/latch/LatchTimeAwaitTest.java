package synchronizers.latch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LatchTimeAwaitTest {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1) {
            @Override
            public String toString() {
                return String.format("[latch count = %s]", getCount());
            }
        };

        Thread t = new Thread(() -> {
            String name = Thread.currentThread().getName();
            try {
                boolean acquired = latch.await(3, TimeUnit.SECONDS);
                System.out.printf("Thread: [%s] finishing, latch acquired? : %s = %s %n", name, latch, acquired);
            } catch (InterruptedException ex) {
                System.out.printf("Thread: [%s] is interrupted %n", name, latch);
            }
        }, "worker");

        t.start();
        Thread.sleep(5 * 1000);
        latch.countDown();
        t.join();

        t = new Thread(() -> {
            String name = Thread.currentThread().getName();
            try {
                boolean acquired = latch.await(3, TimeUnit.SECONDS);
                System.out.printf("Thread: [%s] finishing, latch acquired? : %s = %s %n", name, latch, acquired);
            } catch (InterruptedException ex) {
                System.out.printf("Thread: [%s] is interrupted %n", name, latch);
            }
        }, "worker");
        t.start();
        System.out.printf("Thread: [%s] finishing: %s%n", Thread.currentThread().getName(), latch);
    }
}
