package interruption;

import java.util.ArrayList;
import java.util.List;

public class InterrruptSleepingThread {
	public static void main(String[] args) throws InterruptedException {
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				int counter = 1;
				while(!Thread.currentThread().isInterrupted())
				{
					try {
						System.out.println(counter+" square = "+Math.pow(counter++, 2));
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						System.out.printf("Thread [%s] interrupted my main thread, isInterrupted:%s,%n",
								Thread.currentThread().getName(), Thread.currentThread().isInterrupted());
						Thread.currentThread().interrupt();
					}
				}
			}
		});
		
		
		
		t1.start();
		Thread.sleep(5000);
		System.out.println("main thread interrupting the worker thread");
		t1.interrupt();
		System.out.println(t1.isInterrupted());
		t1.join();
	}
}
