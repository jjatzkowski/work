/*
 * (c) 2021 M.Wolff
 */
package com.lupus.jms;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import java.util.Objects;
import java.util.Random;

public abstract class JMSClient {
    final private String brokerURL;
    final Random rnd = new Random();

    // Final constants.
    public static final String JMSXGROUP_ID_PROP                 = "JMSXGroupID";
    public static final String JMSXGROUP_SEQ_PROP                = "JMSXGroupSeq";
    public static final String JMSXGROUP_FIRST_FOR_CONSUMER_PROP = "JMSXGroupFirstForConsumer";
    public static final String JMSGROUP_SEQ_PROP                 = "GROUP-SEQ";
    public static final String JMS_QUEUE_NAME                    = "lupus";
    public static final String JMS_BROKER_URL                    = "tcp://localhost:61616";

    private ActiveMQConnectionFactory connectionFactory;

    public JMSClient(String brokerURL) {
        Objects.requireNonNull(brokerURL);

        this.brokerURL = brokerURL;
        connectionFactory = new ActiveMQConnectionFactory(brokerURL);
    }

    /**
     * Creates a new {@link Connection} and starts it.
     * @return
     *      Stared {@code Connection}
     * @see Connection#start()
     * @throws JMSException
     */
    protected final Connection getConnection() throws JMSException {
        Connection connection = connectionFactory.createConnection();
        connection.start();

        return connection;
    }
}
