package com.lupus.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;


/**
 *
 */
public class MessageSender extends JMSClient {
    private static Log LOGGER = LogFactory.getLog(MessageSender.class);

    private final String queueName;

    /**
     *
     * @param brokerURL
     */
    public MessageSender(String brokerURL, String queueName) {
        super(brokerURL);
        this.queueName = queueName;
    }

    /**
     *
     * @param text
     * @param msgGroup
     * @param seqNo
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
