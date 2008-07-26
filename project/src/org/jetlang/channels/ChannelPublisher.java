package org.jetlang.channels;

/**
 * User: mrettig
 * Date: Jul 22, 2008
 * Time: 2:57:28 PM
 */
public interface ChannelPublisher<T> {
    /// <summary>
    /// Publish a message to all subscribers. Returns true if any subscribers are registered.
    /// </summary>
    /// <param name="msg"></param>
    /// <returns></returns>
    boolean publish(T msg);
}
