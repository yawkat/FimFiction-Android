package at.yawk.fimfiction.android;

import android.util.Log;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Jonas Konrad (yawkat)
 */
public class TaskManager {
    private final Executor executor =
            new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    private final Collection<Task> tasks = Collections.newSetFromMap(new WeakHashMap<Task, Boolean>());

    public void execute(Helper context, Runnable task) {
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
                    Log.d(Constants.TAG, "Removing now invalid Task from task queue (" + tasks.size() + " remaining)");
                    itr.remove();
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Failed while interrupting scheduled tasks", e);
        }
    }
}

class Task implements Runnable {
    private final Helper owner;
    private final Runnable action;
    private Thread runningThread = null;

    Task(Helper owner, Runnable action) {
        this.owner = owner;
        this.action = action;
    }

    public boolean isValid() { return owner.enabled(); }

    @Override
    public synchronized void run() {
        if (isValid()) {
            runningThread = Thread.currentThread();
            try {
                action.run();
            } catch (Throwable t) {
                Log.e(Constants.TAG, "Failed to execute task", t);
            } finally {
                Thread.interrupted();
                runningThread = null;
            }
        }
    }

    public void checkForInterrupt() {
        if (!isValid()) {
            if (runningThread != null) { runningThread.interrupt(); }
        }
    }
}
