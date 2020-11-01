package design.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MyThreadPoolExecutor {

    private final List<Thread> workerThreads;
    private final BlockingQueue<MyFutureTask<?>> taskQueue;
    private final AtomicBoolean shouldShutdown;
    private final AtomicBoolean forceShutdown;

    public MyThreadPoolExecutor(int poolSize) {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.shouldShutdown = new AtomicBoolean(false);
        this.forceShutdown = new AtomicBoolean(false);
        this.workerThreads = new ArrayList<>(poolSize);
        startWorkerPool(poolSize);
    }

    private void startWorkerPool(int poolSize) {
        for (int i = 0; i < poolSize; i++) {
            Runnable r = () -> {
                while (true) {
                    if (forceShutdown.get()) {
                        System.out.printf("Service forced to shutdown, thread: {%s} exiting %n",
                                Thread.currentThread().getName());
                        break;
                    }

                    if (shouldShutdown.get() && taskQueue.isEmpty()) {
                        System.out.printf("Service graceful shutdown, thread: {%s} exiting %n",
                                Thread.currentThread().getName());
                        break;
                    }

                    try {
                        MyFutureTask<?> futureTask = taskQueue.take();
                        futureTask.execute();
                    } catch (InterruptedException e) {

                    }
                }
            };
            workerThreads.add(new Thread(r, "worker-" + i));
        }
        workerThreads.forEach(Thread::start);
    }

    public <T> MyFutureTask submit(Callable<T> callable) {
       MyFutureTask<T> task = new MyFutureTask<>(callable);
       this.taskQueue.offer(task);
       return task;
    }

    public static class MyFutureTask<T> {

        private final CountDownLatch finished = new CountDownLatch(1);
        private final Callable<T> task;
        private final AtomicReference<T> result = new AtomicReference<>();
        private AtomicBoolean success = new AtomicBoolean(false);
        private ExecutionException executionException;

        public MyFutureTask(Callable<T> task) {
            this.task = task;
        }

        public T get() throws InterruptedException, ExecutionException {
            finished.await();
            if (!success.get()) {
                throw executionException;
            }
            return result.get();
        }

        public void execute() {
            try {
                T resultData = task.call();
                System.out.printf("Thread: [%s] got result: %s %n", Thread.currentThread().getName(), resultData);
                result.set(resultData);
            } catch (Exception exception) {
                success.set(false);
                executionException = new ExecutionException(exception);
            }
            finished.countDown();
        }
    }
}
