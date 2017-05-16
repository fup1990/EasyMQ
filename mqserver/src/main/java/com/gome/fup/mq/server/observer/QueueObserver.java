package com.gome.fup.mq.server.observer;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.model.Listener;
import com.gome.fup.mq.common.util.AddressUtil;
import com.gome.fup.mq.common.util.Cache;
import com.gome.fup.mq.common.util.Constant;
import com.gome.fup.mq.server.callback.CallBack;
import com.gome.fup.mq.server.queue.Queue;

/**
 * 从队列中取出消息，并发送给监听者
 *
 * @author fupeng-ds
 */
public class QueueObserver implements Observer {
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {
		Queue<String> queue = (Queue<String>)o;
		try {
			String groupName = (String) arg;
			Collection<Listener> collection = (Collection<Listener>) Cache.getCache().get(groupName);
			String msg = queue.take();
			if(collection != null && collection.size() != 0) {
				for (Listener listener : collection) {
					logger.debug("将消息发送给消费者。");
					sendMsgToListener(listener, msg);
					break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void sendMsgToListener(Listener listener, String msg) {
		String[] arr = AddressUtil.getServerAddr(listener.getAddr());
		String host = arr[0];
		int port = Integer.parseInt(arr[1]);
		Request request = new Request();
		request.setMsg(listener.getName() + ":" + msg);
		request.setType(Constant.REQUEST_TYPE_CALLBACK);
		CallBack.callback(host, port, request);
	}

	
}
