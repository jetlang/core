package org.jetlang.core;

/**
 * User: mrettig
 * Date: Jul 22, 2008
 * Time: 2:58:02 PM
 */ /// <summary>
/// Methods for schedule events that will be executed in the future.
/// </summary>
public interface RunnableScheduler extends Disposable {
    /// <summary>
    /// Schedules an event to be executes once.
    /// </summary>
    /// <param name="command"></param>
    /// <param name="firstIntervalInMs"></param>
    /// <returns>a controller to dispose the event.</returns>
    Disposable schedule(Runnable command, long firstIntervalInMs);

    /// <summary>
    /// Schedule an event on a recurring interval.
    /// </summary>
    /// <param name="command"></param>
    /// <param name="firstIntervalInMs"></param>
    /// <param name="regularIntervalInMs"></param>
    /// <returns>controller to dispose timer.</returns>
    Disposable scheduleOnInterval(Runnable command, long firstIntervalInMs, long regularIntervalInMs);

}
