package com.gome.fup.mq.server.observer;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gome.fup.mq.common.exception.NoServerAddrException;
import com.gome.fup.mq.common.util.RandomUtil;
import com.gome.fup.mq.sender.QueueSender;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.log4j.Logger;

import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.model.Listener;
import com.gome.fup.mq.common.util.Cache;
import com.gome.fup.mq.common.util.Constant;
import com.gome.fup.mq.server.queue.Queue;

/**
 * 从队列中取出消息，并发送给监听者
 *
 * @author fupeng-ds
 */
public class QueueObserver implements Observer {
	
	private final Logger logger = Logger.getLogger(this.getClass());

	private Disruptor<QueueSender> disruptor;

	private RingBuffer<QueueSender> ringBuffer;

	private int size = 1<<10;

	private ExecutorService executorService = Executors.newFixedThreadPool(16);

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {
		Queue<String> queue = (Queue<String>)o;
		String groupName = (String) arg;
		sendMsgToListener(queue, groupName);
	}

	private void sendMsgToListener(Queue<String> queue, String groupName) {
		try {
			List<String> ips = (List<String>) Cache.getCache().get(groupName);
			int size = queue.size();
			for (int i = 0; i < size; i++) {
				String msg = queue.take();
				logger.debug("将消息发送给消费者。");
				sendMsg(getLisenter(ips), msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	private Listener getLisenter(List<String> ips) {
		int random = RandomUtil.random(ips.size());
		List<String> classNames = (List<String>) Cache.getCache().get(ips.get(random));
		return new Listener(classNames.get(random),ips.get(random));
	}

	private void sendMsg(Listener listener, String msg) throws NoServerAddrException {
		Request request = new Request();
		request.setListenerName(listener.getName());
		request.setMsg(msg);
		request.setType(Constant.REQUEST_TYPE_CALLBACK);
		//CallBack.getCallBack().callback(host, port, request);
		long next = ringBuffer.next();
		try {
			QueueSender queueSender = ringBuffer.get(next);
			queueSender.setRequest(request);
			queueSender.setServerAddr(listener.getAddr());
		} finally {
			ringBuffer.publish(next);
		}
	}

	public QueueObserver(Disruptor<QueueSender> disruptor) {
		this.disruptor = disruptor;
		ringBuffer = disruptor.start();
	}

	public Disruptor<QueueSender> getDisruptor() {
		return disruptor;
	}

	public void setDisruptor(Disruptor<QueueSender> disruptor) {
		this.disruptor = disruptor;
	}
}
