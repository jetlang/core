package com.jetlang.channels;

import com.jetlang.core.ICommandQueue;
import com.jetlang.core.Callback;

    /// <summary>
    /// Subscription for events on a channel.
    /// </summary>
    /// <typeparam name="T"></typeparam>
    public class ChannelSubscription<T>
    {
        private final Callback<T> _receiveMethod;
        private final ICommandQueue _targetQueue;

        /// <summary>
        /// Construct the subscription
        /// </summary>
        /// <param name="queue"></param>
        /// <param name="receiveMethod"></param>
        public ChannelSubscription(ICommandQueue queue, Callback<T> receiveMethod)
        {
            _receiveMethod = receiveMethod;
            _targetQueue = queue;
        }

        /// <summary>
        /// Receives the event and queues the execution on the target queue.
        /// </summary>
        /// <param name="msg"></param>
        protected void OnMessageOnProducerThread(final T msg)
        {
            Runnable asyncExec = new Runnable(){
                public void run()
                {
                    _receiveMethod.onMessage(msg);
                }
            };
            _targetQueue.queue(asyncExec);
        }
    }
