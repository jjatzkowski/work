/*
 * (c) 2021 M.Wolff
 */
package com.lupus.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;
import java.util.Enumeration;
import java.util.Objects;
import java.util.function.Consumer;

public class MessageReceiver extends JMSClient implements  Runnable {
    private static final Log LOGGER = LogFactory.getLog(MessageReceiver.class);
    private final String queueName;
    private final boolean doExit;
    private Connection connection = null;
    private Session session = null;
    private Destination destination = null;
    private MessageConsumer consumer = null;
    private MessageProducer producer = null;
    private final Consumer<Message> messageHandler;

    public MessageReceiver(String brokerURL, String queueName, boolean exitOnTimeout, Consumer<Message> msgConsumer) throws JMSException {
        super(brokerURL);

        Objects.requireNonNull(queueName);
        Objects.requireNonNull(msgConsumer);

        this.queueName = queueName;
        this.messageHandler = msgConsumer;
        doExit = exitOnTimeout;

        try {
            init();
        } catch (Exception e) {
            destroy();
            throw e;
        }
    }

    public void run() {
        long timeOut = 10000L;
        long cnt = 0;

        try {
            while (true) {
                Message message = consumer.receive(timeOut);

                try {
                    if (message != null) {
                        messageHandler.accept(message);
                        session.commit();
                    } else {
                        LOGGER.warn("Timed out - no message received.");
                        if (doExit) {
                            return; // Finish thread/message receive loop
                        }
                    }
                } catch (Exception e) {
                    LOGGER.fatal("Receiving or handling message failed.", e);
                    session.rollback();
                } finally {
                    if (cnt++ % 10 == 0) {
                        consumer = recreateConsumer(consumer);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.fatal("Error in message processing", e);
            try {
                destroy();
            } catch (Exception _e) {
                // Intentionally left blank
            }
        }
    }

    private MessageConsumer recreateConsumer(MessageConsumer oldConsumer) throws JMSException {
        oldConsumer.close();
        return session.createConsumer(destination);
    }

    private void closeMessageGroup(String messageGroup) throws JMSException {
        LOGGER.info(String.format("[%s] - Closing group: %s" ,Thread.currentThread().getName() ,messageGroup));
        TextMessage msg = session.createTextMessage("Closing group: " + messageGroup);
        msg.setStringProperty(JMSXGROUP_ID_PROP, messageGroup);
        msg.setIntProperty(JMSXGROUP_SEQ_PROP, -1);

        producer.send(msg);
    }

    /**
     * Sets up connection, session and destination
     * @throws JMSException
     */
    private void init() throws JMSException {
        connection = getConnection();
        session = connection.createSession(true, Session.SESSION_TRANSACTED);
        destination = session.createQueue(queueName);
        consumer = session.createConsumer(destination);
        producer = session.createProducer(destination);
    }

    /**
     * Safely closes {@link Session} and {@link Connection}.
     *
     * @throws JMSException
     */
    private void destroy() throws JMSException {
        if (consumer != null) try {
            consumer.close();
        } catch (Exception e) {
        }
        if (producer != null) try {
            producer.close();
        } catch (Exception e) {
        }
        if (session != null) try {
            session.close();
        } catch (Exception e) {
        }
        if (connection != null) try {
            connection.close();
        } catch (Exception e) {
        }
    }

    /**
     *
     * @param message
     * @return
     * @throws JMSException
     */
    private String messageToString(Message message) throws JMSException {
        StringBuilder buf = new StringBuilder();

        buf.append("Message {\n");

        if (message != null) {
            buf.append(messagePropertiesToString(message));

            if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;

                buf.append(txtMsg.getText());
            }
        }

        buf.append("}\n");

        return buf.toString();
    }

    /**
     *
     * @param message
     * @return
     * @throws JMSException
     */
    private String messagePropertiesToString(Message message) throws JMSException {
        StringBuilder buf = new StringBuilder();

        if (message != null) {
            buf.append("   props: {\n");

            Enumeration<String> _names = message.getPropertyNames();
            while (_names.hasMoreElements()) {
                String name = _names.nextElement();
                Object prop = message.getObjectProperty(name);

                buf.append(String.format("      %s: \"%s\"\n", name, String.valueOf(prop)));
            }
            buf.append("   }\n");
        }

        return buf.toString();
    }

    /**
     *
     * @param message
     * @return
     */
    private String getMessageGroup(Message message) {
        String _groupId = null;
        try {
            _groupId = message.getStringProperty(JMSXGROUP_ID_PROP);
        } catch (JMSException e) {
            // Intentionally left blank
        }

        return _groupId;
    }
}
