package org.jetlang.core;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Component that can schedule events to execute in the future. Similar to the {@link ScheduledExecutorService}
 */
public interface Scheduler extends Disposable {

    /**
     * Creates and executes a one-shot action that becomes enabled  after the given delay.
     *
     * @param command the task to execute
     * @param delay   the time from now to delay execution
     * @param unit    the time unit of the delay parameter
     * @return a Disposable that can be used to cancel execution
     */
    Disposable schedule(Runnable command, long delay, TimeUnit unit);

    /**
     * Creates and executes a periodic action that becomes enabled first
     * after the given initial delay, and subsequently with the
     * given delay between the termination of one execution and the
     * commencement of the next.  If any execution of the task
     * encounters an exception, subsequent executions are suppressed.
     * Otherwise, the task will only terminate via cancellation or
     * termination of the scheduler.
     *
     * @param command      the task to execute
     * @param initialDelay the time to delay first execution
     * @param delay        the delay between the termination of one
     *                     execution and the commencement of the next
     * @param unit         the time unit of the initialDelay and delay parameters
     * @return a Disposable that can be used to cancel execution
     */
    Disposable scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);
}
