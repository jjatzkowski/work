/*
 * Copyright (c) 2021. Michael Wolff
 */

package com.lupus.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;


/**
 * A JMS message sender.
 *
 * Each instance of JMS message sender has/requires a broker URL and a Queue nanem.
 */
public class MessageSender extends JMSClient {
    private static Log LOGGER = LogFactory.getLog(MessageSender.class);

    private final String queueName;

    /**
     * Creates a {@link MessageSender} with specified broker and target queue.
     * @param brokerURL
     *      The borker URL
     * @param  queueName
     *      Name of the JMS-Queue to which this sender should deliver it's messages.
     */
    public MessageSender(String brokerURL, String queueName) {
        super(brokerURL);
        this.queueName = queueName;
    }

    /**
     * Sends a JMS {@link TextMessage Message}.
     *
     * @param text
     *      Text to be sent.
     * @param msgGroup
     *      Optional message group (for partitioning)
     * @param seqNo
     *      A sequence number, which allows to check if ordering is as expected/required.
     * @throws JMSException
     */
    public void sendMessage(String text, String msgGroup, int seqNo) throws JMSException {
        final Connection connection = getConnection();
        long current = 0;

        if (LOGGER.isInfoEnabled()) {
            current = System.currentTimeMillis();
            LOGGER.info(">>> MessageSender.sendMessage(" + text + "," + msgGroup + ')');
        }

        try  {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            try {
                Queue queue = session.createQueue(queueName);
                TextMessage message = session.createTextMessage(text);
                MessageProducer producer = session.createProducer(queue);

                if (msgGroup != null) {
                    message.setStringProperty(JMSXGROUP_ID_PROP, msgGroup);
                    message.setIntProperty(JMSGROUP_SEQ_PROP, seqNo);
                }
                
                producer.send(message);
            } finally {
                session.close();
            }
        } finally {
            connection.close();

            if (LOGGER.isInfoEnabled()) {
                long duration = System.currentTimeMillis() - current;
                LOGGER.info(">>> MessageSender.sendMessage(). duration: " + duration + "ms");
            }
        }
    }
}
