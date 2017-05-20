package com.gome.fup.mq.client.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gome.fup.mq.client.util.MessageSender;

/**
 * 
 *
 * @author fupeng-ds
 */
public class SenderTest {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("classpath:spring.xml");
		MessageSender.send("group1", "这是一条测试消息");		
		MessageSender.send("group2", "这是第二条条测试消息");	
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					MessageSender.send("group1", "这是第" + no + "条测试消息");
				}
			});
			thread.start();
		}

	}
}
