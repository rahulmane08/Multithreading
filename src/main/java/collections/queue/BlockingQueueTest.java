package collections.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;

public class BlockingQueueTest {

    public static void main(String[] args) throws InterruptedException {
        List<Integer> elements = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            elements.add(i);
        }
        Buffer<Integer> buffer = new Buffer<>(new ArrayBlockingQueue<>(elements.size()));
        List<Runnable> tasks = new ArrayList<>();
        tasks.add(new ProducerTask(buffer, 3, elements));
        for (int i = 0; i < 3; i++) {
            tasks.add(new ConsumerTask<>(buffer));
        }
        ExecutorService service = Executors.newFixedThreadPool(5);
        tasks.forEach(service::submit);
        Thread.sleep(60 * 1000);
        service.shutdownNow();
    }

    @AllArgsConstructor
    private static class ProducerTask<T> implements Runnable {
        private final Buffer<T> buffer;
        private final int waitTime;
        private final List<T> elements;

        @Override
        public void run() {
            try {
                for (T element : elements) {
                    Thread.sleep(waitTime * 1000);
                    buffer.put(element);
                    System.out.printf("Thread: [%s] added [%s] to buffer %n", Thread.currentThread().getName(), element);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @AllArgsConstructor
    private static class ConsumerTask<T> implements Runnable {
        private final Buffer<T> buffer;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.printf("Thread: [%s] consumed %s %n", Thread.currentThread().getName(), buffer.get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static class Buffer<T> {
        private final BlockingQueue<T> queue;

        public Buffer(BlockingQueue<T> queue) {
            this.queue = queue;
        }

        public T get() throws InterruptedException {
            return queue.take();
        }

        public T get(long timeout, TimeUnit timeUnit) throws InterruptedException {
            return queue.poll(timeout, timeUnit);
        }

        public void put(T elem) throws InterruptedException {
            if (elem == null) {
                return;
            }
            queue.put(elem);
        }
    }
}
