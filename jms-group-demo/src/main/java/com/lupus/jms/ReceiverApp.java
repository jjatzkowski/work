/*
 * (c) 2021 M.Wolff
 */
package com.lupus.jms;

import com.lupus.jms.thread.JMSReceiverThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Random;

import static com.lupus.jms.JMSClient.*;

public class ReceiverApp {
    private final static Log LOGGER = LogFactory.getLog("GroupingConsumers");
    private final static int NUM_THREADS = 48;
    private final static Random rnd = new Random();

    public static void main(String[] args) throws JMSException {
        JMSReceiverThreadFactory threadFactory = new JMSReceiverThreadFactory(JMS_QUEUE_NAME);

        for (int i = 0; i < NUM_THREADS; i++) {
            MessageReceiver receiver = new MessageReceiver(JMS_BROKER_URL, JMS_QUEUE_NAME, false, ReceiverApp::onMessage);
            Thread _thread = threadFactory.newThread(receiver);
            _thread.start();
            sleep(5000);
        }

        while(true) {
            sleep(10000);
        }
    }

    /**
     *
     * @param millis
     */
    protected static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     *
     * @param message
     */
    protected static void onMessage(Message message) {
        long _start = System.currentTimeMillis();
        String grpId = getSafelyMessageProperty(message, JMSXGROUP_ID_PROP);
        Integer seqNo = getSafelyMessageProperty(message, JMSClient.JMSGROUP_SEQ_PROP);
        String threadName = Thread.currentThread().getName();
        sleep(rnd.nextInt(2000) + 1);

        long _duration = System.currentTimeMillis() - _start;
        LOGGER.info(String.format("%s: %s(%d) - %dms", threadName, grpId != null ? grpId : "-", seqNo != null ? seqNo : "-", _duration));
    }

    /**
     *
     * @param message
     * @param name
     * @param <T>
     * @return
     */
    private static <T> T getSafelyMessageProperty(Message message, String name) {
        try {
            return (T) message.getObjectProperty(name);
        } catch (JMSException e) {
            return null;
        }
    }
}
