package at.yawk.fimfiction.android;

import android.app.Application;
import android.util.Log;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Yawkat
 */
public class BrowserApp extends Application implements Constants {
    private final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    private final Collection<Task> tasks = Collections.newSetFromMap(new WeakHashMap<Task, Boolean>());

    public void execute(final Fimtivity context, final Runnable task) {
        Task taskElement = new Task(context, task);
        executor.execute(taskElement);
        tasks.add(taskElement);
    }

    public void interruptScheduler() {
        try {
            Iterator<Task> itr = tasks.iterator();
            while (itr.hasNext()) {
                Task task = itr.next();
                task.checkForInterrupt();
                if (!task.isValid()) {
                    Log.d(TAG, "Removing now invalid Task from task queue (" + tasks.size() + " remaining)");
                    itr.remove();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed while interrupting scheduled tasks", e);
        }
    }

    public static void runTask(Fimtivity context, Runnable task) {
        ((BrowserApp) context.getApplication()).execute(context, task);
    }

    private class Task implements Runnable {
        private final Fimtivity context;
        private final Runnable action;
        private Thread runningThread = null;

        private Task(final Fimtivity context, final Runnable action) {
            this.context = context;
            this.action = action;
        }

        private boolean isValid() {
            return context == null || !context.isShutDown();
        }

        @Override
        public synchronized void run() {
            if (isValid()) {
                runningThread = Thread.currentThread();
                try {
                    action.run();
                } catch (Throwable t) {
                    Log.e(TAG, "Failed to execute task", t);
                } finally {
                    Thread.interrupted();
                    runningThread = null;
                }
            }
        }

        public void checkForInterrupt() {
            Log.d(TAG, "Checking for interrupt: " + context + " " + action + " " + runningThread);
            if (!isValid()) {
                Log.d(TAG, "Interrupting scheduled runnable.");
                if (runningThread != null) {
                    runningThread.interrupt();
                }
            }
        }
    }
}
