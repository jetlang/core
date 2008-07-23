package com.jetlang.core;

/**
 * Created by IntelliJ IDEA.
* User: mrettig
* Date: Jul 22, 2008
* Time: 2:58:02 PM
* To change this template use File | Settings | File Templates.
*/ /// <summary>
/// Methods for schedule events that will be executed in the future.
/// </summary>
public interface ICommandTimer
{
    /// <summary>
    /// Schedules an event to be executes once.
    /// </summary>
    /// <param name="command"></param>
    /// <param name="firstIntervalInMs"></param>
    /// <returns>a controller to cancel the event.</returns>
    //ITimerControl Schedule(Runnable command, long firstIntervalInMs);

    /// <summary>
    /// Schedule an event on a recurring interval.
    /// </summary>
    /// <param name="command"></param>
    /// <param name="firstIntervalInMs"></param>
    /// <param name="regularIntervalInMs"></param>
    /// <returns>controller to cancel timer.</returns>
    //ITimerControl ScheduleOnInterval(Runnable command, long firstIntervalInMs, long regularIntervalInMs);
}
