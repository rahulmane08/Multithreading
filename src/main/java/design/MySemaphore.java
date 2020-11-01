package design;

import java.util.concurrent.TimeUnit;

public class MySemaphore {
    private final int permits;
    private volatile int workers;

    public MySemaphore(int permits) {
        this.permits = permits;
    }

    public void acquire() throws InterruptedException {
        checkIfInterrupted();
        synchronized (this) {
            while (this.workers == this.permits) {
                wait();
            }
            ++this.workers;
            notifyAll();
        }
    }

    public void acquire(long timeout, TimeUnit timeUnit) throws InterruptedException {
        checkIfInterrupted();
        synchronized (this) {
            while (this.workers == this.permits) {
                wait(timeUnit.toMillis(timeout));
            }
            ++this.workers;
            notifyAll();
        }
    }

    public void acquire(int permits) throws InterruptedException {
        checkIfInterrupted();
        synchronized (this) {
            while (this.workers + permits >= this.permits) {
                wait();
            }
            this.workers += permits;
            notifyAll();
        }
    }

    public void acquireUninterruptibly() {
        synchronized (this) {
            while (this.workers == this.permits) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            ++this.workers;
            notifyAll();
        }
    }

    public void acquireUninterruptibly(int permits) {
        synchronized (this) {
            while (this.workers + permits >= this.permits) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            this.workers += permits;
            notifyAll();
        }
    }

    public void acquireUninterruptibly(long timeout, TimeUnit timeUnit) {
        synchronized (this) {
            while (workers == permits) {
                try {
                    wait(timeUnit.toMillis(timeout));
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            ++workers;
            notifyAll();
        }
    }

    public boolean tryAcquire() {
        synchronized (this) {
            while (this.workers == this.permits) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // keep the interrupted status intact
                    return false;
                }
            }
            ++this.workers;
            notifyAll();
            return true;
        }
    }

    public boolean tryAcquire(long timeout, TimeUnit timeUnit) throws InterruptedException {
        checkIfInterrupted();
        synchronized (this) {
            while (this.workers == this.permits) {
                try {
                    wait(timeUnit.toMillis(timeout));
                    if (this.workers == this.permits) {
                        return false; // wait expired, immediately return if no permits available
                    }
                } catch (InterruptedException e) {
                    throw e;
                }
            }
            ++this.workers;
            notifyAll();
            return true;
        }
    }

    public void release() {
        synchronized (this) {
            --workers;
            notifyAll();
        }
    }

    public void release(int permits) {
        synchronized (this) {
            workers = Math.max(0, workers - permits);
            notifyAll();
        }
    }

    public boolean hasQueuedThreads() {
        return workers != 0;
    }

    private void checkIfInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }
}
