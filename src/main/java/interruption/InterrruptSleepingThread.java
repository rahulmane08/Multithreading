package interruption;

public class InterrruptSleepingThread {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            int counter = 1;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println(counter + " square = " + Math.pow(counter++, 2));
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    System.out.printf("Thread [%s] interrupted my main thread, isInterrupted:%s,%n",
                            Thread.currentThread().getName(), Thread.currentThread().isInterrupted());
                    Thread.currentThread().interrupt();
                }
            }
        });


        t1.start();
        Thread.sleep(2000);
        System.out.println("main thread interrupting the worker thread");
        t1.interrupt();
        System.out.println(t1.isInterrupted());
        t1.join();
    }
}
