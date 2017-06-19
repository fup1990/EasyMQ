package com.gome.fup.mq.sender;

import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.util.Constant;

/**
 * 消息发送工具类
 *
 * @author fupeng-ds
 */
public class MessageSender extends AbstractSender{

	public void send(String groupName, String data) {
		try {
			Request request = new Request();
			request.setGroupName(groupName);
			request.setMsg(data);
			request.setType(Constant.REQUEST_TYPE_MSG);
			super.sendRequest(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void send(Request request) {
		super.sendRequest(request);
	}
}
