package com.lupus.jms;

import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static com.lupus.jms.JMSClient.JMS_BROKER_URL;
import static com.lupus.jms.JMSClient.JMS_QUEUE_NAME;


public class SenderApp {
    private static Random rnd = new Random();
    private static Map<String, Integer> grpMap = new HashMap<>();
    private final static int NUM_OF_MESSAGES = 10000;
    private final static int NUM_OF_GROUPS = 48;

    public static void main(String[] args) throws JMSException {
        MessageSender sender = new MessageSender(JMS_BROKER_URL, JMS_QUEUE_NAME);

        for (int i = 0; i < NUM_OF_MESSAGES; i++) {
            String group = createGroup(NUM_OF_GROUPS);
            grpMap.compute(group, (k,v) -> v == null ? 1 : v + 1);
            sender.sendMessage("<CIN/>", group, grpMap.get(group));
        }
    }

    /**
     *
     * @return
     */
    private static String createGroup(int max) {
        int val = rnd.nextInt(max) + 1;

        return "Group: " + String.valueOf(val);
    }

}
