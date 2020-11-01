package synchronizers.barrier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BarrierTimedAwaitTest {

    public static void main(String[] args) {
        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties + 1);
        List<Thread> threads = spawnThreads(parties, barrier);
        try {
            Thread.sleep(5 * 1000);
            barrier.await();
        } catch (InterruptedException e) {
            System.out.printf("Thread: [%s] interrupted %n", Thread.currentThread().getName());
        } catch (BrokenBarrierException e) {
            System.out.printf("Thread: [%s] threw BrokenBarrierException %n", Thread.currentThread().getName());
        }
        System.out.println("exiting");
    }

    private static List<Thread> spawnThreads(final int noOfParties, final CyclicBarrier barrier) {
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= noOfParties; i++) {
            int finalI = i;
            Runnable runnable = () -> {
                String currentThreadname = Thread.currentThread().getName();
                try {
                    if (finalI == 3) {
                        // of the threads times out on await.
                        barrier.await(2, TimeUnit.SECONDS);
                    } else {
                        barrier.await();
                    }
                    Thread.sleep(2000);
                    System.out.println(currentThreadname + " is now executing");
                } catch (InterruptedException e) {
                    System.out.printf("Thread [%s] is interrupted, barrier = (%s,%s) %n", currentThreadname,
                            barrier.getNumberWaiting(), barrier.isBroken());
                } catch (BrokenBarrierException e) {
                    System.out.printf("Thread: [%s] threw BrokenBarrierException, barrier = (%s,%s)  %n", currentThreadname,
                            barrier.getNumberWaiting(), barrier.isBroken());
                } catch (TimeoutException e) {
                    System.out.printf("Thread: [%s] threw TimeoutException, barrier = (%s,%s)  %n", currentThreadname,
                            barrier.getNumberWaiting(), barrier.isBroken());
                }
            };
            threads.add(new Thread(runnable));
        }
        threads.forEach(Thread::start);
        return threads;
    }
}
