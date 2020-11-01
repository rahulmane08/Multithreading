package synchronizers.barrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BarrierBrokenTest {

    public static void main(String[] args) throws BrokenBarrierException, InterruptedException {

        CyclicBarrier barrier = new CyclicBarrier(2);
        Thread  t = new Thread(() -> {
            String name = Thread.currentThread().getName();
            for (int i = 0; i< 10; i++) {
                try {
                    System.out.printf("Thread [%s] before executing, barrier: (%s, %s) %n", name,
                            barrier.getNumberWaiting(), barrier.isBroken());
                    barrier.await();
                    Thread.sleep(2*1000);
                    System.out.printf("Thread [%s] finishing, barrier: (%s, %s) %n", name,
                            barrier.getNumberWaiting(), barrier.isBroken());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }, "worker-thread");
        t.start();
        Thread.sleep(10*1000);
        System.out.printf("Thread [%s] before executing, barrier: (%s, %s) %n", Thread.currentThread().getName(),
                barrier.getNumberWaiting(), barrier.isBroken());
        barrier.await();
        Thread.sleep(10*1000);
        barrier.reset();
        System.out.printf("Thread [%s] after resetting barrier: (%s, %s) %n",
                Thread.currentThread().getName(),
                barrier.getNumberWaiting(), barrier.isBroken());
        barrier.await();
        t.join();
        System.out.println("Exiting");
    }
}
