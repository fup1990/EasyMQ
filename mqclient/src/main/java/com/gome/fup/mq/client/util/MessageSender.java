package com.gome.fup.mq.client.util;

import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.util.Constant;
import com.gome.fup.mq.sender.QueueSender;

/**
 * 消息发送工具类
 *
 * @author fupeng-ds
 */
public class MessageSender {

	private static QueueSender queueSender;
	
	public static void send(String groupName, String data) {
		try {
			Request request = new Request();
			request.setGroupName(groupName);
			request.setMsg(data);
			request.setType(Constant.REQUEST_TYPE_MSG);
			queueSender.send(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static QueueSender getQueueSender() {
		return queueSender;
	}

	public static void setQueueSender(QueueSender queueSender) {
		MessageSender.queueSender = queueSender;
	}

	
}
