package synchronizers.barrier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


public class CyclicBarrierTest {
    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties + 1);
        List<Thread> threads = spawnThreads(parties, barrier);
        Thread.sleep(10000);
        barrier.await();
        for (Thread thread : threads) {
            thread.join();
        }
        threads = spawnThreads(parties, barrier);;
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("exiting");
    }

    private static List<Thread> spawnThreads(final int noOfParties, final CyclicBarrier barrier) {
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= noOfParties; i++) {
            Thread t = new Thread(() -> {
                String currentThreadname = Thread.currentThread().getName();
                try {
                    System.out.printf("Thread: [%s] starting, barrier(%s, %s)  %n", currentThreadname,
                            barrier.getNumberWaiting(), barrier.isBroken());
                    barrier.await();
                    Thread.sleep(2000);
                    System.out.printf("Thread: [%s] finishing, barrier(%s, %s)  %n", currentThreadname,
                            barrier.getNumberWaiting(), barrier.isBroken());
                } catch (InterruptedException e) {
                    System.out.printf("Thread [%s] is interrupted, barrier(%s, %s)  %n", currentThreadname,
                            barrier.getNumberWaiting(), barrier.isBroken());
                } catch (BrokenBarrierException e) {
                    System.out.printf("Thread: [%s] threw BrokenBarrierException, barrier(%s, %s)  %n",
                            currentThreadname, barrier.getNumberWaiting(), barrier.isBroken());
                }
            });
            threads.add(t);
        }
        threads.forEach(Thread::start);
        return threads;
    }
}
