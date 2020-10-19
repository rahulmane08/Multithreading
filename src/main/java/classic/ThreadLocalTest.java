package classic;

import java.util.ArrayList;
import java.util.List;

public class ThreadLocalTest {

    private static class Counter {
        private int counter = 0;

        public void increment() {
            counter++;
        }

        public int getCounter() {
            return counter;
        }
    }

    private static class CacheContext {
        private static ThreadLocal<Counter> context = new ThreadLocal<>();

        public static void setCacheVal(Counter counter) {
            context.set(counter);
        }

        public static Counter getCacheVal() {
            return context.get();
        }

        public static void clearCacheVal() {
            context.remove();
        }

        public static Counter initVal() {
            Counter counter = new Counter();
            setCacheVal(counter);
            return counter;
        }
    }

    public static void main(String[] args) {
        Counter counter = new Counter();

        CacheContext.setCacheVal(counter);

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(() -> {
                int loop = (int) (Math.random() * 10);
                Counter threadCounter = CacheContext.initVal();
                System.out.println(Thread.currentThread().getName() + " will increment counter for loops = "+ loop);
                for (int j = 0; j < loop; j++) {
                    threadCounter.increment();
                }
                CacheContext.setCacheVal(threadCounter);

                try {
                    Thread.sleep(2*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println(Thread.currentThread().getName() + " final counter val = " +
                        CacheContext.getCacheVal().getCounter());
            });
            threads.add(t);
        }

        threads.forEach(Thread::start);

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        System.out.println("Main threads counter = " + CacheContext.getCacheVal().getCounter());
    }
}
