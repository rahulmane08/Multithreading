package examples.classic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImmutabilityTest {
    public static void main(String[] args) throws InterruptedException {
        //construct immutable object
        final Immutable immutable = new Immutable(10, Arrays.asList("Rahul", "Mane"));

        Thread t1 = new Thread(() -> {
            int x = immutable.getValue();
            x++;
            immutable.getList().add("Thread1");
        });

        Thread t2 = new Thread(() -> {
            int x = immutable.getValue();
            x = x * 100;
            immutable.getList().add("Thread2");
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println(immutable);
    }

    private static class Immutable {
        private final Integer value;
        private final List<String> list;

        public Immutable(Integer value, List<String> list) {
            super();
            this.value = value;
            this.list = list;
        }

        public Integer getValue() {
            return value;
        }

        public List<String> getList() {
            List<String> clone = new ArrayList<>(list);
            return clone;
        }

        @Override
        public String toString() {
            return "Immutable [value=" + value + ", list=" + list + "]";
        }
    }
}
