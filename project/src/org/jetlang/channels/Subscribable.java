package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.DisposingExecutor;

/**
 * User: mrettig
 * Date: Jul 27, 2008
 * Time: 3:55:14 PM
 */
public interface Subscribable<T> extends Callback<T> {

    DisposingExecutor getQueue();

}
