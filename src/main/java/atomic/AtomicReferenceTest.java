package atomic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceTest {

    public static void main(String[] args) throws InterruptedException {
        String message = "";
        AtomicReference<String> atomicReference = new AtomicReference<>(message);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    atomicReference.set(Thread.currentThread().getName());
                }
            }, String.valueOf(i));
            threads.add(t);
        }

        threads.forEach(Thread::start);
        for (Thread t : threads) {
            t.join();
        }
        System.out.printf("Main thread reading message = %s, length: %s%n",
                atomicReference.toString(), atomicReference.toString().length());
    }
}
