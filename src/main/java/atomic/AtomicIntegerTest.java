package atomic;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 *

 A very important difference is that the methods compareAndSet and weakCompareAndSet have different semantics
 for AtomicReference<Integer> than they do for AtomicInteger.
 This is because with AtomicReferece<Integer>,
 those methods use == for comparing and two Integer objects can be equal without being ==.
 With AtomicInteger, the comparison is of the integer value equality, not reference identity.
 As others have pointed out, AtomicInteger has additional features not available with AtomicReference<Integer>.
 Also, AtomicInteger extends Number, so it inherits all the Number methods (doubleValue(), etc.)
 and can be used whenever a Number is expected.

 */
public class AtomicIntegerTest {
    public static void main(String[] args) throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        AtomicReference<Integer> atomicReferenceInteger = new AtomicReference<>(0);
        Runnable r = () -> {
            for (int i = 0; i < 100000000; i++) {
                atomicInteger.set(atomicInteger.incrementAndGet());
                int x = atomicReferenceInteger.get();
                atomicReferenceInteger.set(x + 1);
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
        System.out.println("Atomic integer = " + atomicInteger.get());
        System.out.println("Atomic reference integer = " + atomicInteger.get());
    }
}
