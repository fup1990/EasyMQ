package com.gome.fup.mq.client.test;

import com.gome.fup.mq.sender.MessageSender;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 *
 * @author fupeng-ds
 */
public class SenderTest {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        final MessageSender messageSender = (MessageSender) applicationContext.getBean("messageSender");
        for(int i = 0; i < 1000; i++) {
            final int no = i;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    messageSender.send("group1", "这是第" + no + "条测试消息");
                }
            });
            thread.start();
        }
    }
}
