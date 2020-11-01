package examples.threadlocal;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

public class ThreadLocalTest {
    public static void main(String[] args) throws InterruptedException {
        AuthContextHolder holder = new AuthContextHolder();
        AuthContext authContext1 = AuthContext.builder().userId("rahulmane").userName("rahul").build();
        AuthContext authContext2 = AuthContext.builder().userId("poojasharma").userName("pooja").build();

        Thread t1 = new Thread(() -> {
            holder.setContext(authContext1);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(3000);
                    System.out.printf("Thread [%s] , authContext: [%s] %n", Thread.currentThread().getName(), holder.getContext());
                } catch (InterruptedException e) {
                    System.out.printf("Thread [%s] interrupted %n", Thread.currentThread().getName());
                    Thread.currentThread().interrupt();
                }
            }
            System.out.printf("Thread [%s] finished %n", Thread.currentThread().getName());
        }, "worker-thread-1");

        Thread t2 = new Thread(() -> {
            holder.setContext(authContext2);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(3000);
                    System.out.printf("Thread [%s] , authContext: [%s] %n", Thread.currentThread().getName(), holder.getContext());
                } catch (InterruptedException e) {
                    System.out.printf("Thread [%s] interrupted %n", Thread.currentThread().getName());
                    Thread.currentThread().interrupt();
                }
            }
            System.out.printf("Thread [%s] finished %n", Thread.currentThread().getName());
        }, "worker-thread-2");

        t1.start();
        t2.start();
        Thread.sleep(15000);
        System.out.printf("Thread [%s] , authContext: [%s] %n", Thread.currentThread().getName(), holder.getContext());
        t1.interrupt();
        t2.interrupt();
        t1.join();
        t2.join();
        System.out.println("Exiting");
    }

    // Thread local holder
    private static class AuthContextHolder {
        private static ThreadLocal<AuthContext> context = new ThreadLocal<>();

        public static void clearContext() {
            context.set(null);
        }

        public static AuthContext getContext() {
            return context.get();
        }

        public static void setContext(AuthContext authContext) {
            context.set(authContext);
        }
    }

    @Data
    @Builder
    @ToString
    private static class AuthContext {
        private String userName;
        private String userId;
    }
}
