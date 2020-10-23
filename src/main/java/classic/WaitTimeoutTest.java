package classic;

import java.util.concurrent.atomic.AtomicBoolean;

public class WaitTimeoutTest {

    public static void main(String[] args) throws InterruptedException {

        AtomicBoolean flag = new AtomicBoolean(false);

        Thread t = new Thread(() -> {
            synchronized (WaitTimeoutTest.class) {
                try {
                    while (!flag.get()) {
                        WaitTimeoutTest.class.wait(2 * 1000);
                        System.out.printf("Thread: [%s] wait timedout, state = %s %n",
                                Thread.currentThread().getName(), Thread.currentThread().getState());
                    }
                    System.out.println(Thread.currentThread().getName() + " finished");
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        t.start();
        Thread.sleep(5 * 1000);
        synchronized (WaitTimeoutTest.class) {
            flag.set(true);
            WaitTimeoutTest.class.notifyAll();
        }
        t.join();
        System.out.println("Exiting");
    }
}
