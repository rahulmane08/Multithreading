package atomic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceTest {

    public static void main(String[] args) throws InterruptedException {
        String message = "hello";
        AtomicReference<String> atomicReference = new AtomicReference<>(message);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(() -> {
                atomicReference.set(atomicReference.get() + "-" + Thread.currentThread().getName());
            }, String.valueOf(i));
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        System.out.printf("Main thread reading message = %s%n", atomicReference.toString());
    }
}
