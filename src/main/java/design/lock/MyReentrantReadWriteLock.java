package design.lock;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class MyReentrantReadWriteLock implements ReadWriteLock {
    private final ReadLock readLock;
    private final WriteLock writeLock;
    private final Object lock;
    private final boolean fair;
    private final AtomicReference<Thread> owner;
    private final AtomicBoolean locked; // writer acquires
    private final ConcurrentLinkedQueue<Thread> workerThreads;
    private final AtomicInteger readerCount;

    public MyReentrantReadWriteLock() {
        this(false);
    }

    public MyReentrantReadWriteLock(boolean fair) {
        this.readLock = new ReadLock(this);
        this.writeLock = new WriteLock(this);
        this.lock = new Object();
        this.fair = fair;
        this.owner = new AtomicReference<>(null);
        this.locked = new AtomicBoolean(false);
        this.workerThreads = new ConcurrentLinkedQueue<>();
        this.readerCount = new AtomicInteger(0);
    }

    @Override
    public Lock readLock() {
        return readLock;
    }

    @Override
    public Lock writeLock() {
        return writeLock;
    }

    private void acquireLock() {
        owner.set(Thread.currentThread());
        locked.set(true);
    }

    private void releaseLock() {
        boolean flag = owner.compareAndSet(Thread.currentThread(), null);
        if (!flag) {
            throw new IllegalMonitorStateException();
        }
        locked.set(false);
        lock.notifyAll();
    }

    private void throwIfInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    private boolean isAvailable() {
        return locked.get() == false && owner.get() == null;
    }

    public boolean isLocked() {
        return !isAvailable();
    }

    private static class ReadLock implements Lock {
        private final MyReentrantReadWriteLock readWriteLock;

        private ReadLock(MyReentrantReadWriteLock readWriteLock) {
            this.readWriteLock = readWriteLock;
        }

        public void lock() {
            if (readWriteLock.isLocked()) {
                synchronized (readWriteLock.lock) {
                    while (readWriteLock.isLocked()) {
                        try {
                            readWriteLock.lock.wait();
                        } catch (InterruptedException e) {
                            // do nothing
                        }
                    }
                }
            }
            readWriteLock.readerCount.incrementAndGet();
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            readWriteLock.throwIfInterrupted();
            if (readWriteLock.isLocked()) {
                synchronized (readWriteLock.lock) {
                    while (readWriteLock.isLocked()) {
                        readWriteLock.lock.wait();
                    }
                }
            }
            readWriteLock.readerCount.incrementAndGet();
        }

        @Override
        public boolean tryLock() {
            return false;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public void unlock() {
            readWriteLock.readerCount.decrementAndGet();
        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }

    private static class WriteLock implements Lock {
        private final MyReentrantReadWriteLock readWriteLock;

        private WriteLock(MyReentrantReadWriteLock readWriteLock) {
            this.readWriteLock = readWriteLock;
        }

        @Override
        public void lock() {
            synchronized (readWriteLock.lock) {
                while (readWriteLock.isLocked() || readWriteLock.readerCount.get() != 0) {
                    try {
                        readWriteLock.lock.wait();
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
                readWriteLock.acquireLock();
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            readWriteLock.throwIfInterrupted();
            synchronized (readWriteLock.lock) {
                while (readWriteLock.readerCount.get() != 0) {
                    readWriteLock.lock.wait();
                }
                readWriteLock.acquireLock();
            }
        }

        @Override
        public boolean tryLock() {
            return false;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public void unlock() {
            synchronized (readWriteLock.lock) {
                readWriteLock.releaseLock();
            }
        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }
}
