package org.jetlang.core;

/// <summary>
/// Controls thread lifecycle.

/// </summary>
public interface IThreadController {
    /// <summary>
    /// Starts thread execution.
    /// </summary>
    void Start();

    /// <summary>
    /// Stops thread
    /// </summary>
    void Stop();

    /// <summary>
    /// Waits for thread to finish.
    /// </summary>
    void Join();
}
