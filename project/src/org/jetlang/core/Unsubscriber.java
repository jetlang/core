package org.jetlang.core;

/**
 * User: mrettig
 * Date: Jul 22, 2008
 * Time: 2:59:02 PM
 */
public class Unsubscriber {
    private Runnable unsub;

    public Unsubscriber(Runnable unsub) {

        this.unsub = unsub;
    }

    /// <summary>
    /// unsubscribe.
    /// </summary>
    public void unsubscribe() {
        this.unsub.run();
    }
}
