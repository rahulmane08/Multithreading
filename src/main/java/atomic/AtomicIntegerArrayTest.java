package atomic;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicIntegerArrayTest {
    public static void main(String[] args) throws InterruptedException {
        AtomicIntegerArray atomicArray = new AtomicIntegerArray(10);
        Runnable r = () -> {
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < atomicArray.length(); j ++) {
                    atomicArray.set(j, atomicArray.incrementAndGet(j));
                }
            }
        };

        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        Thread t3 = new Thread(r);
        t1.run();
        t2.run();
        t3.run();
        t1.join();
        t2.join();
        t3.join();
        System.out.print("Atomic integer array: [");
        for (int i = 0; i < atomicArray.length(); i++) {
            System.out.print(atomicArray.get(i) + ", ");
        }
        System.out.println("]");
    }
}
