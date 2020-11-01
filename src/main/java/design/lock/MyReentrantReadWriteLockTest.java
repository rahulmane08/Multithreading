package design.lock;

import java.util.ArrayList;
import java.util.List;

public class MyReentrantReadWriteLockTest {
    public static void main(String[] args) throws InterruptedException {
        MyReentrantReadWriteLock readWriteLock = new MyReentrantReadWriteLock();

        Runnable reader = () -> {
            Thread thread = Thread.currentThread();
            String threadName = thread.getName();
            while (true) {
                try {
                    readWriteLock.readLock().lock();
                    System.out.printf("Thread: [%s] acquired lock %n", Thread.currentThread().getName());
                    Thread.sleep(2*1000);
                } catch (InterruptedException e) {
                    System.out.printf("Thread: [%s] interrupted %n", threadName);
                } finally {
                    readWriteLock.readLock().unlock();
                }
            }
        };

        Runnable writer = () -> {
            Thread thread = Thread.currentThread();
            String threadName = thread.getName();
            while (true) {
                try {
                    readWriteLock.writeLock().lock();
                    System.out.printf("Thread: [%s] acquired lock %n", Thread.currentThread().getName());
                    Thread.sleep(5*1000);
                } catch (InterruptedException e) {
                    System.out.printf("Thread: [%s] interrupted %n", threadName);
                } finally {
                    readWriteLock.writeLock().unlock();
                }
            }
        };

        List<Thread> readers = new ArrayList<>();
        List<Thread> writers = new ArrayList<>();

        for (int i=0; i< 5; i++) {
            readers.add(new Thread(reader, "READER-" +i));
        }

        for (int i=0; i< 2; i++) {
            writers.add(new Thread(writer, "WRITER-" +i));
        }


        readers.forEach(Thread::start);
        Thread.sleep(1000);
        writers.forEach(Thread::start);

        for (Thread thread : readers) {
            thread.join();
        }
    }
}
