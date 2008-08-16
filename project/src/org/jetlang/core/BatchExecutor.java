package org.jetlang.core;

import java.util.Collection;

/**
 * User: mrettig
 * Date: Aug 16, 2008
 * Time: 11:00:31 AM
 */
public interface BatchExecutor {

    void execute(Collection<Runnable> toExecute);
}
