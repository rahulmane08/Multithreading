package design;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.Data;

/**
 If the current thread is not the last to arrive then it is disabled for thread scheduling purposes
 and lies dormant until one of the following things happens:
 - The last thread arrives; or -- ALL THREADS ARE NOTIFIED AND THEY GO AHEAD
 - Some other thread interrupts the current thread; or -- NOTIFIES OTHER THREADS AND THROWS IEX, OTHER THREADS THROW BBE
 - Some other thread interrupts one of the other waiting threads; or CURRENT THREAD THROWS BBE, THAT THREAD THROWS IEX
 - Some other thread times out while waiting for barrier; or
 - Some other thread invokes reset() on this barrier.
 */
@Data
public class MyCyclicBarrier {

    private int parties;
    private boolean broken;
    private int counter;
    private Queue<Thread> waiters = new LinkedList<>();

    public MyCyclicBarrier(int parties) {
        this.parties = parties;
    }

    public int getNumberWaiting() {
        return waiters.size();
    }

    public int await() throws InterruptedException, BrokenBarrierException {
        throwIfInterrupted(); // interrupted threads not allowed
        throwIfBarrierBroken(); // threads shouldnt use broken barrier
        synchronized (this) {
            int arrivalCount = ++counter;
            try {
                if (counter != parties) {
                    waiters.add(Thread.currentThread());
                    wait();
                    waiters.remove(Thread.currentThread());
                }
                throwIfBarrierBroken();
            } catch (InterruptedException ex) {
                breakBarrier(); // current thread is interrupted, so break the barrier, other threads to throw BBE
                throw ex; // current thread throws IEX
            }
            notifyAll();
            return arrivalCount;
        }
    }

    public int await(long timeout, TimeUnit timeUnit) throws InterruptedException, BrokenBarrierException, TimeoutException {
        throwIfInterrupted();
        synchronized (this) {
            int arrivalCount = ++counter;
            try {
                if (counter != parties) {
                    waiters.add(Thread.currentThread());
                    wait(timeUnit.toMillis(timeout));
                    waiters.remove(Thread.currentThread());
                    throwIfTimedOut();
                }
                throwIfBarrierBroken();
            } catch (InterruptedException ex) {
                breakBarrier();
                throw ex;
            }
            notifyAll();
            return arrivalCount;
        }
    }

    private void breakBarrier() {
        synchronized (this) {
            broken = true;
            notifyAll(); // if current thread is interrupted then notify all
        }
    }

    private void throwIfInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    private void throwIfBarrierBroken() throws BrokenBarrierException {
        synchronized (this) {
            if (broken) {
                throw new BrokenBarrierException();
            }
        }
    }

    private void throwIfTimedOut() throws TimeoutException {
        if (counter != parties) {
            breakBarrier();
            throw new TimeoutException();
        }
    }

    public static void main(String[] args) throws BrokenBarrierException, InterruptedException {
        int parties = 3;
        MyCyclicBarrier barrier = new MyCyclicBarrier(parties + 1);
        List<Thread> threads = spawnThreads(parties, barrier);
        Thread.sleep(10 * 1000);
        barrier.await();
        for (Thread thread : threads) {
            thread.join();
        }

        barrier = new MyCyclicBarrier(parties + 1);
        threads = spawnThreads(parties, barrier);
        Thread.sleep(2000);
        threads.get(0).interrupt();
        for (Thread thread : threads) {
            thread.join();
        }
        barrier.await();
        System.out.println("Main thread finishing");
    }

    private static List<Thread> spawnThreads(final int noOfParties, final MyCyclicBarrier barrier) {
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= noOfParties; i++) {
            Runnable runnable = () -> {
                String currentThreadname = Thread.currentThread().getName();
                try {
                    barrier.await();
                    Thread.sleep(2000);
                    System.out.println(currentThreadname + " is now executing");
                } catch (InterruptedException e) {
                    System.out.printf("Thread [%s] is interrupted, barrier = (%s,%s) %n", currentThreadname,
                            barrier.getNumberWaiting(), barrier.isBroken());
                } catch (BrokenBarrierException e) {
                    System.out.printf("Thread: [%s] threw BrokenBarrierException, barrier = (%s,%s)  %n", currentThreadname,
                            barrier.getNumberWaiting(), barrier.isBroken());
                }
            };
            threads.add(new Thread(runnable));
        }
        threads.forEach(Thread::start);
        return threads;
    }
}
