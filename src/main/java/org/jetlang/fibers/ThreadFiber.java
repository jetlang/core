package org.jetlang.fibers;

import org.jetlang.core.*;

import java.util.concurrent.TimeUnit;

/**
 * Fiber implementation backed by a dedicated thread for execution.
 */
public class ThreadFiber implements Fiber {

    private final Thread _thread;
    private final RunnableExecutor _queue;
    private final Scheduler _scheduler;

    /**
     * Create thread backed fiber
     *
     * @param queue          - target queue
     * @param threadName     - name to assign thread
     * @param isDaemonThread - true if daemon thread
     */
    public ThreadFiber(RunnableExecutor queue, String threadName, boolean isDaemonThread) {
        _queue = queue;
        Runnable runThread = new Runnable() {
            public void run() {
                runThread();
            }
        };
        _thread = createThread(threadName, runThread);
        _thread.setDaemon(isDaemonThread);
        _scheduler = new SchedulerImpl(queue);
    }

    private Thread createThread(String threadName, Runnable runThread) {
        if (threadName == null) {
            return new Thread(runThread);
        }
        return new Thread(runThread, threadName);
    }

    public ThreadFiber() {
        this(new RunnableExecutorImpl(), null, true);
    }

    public Thread getThread() {
        return _thread;
    }

    private void runThread() {
        _queue.run();
    }

    /**
     * Queue runnable for execution on this fiber.
     */
    public void execute(Runnable command) {
        _queue.execute(command);
    }

    public void add(Disposable runOnStop) {
        _queue.add(runOnStop);
    }

    public boolean remove(Disposable disposable) {
        return _queue.remove(disposable);
    }

    public int size() {
        return _queue.size();
    }

    public void dispose() {
        _scheduler.dispose();
        _queue.dispose();
    }

    /**
     * Start thread
     */
    public void start() {
        _thread.start();
    }

    /**
     * Wait for thread to complete
     */
    public void join() throws InterruptedException {
        _thread.join();
    }

    /**
     * Schedule a Runnable to execute in the future. Event will be executed on Fiber thread.
     */
    public Disposable schedule(Runnable command, long delay, TimeUnit unit) {
        return _scheduler.schedule(command, delay, unit);
    }

    /**
     * Schedule recurring event. Event will be fired on fiber thread.
     */
    public Disposable scheduleAtFixedRate(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return _scheduler.scheduleAtFixedRate(command, initialDelay, delay, unit);
    }

    public Disposable scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return _scheduler.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}
