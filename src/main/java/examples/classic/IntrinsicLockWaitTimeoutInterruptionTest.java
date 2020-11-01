package examples.classic;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class IntrinsicLockWaitTimeoutInterruptionTest {

    public static void main(String[] args) throws InterruptedException {
        AtomicBoolean flag = new AtomicBoolean(false);
        Class<IntrinsicLockWaitTimeoutInterruptionTest> clazz = IntrinsicLockWaitTimeoutInterruptionTest.class;

        Thread t = new Thread(() -> {
            String threadName = Thread.currentThread().getName();
            synchronized (clazz) {
                try {
                    if (!flag.get()) {
                        System.out.printf("Thread: [%s] going to wait on lock for 15 seconds%n",
                                threadName);
                        clazz.wait(TimeUnit.SECONDS.toMillis(15));
                        System.out.printf("Thread: [%s] resuming %n", threadName);
                    }
                    System.out.printf("Thread: [%s] executed task %n", threadName);
                } catch (InterruptedException e) {
                    System.out.printf("Thread: [%s] interrupted %n", threadName);
                }
            }
            System.out.printf("Thread: [%s] finishing %n", threadName);
        }, "worker-thread");

        /*t.start();
        Thread.sleep(5* 1000);
        t.interrupt();*/

        synchronized (clazz) {
            System.out.println("Main thread acquired lock");
            t.start();
            Thread.sleep(5 * 1000);
            t.interrupt(); // when the thread is interrupted while BLOCKING on sync block, interruptions dont take effect.
            // when its WAITING on lock interrupting will throw IEX.
            flag.set(true);
            Thread.sleep(5 * 1000);
            clazz.notify();
        }
        System.out.println("Main thread released lock");
        t.join();
        System.out.println("Exiting");
    }
}
