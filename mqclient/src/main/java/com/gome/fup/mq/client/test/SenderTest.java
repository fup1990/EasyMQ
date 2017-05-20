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
		for(int i = 0; i < 1000; i++) {
			final int no = i;
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
