package com.gome.fup.mq.server.observer;

import java.util.*;

import com.gome.fup.mq.common.exception.NoServerAddrException;
import com.gome.fup.mq.common.util.RandomUtil;
import com.gome.fup.mq.server.util.SendUtil;
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
	public synchronized void update(Observable o, Object arg) {
		Queue<String> queue = (Queue<String>)o;
		String groupName = (String) arg;
		SendUtil.sendMsgToListener(queue, groupName);
	}
}
