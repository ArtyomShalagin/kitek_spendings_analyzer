package util;

import jep.Jep;
import jep.JepException;
import org.eclipse.jetty.util.BlockingArrayQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

public class JepHolder {
    private static Semaphore semaphore = new Semaphore(1);
    private static JepRunner runner = new JepRunner();
    private static Thread runnerThread;

    static {
        runnerThread = new Thread(runner);
        runnerThread.start();
    }

    private JepHolder() { }

    public static Object execute(Function<Jep, Object> task) throws ExecutionException, InterruptedException {
        return runner.addTask(task).get();
    }

    public static boolean jepInited() {
        return runner.jep != null;
    }

    public static boolean jepInitedOrWarn() {
        if (!jepInited()) {
            System.err.println("Warning: Jep is not initialized");
        }
        return jepInited();
    }

    private static class JepRunner implements Runnable {
        BlockingQueue<Pair<Function<Jep, Object>, CompletableFuture<Object>>> queue = new BlockingArrayQueue<>();
        Jep jep;

        @Override
        public void run() {
            try {
                jep = new Jep();
            } catch (JepException e) {
                System.err.println("Unable to initialize Jep: " + e.getMessage());
            }
            try {
                while (true) {
                    Pair<Function<Jep, Object>, CompletableFuture<Object>> entry = queue.take();
                    Object result = entry.first.apply(jep);
                    entry.second.complete(result);
                }
            } catch (InterruptedException e) {
                System.err.println("Jep thread interrupted!");
            }
        }

        CompletableFuture<Object> addTask(Function<Jep, Object> task) {
            CompletableFuture<Object> future = new CompletableFuture<>();
            queue.add(new Pair<>(task, future));
            return future;
        }
    }
}
