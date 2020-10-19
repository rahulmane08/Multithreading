package interruption;

public class InterruptWaitingThread {
	public static void main(String[] args) throws InterruptedException {
		Object lock = new Object();
		Runnable r = ()->{
			synchronized(lock)
			{
				System.out.printf("Thread [%s] isInterrupted:%s%n",
						Thread.currentThread().getName(),
						Thread.currentThread().isInterrupted());
				int counter = 1;
				while(counter<100)
					System.out.println(counter+" square = "+Math.pow(counter++, 2));
			}
		};
		Thread t = new Thread(r);
		synchronized (lock) {
			t.start();
			Thread.sleep(5*1000);
			t.interrupt();
			Thread.sleep(5*1000);
		}
		t.join();
		System.out.println("main thread exiting");
	}
}
