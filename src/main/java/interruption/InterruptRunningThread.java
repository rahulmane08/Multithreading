package interruption;

/**
 * Here the worker thread needs to check for the interrupted flag as
 */
public class InterruptRunningThread {
    public static void main(String[] args) throws InterruptedException {
        Runnable r = () -> {
            int counter = 1;
            while (!Thread.currentThread().isInterrupted())
                System.out.println(" Counter = " + counter++);
			System.out.printf("Thread [%s] interrupted my main thread, hence finishing%n",
					Thread.currentThread().getName());
        };
        Thread t = new Thread(r, "worker");
        t.start();
        Thread.sleep(5 * 1000);
        t.interrupt();

		r = () -> {
			int counter = 1;
			while (true) {
				if (!Thread.currentThread().isInterrupted()) {
					System.out.println(" Counter = " + counter++);
				} else {
					System.out.printf("Thread [%s] interrupted my main thread, hence sleeping and resuming %n",
							Thread.currentThread().getName());
					try {
						/**
						 * commenting this will make the thread throw interrupted exception as the interrupte status is
						 * still set to true and calling sleep on such a thread throws InterruptedException. However if
						 * we wish to resume the thread we can clear the interrupted flag by Thread.interrupted();
						 */
						Thread.interrupted();
						Thread.sleep(2*1000);
					} catch (InterruptedException e) {
						System.out.printf("Thread [%s] is interrupted while sleeping and hence quitting%n",
								Thread.currentThread().getName());
						break;
					}
				}
			}
		};
		t = new Thread(r, "worker-1");
		t.start();
		Thread.sleep(5 * 1000);
		t.interrupt();
		t.join();
		System.out.println("main thread exiting");
	}
}
