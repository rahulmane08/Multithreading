package design.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import design.executors.MyThreadPoolExecutor.MyFutureTask;

public class MyThreadPoolExecutorTest {

    public static void main(String[] args) {
        MyThreadPoolExecutor executor = new MyThreadPoolExecutor(5);
        List<MyFutureTask> futureTasks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Callable<Double> callable = () -> {
                Thread.sleep(3*1000);
                return Math.random();
            };
            futureTasks.add(executor.submit(callable));
        }

        for (MyFutureTask f : futureTasks) {
            try {
                System.out.println(f.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
