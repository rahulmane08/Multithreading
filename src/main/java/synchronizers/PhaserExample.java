package synchronizers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class PhaserExample {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(3);
        Phaser phaser = new Phaser();
        phaser.register(); // main registers, parties = 1

        for (int i = 0; i < 3; i++) {
            service.submit(new LongAction(1, phaser));
        } // parties = 4
        Thread.sleep(5 * 1000); // tasks wont start until main thread resumes.
        int phaseNumber = phaser.arriveAndAwaitAdvance();

        System.out.println("Finished phase: " + phaseNumber);


        phaser.arriveAndDeregister(); // main deregisters
    }

    private static class LongAction implements Runnable {
        private final int timeout;
        private final Phaser phaser;

        public LongAction(int timeout, Phaser phaser) {
            this.timeout = timeout;
            this.phaser = phaser;
            this.phaser.register();
        }

        @Override
        public void run() {
            try {
                String threadName = Thread.currentThread().getName();
                int phaseNumber = phaser.arriveAndAwaitAdvance();
                System.out.printf("Thread: [%s] starting to execute the task at phase: %d %n", threadName, phaseNumber);
                Thread.sleep(timeout * 1000);
                System.out.printf("Thread: [%s] finished executing the task at phase: %d %n", threadName, phaseNumber);
                phaser.arriveAndDeregister();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
