package examples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import lombok.AllArgsConstructor;

public class PrintSequentialReentrantLock {

    @AllArgsConstructor
    private static class SequentialTask implements Runnable {
        private final CountDownLatch latch;
        private final int partyCount;
        private final int totalParties;
        private final AtomicInteger currentParty;
        private final List<Integer> data;
        private final ReentrantLock lock;
        private final Condition condition;


        @Override
        public void run() {
            Thread thread = Thread.currentThread();
            String threadName = thread.getName();
            while (!thread.isInterrupted()) {
                try {
                    lock.lockInterruptibly();

                    while (partyCount != currentParty.get()) {
                        condition.await();
                    }

                    if (!data.isEmpty()) {
                        System.out.printf("Thread: [%s] printing: %s %n",
                                Thread.currentThread().getName(), data.remove(0));
                    } else {
                        latch.countDown();
                    }

                    int nextParty = (partyCount + 1 ) % totalParties;
                    currentParty.set(nextParty);
                    condition.signalAll();
                } catch (InterruptedException e) {
                    System.out.printf("Thread: [%s] interrupted. %n", Thread.currentThread().getName());
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
            System.out.printf("Thread: [%s] is interrupted and finishing %n", threadName);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int parties = 4;
        CountDownLatch latch = new CountDownLatch(parties);
        AtomicInteger currentParty = new AtomicInteger(0);
        List<Integer> data = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        for (int i = 0; i < 200; i++) {
            data.add(i);
        }

        for (int i = 0; i < parties; i++) {
            threads.add(new Thread(
                    new SequentialTask(latch, i, parties, currentParty, data, lock, condition), "Party-" + i));
        }
        threads.forEach(Thread::start);
        latch.await();
        threads.forEach(Thread::interrupt);
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("exiting");
    }
}
