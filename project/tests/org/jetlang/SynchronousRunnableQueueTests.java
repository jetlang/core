package org.jetlang;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.jetlang.core.SynchronousRunnableQueue;

/**
 * User: mrettig
 * Date: Jul 27, 2008
 * Time: 1:03:34 PM
 */
public class SynchronousRunnableQueueTests {

    @Test
    public void execution() {
        SynchronousRunnableQueue queue = new SynchronousRunnableQueue();
        final boolean[] executed = new boolean[1];
        Runnable run = new Runnable() {

            public void run() {
                executed[0] = true;
            }
        };
        queue.execute(run);
        assertTrue(executed[0]);
    }
}
