package synchronizers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadLocalRandom;

public class SynchronousQueueTest {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        SynchronousQueue<Integer> queue = new SynchronousQueue<>();

        Runnable consumer = () -> {
            try {
                Integer consumedElement = queue.take();
                System.out.printf("Thread: [%s] consumed element: %s %n",
                        Thread.currentThread().getName(), consumedElement);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        };

        Runnable producer = () -> {
            Integer producedElement = ThreadLocalRandom
                    .current()
                    .nextInt();
            try {
                System.out.printf("Thread: [%s] putting element: %s %n",
                        Thread.currentThread().getName(), producedElement);
                queue.put(producedElement);
                System.out.printf("Thread: [%s] transferred the element successfully: %s %n",
                        Thread.currentThread().getName(), producedElement);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        };

        executor.submit(producer);
        executor.submit(producer);

        Thread.sleep(2 * 1000);
        executor.submit(consumer);
        Thread.sleep(2 * 1000);
        executor.submit(consumer);

        executor.shutdown();
    }
}
