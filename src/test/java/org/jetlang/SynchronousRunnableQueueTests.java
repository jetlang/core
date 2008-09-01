package org.jetlang;

import org.jetlang.core.SynchronousDisposingExecutor;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * User: mrettig
 * Date: Jul 27, 2008
 * Time: 1:03:34 PM
 */
public class SynchronousRunnableQueueTests {

    @Test
    public void execution() {
        SynchronousDisposingExecutor queue = new SynchronousDisposingExecutor();
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
