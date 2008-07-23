package com.jetlang.channels;

/**
 * Created by IntelliJ IDEA.
* User: mrettig
* Date: Jul 22, 2008
* Time: 2:57:28 PM
* To change this template use File | Settings | File Templates.
*/ /// <summary>
/// Channel publishing interface.
/// </summary>
/// <typeparam name="T"></typeparam>
public interface IChannelPublisher<T>
{
    /// <summary>
    /// Publish a message to all subscribers. Returns true if any subscribers are registered.
    /// </summary>
    /// <param name="msg"></param>
    /// <returns></returns>
    boolean Publish(T msg);
}
