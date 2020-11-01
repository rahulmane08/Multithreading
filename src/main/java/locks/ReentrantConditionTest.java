package locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantConditionTest {

    public static void main(String[] args) throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        Runnable r = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    lock.lockInterruptibly();
                    System.out.printf("Thread: [%s] got the lock%n", Thread.currentThread().getName());
                    condition.await();
                    System.out.printf("Thread: [%s] got the lock again %n", Thread.currentThread().getName());
                    Thread.sleep(4000);
                    System.out.printf("Thread: [%s] executed task %n", Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    System.out.printf("Thread: [%s] interrupted%n", Thread.currentThread().getName());
                    Thread.currentThread().interrupt();
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        System.out.printf("Thread: [%s] released the lock%n", Thread.currentThread().getName());
                        lock.unlock();
                    }
                }
            }
            System.out.printf("Thread: [%s] finishing %n", Thread.currentThread().getName());
        };

        Thread t1 = new Thread(r, "T1");
        Thread t2 = new Thread(r, "T2");

        t1.start();
        t2.start();
        Thread.sleep(2000);
        lock.lock();
        System.out.printf("Thread: [%s] signalled the condition %n", Thread.currentThread().getName());
        condition.signalAll();
        Thread.sleep(5000);
        System.out.printf("Thread: [%s] releasing the lock %n", Thread.currentThread().getName());
        lock.unlock();
        Thread.sleep(5000);

        t1.interrupt();
        t2.interrupt();
        t1.join();
        t2.join();
        System.out.println("exiting");
    }
}
