package synchronizers.barrier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BarrierInterruptionTest {
    public static void main(String[] args) {
        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties + 1);
        for (int i = 0; i < 2; i++) {
            try {
                List<Thread> threads = spawnThreads(parties, barrier);
                Thread.sleep(5 * 1000);
                System.out.printf("Thread: [%s] before interrupting 1 thread, barrier(%s, %s) %n", Thread.currentThread().getName(),
                        barrier.getNumberWaiting(), barrier.isBroken());
                threads.get(0).interrupt();
                Thread.sleep(5 * 1000);
                barrier.await();
                for (Thread thread : threads) {
                    thread.join();
                }
            } catch (InterruptedException e) {
                System.out.printf("Thread: [%s] interrupted, barrier(%s, %s) %n", Thread.currentThread().getName(),
                        barrier.getNumberWaiting(), barrier.isBroken());
            } catch (BrokenBarrierException e) {
                System.out.printf("Thread: [%s] threw BrokenBarrierException, barrier(%s, %s)  %n", Thread.currentThread().getName(),
                        barrier.getNumberWaiting(), barrier.isBroken());
            }
        }

        System.out.println("exiting");
    }

    private static List<Thread> spawnThreads(final int noOfParties, final CyclicBarrier barrier) {
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= noOfParties; i++) {
            Runnable runnable = () -> {
                String currentThreadname = Thread.currentThread().getName();
                try {
                    Thread.sleep(2000);
                    System.out.printf("Thread: [%s] is executing, barrier = (%s,%s) %n", currentThreadname,
                            barrier.getNumberWaiting(), barrier.isBroken());
                    barrier.await();
                } catch (InterruptedException e) {
                    System.out.printf("Thread [%s] is interrupted, barrier(%s, %s)  %n", currentThreadname,
                            barrier.getNumberWaiting(), barrier.isBroken());
                } catch (BrokenBarrierException e) {
                    System.out.printf("Thread: [%s] threw BrokenBarrierException, barrier(%s, %s)  %n",
                            currentThreadname, barrier.getNumberWaiting(), barrier.isBroken());
                }
            };
            threads.add(new Thread(runnable));
        }
        threads.forEach(Thread::start);
        return threads;
    }
}
