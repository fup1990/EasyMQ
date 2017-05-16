package com.gome.fup.mq.register;

import java.util.List;
import java.util.Map;

import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.model.Listener;
import com.gome.fup.mq.common.util.Constant;
import com.gome.fup.mq.common.util.KryoUtil;
import com.gome.fup.mq.sender.QueueSender;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * 
 *
 * @author fupeng-ds
 */
public abstract class AbstractRegister {

	private QueueSender sender;

	protected void sendListenerToMQServer(Map<String, List<Listener>> multimap) {
		Request request = mapToRequest(multimap);
		sender.send(request);
	}
	
	private Request mapToRequest(Map<String, List<Listener>> multimap) {
		Request request = new Request();
		try {
			byte[] bytes = KryoUtil.objToByte(multimap);
			
			request.setMsg(Base64.encode(bytes));
			//request.setMsg(multimap);
			request.setType(Constant.REQUEST_TYPE_LISTENER);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}

	public QueueSender getSender() {
		return sender;
	}

	public void setSender(QueueSender sender) {
		this.sender = sender;
	}
	
	
}
