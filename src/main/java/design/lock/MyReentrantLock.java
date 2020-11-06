package design.lock;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MyReentrantLock implements Lock {

    private final boolean fair;
    private final Object lock;
    private final AtomicReference<Thread> owner;
    private final AtomicBoolean locked;
    private final ConcurrentLinkedQueue<Thread> workerThreads;

    public MyReentrantLock() {
        this(false);
    }

    public MyReentrantLock(boolean fair) {
        this.lock = new Object();
        this.fair = fair;
        this.owner = new AtomicReference<>(null);
        this.locked = new AtomicBoolean(false);
        this.workerThreads = new ConcurrentLinkedQueue<>();
    }

    //---------LOCK METHODS------
    @Override
    public void lock() {
        if (fair) {
            lockFairly();
            return;
        }

        if (isHeldByCurrentThread()) {
            // reentrancy
            System.out.println("Lock is held by current thread");
            return;
        }
        synchronized (lock) {
            while (!isAvailable()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            acquireLock();
        }
    }

    private void lockFairly() {
        if (isHeldByCurrentThread()) {
            // reentrancy
            System.out.println("Lock is held by current thread");
            return;
        }
        addWorker();
        synchronized (lock) {
            while (!isAvailable() || workerThreads.peek() != Thread.currentThread()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            acquireLock();
            removeWorker();
        }
    }

    private void addWorker() {
        if (!workerThreads.contains(Thread.currentThread())) {
            workerThreads.offer(Thread.currentThread());
        }
    }

    private void removeWorker() {
        workerThreads.poll();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throwIfInterrupted();
        synchronized (lock) {
            while (!isAvailable()) {
                lock.wait();
            }
            acquireLock();
        }
    }

    @Override
    public boolean tryLock() {
        if (isAvailable()) {
            synchronized (lock) {
                if (isAvailable()) {
                    acquireLock();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long expiry = System.currentTimeMillis() + unit.toMillis(time);
        while (System.currentTimeMillis() < expiry) {
            throwIfInterrupted();
            if (tryLock()) {
                return true;
            }
        }
        return false;
    }

    private void acquireLock() {
        owner.set(Thread.currentThread());
        locked.set(true);
    }
    //---------END LOCK METHODS------

    //-----UNLOCK------------
    @Override
    public void unlock() {
        synchronized (lock) {
            boolean flag = owner.compareAndSet(Thread.currentThread(), null);
            if (!flag) {
                throw new IllegalMonitorStateException();
            }
            locked.set(false);
            lock.notifyAll();
        }
    }
    //-----UNLOCK------------


    @Override
    public Condition newCondition() {
        return null;
    }

    private boolean isAvailable() {
        return locked.get() == false && owner.get() == null;
    }

    public boolean isLocked() {
        return !isAvailable();
    }

    public boolean isFair() {
        return fair;
    }

    private void throwIfInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    public boolean isHeldByCurrentThread() {
        return owner.get() == Thread.currentThread();
    }

}
