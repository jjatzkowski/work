/*
 * (c) 2021 M.Wolff
 */
package com.lupus.jms.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class JMSReceiverThreadFactory implements ThreadFactory {
    private String queueName;
    private static AtomicInteger threadNo = new AtomicInteger();

    /**
     *
     * @param queue
     */
    public JMSReceiverThreadFactory(String queue) {
        queueName = queue;
    }

    /**
     *
     * @param r
     * @return
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread _thread = new Thread(r);

        _thread.setName(String.format("JMS-REC-%s-%d", queueName, threadNo.incrementAndGet()));
        _thread.setDaemon(false);
        return _thread;
    }
}
