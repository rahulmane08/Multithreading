package synchronizers.latch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SimpleLatchTest {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                boolean acquired = latch.await(3, TimeUnit.SECONDS);
                System.out.println("acquired = " + acquired);
            } catch (InterruptedException ex) {
                System.out.println(Thread.currentThread().getName() + " interrupted");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        Thread.sleep(5 * 1000);
        latch.countDown();
    }
}
