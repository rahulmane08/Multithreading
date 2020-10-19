package locks;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockInterruptiblyTest {

    public static void main(String[] args) throws InterruptedException {

        Lock lock = new ReentrantLock();
        Thread thread = new Thread(() -> {
            try {
                lock.lockInterruptibly();
//                lock.lock();
                Thread.sleep(5 * 1000);
                System.out.println(Thread.currentThread().getName() + "finishing execution");
            } catch (InterruptedException e) {
                System.out.printf("Thread: [%s] is interrupted hence cant acquire the lock", Thread.currentThread().getName());
            } finally {
                lock.unlock();
            }
        }, "worker-thread-1");
        lock.lock(); // main thread has locked, worker cant access.
        thread.start();
        Thread.sleep(5 * 1000);
        thread.interrupt();// issue a interrupt
        lock.unlock();

        thread.join();
    }
}
