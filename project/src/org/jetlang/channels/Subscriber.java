package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.core.DisposingExecutor;

/**
 * Interface for components that allow for message subscriptions
 *
 * @param <T> Type of messages a subscription will be for
 */
public interface Subscriber<T> {
    /**
     * Subscribe to receive messages produced by this subscriber
     *
     * @param executor {@link DisposingExecutor} to use for invoking the callback upon message receipt.
     * @param receive  {@link Callback} to invoke upon message receipt
     * @return {@link Disposable} that can be invoked to cancel this subscription
     */
    Disposable subscribe(DisposingExecutor executor, Callback<T> receive);
}