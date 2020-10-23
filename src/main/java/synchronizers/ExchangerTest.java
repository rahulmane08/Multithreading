package synchronizers;

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExchangerTest {

    public static void main(String[] args) throws InterruptedException {
        Exchanger<String> exchanger = new Exchanger<>();

        Thread t1 = new Thread(() -> {
            String elem = "A";
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1 * 1000);
                    String prev = elem;
                    elem = exchanger.exchange(elem, 1, TimeUnit.SECONDS);
                    System.out.printf("Thread: [%s] exchanged %s for %s %n",
                            Thread.currentThread().getName(), prev, elem);
                } catch (InterruptedException e) {
                    System.out.printf("Thread: [%s] is finishing %n", Thread.currentThread().getName());
                    Thread.currentThread().interrupt();
                } catch (TimeoutException e) {
                    System.out.printf("Thread: [%s] timedout %n", Thread.currentThread().getName());
                }
            }
        }, "worker-thread-1");
        Thread t2 = new Thread(() -> {
            String elem = "B";
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(3 * 1000);
                    String prev = elem;
                    elem = exchanger.exchange(elem);
                    System.out.printf("Thread: [%s] exchanged %s for %s %n",
                            Thread.currentThread().getName(), prev, elem);
                } catch (InterruptedException e) {
                    System.out.printf("Thread: [%s] is finishing %n", Thread.currentThread().getName());
                    Thread.currentThread().interrupt();
                }
            }
        }, "worker-thread-2");

        t1.start();
        t2.start();

        Thread.sleep(10 * 1000);
        t1.interrupt();
        t2.interrupt();
        t1.join();
        t2.join();
        System.out.println("Exiting");
    }

}
