package com.gome.fup.mq.server.handler;

import com.gome.fup.mq.common.util.*;
import com.gome.fup.mq.server.util.SendUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.log4j.Logger;

import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.http.Response;
import com.gome.fup.mq.common.model.Listener;
import com.gome.fup.mq.server.observer.QueueObserver;
import com.gome.fup.mq.server.queue.Queue;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * 
 *
 * @author fupeng-ds
 */
public class MQHandler extends SimpleChannelInboundHandler<Request> {

	private final Logger logger = Logger.getLogger(this.getClass());

	private Map<String, Queue<String>> cacheQueue;

	@SuppressWarnings("unchecked")
	@Override
	protected synchronized void channelRead0(ChannelHandlerContext ctx,
			Request request) throws Exception {
		Response response;
		if (request.getType() == Constant.REQUEST_TYPE_MSG) {	//接收到消息
			response = putRequestInQueue(request);
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}
		/*if (request.getType() == Constant.REQUEST_TYPE_LISTENER) {	//接收到消费监听信息
			cacheListener(request);
		}*/

	}

	private Response putRequestInQueue(Request request) throws Exception{
		String groupName = request.getGroupName();
		Queue<String> queue = cacheQueue.get(groupName);
		if (queue == null) {
			queue = new Queue<>(groupName);
			cacheQueue.put(groupName, queue);
		}

		logger.debug("MQ服务器接收到消息，并将消息存入队列中。");
		queue.put(request.getMsg());
		return ResponseUtil.success("mq already recieved message!!", request.getGroupName());
	}

	private Response cacheListener(Request request) throws Exception {
		String msg = request.getMsg();
		byte[] bytes = Base64.decode(msg);
		Map<String, List<Listener>> multimap = KryoUtil.byteToObj(bytes, HashMap.class);
		for (Map.Entry<String, List<Listener>> entry : multimap.entrySet()) {
			List<Listener> list;
			if (Cache.getCache().hasKey(entry.getKey())) {
				list = (List<Listener>) Cache.getCache().get(entry.getKey());
				list.addAll(entry.getValue());
			} else {
				list = entry.getValue();
			}
			Cache.getCache().set(entry.getKey(), list);
			//队列中已经有消息
			if (cacheQueue.keySet().contains(entry.getKey())) {
				//SendUtil.sendMsgToListener(cacheQueue.get(entry.getKey()), entry.getValue());
			}
		}
		logger.debug("MQ服务器接收到消息消费者的监听记录。");

		return ResponseUtil.success("Listener already Registed in server cache!!", request.getGroupName());
	}

	public Map<String, Queue<String>> getCacheQueue() {
		return cacheQueue;
	}

	public void setCacheQueue(Map<String, Queue<String>> cacheQueue) {
		this.cacheQueue = cacheQueue;
	}

	public MQHandler(Map<String, Queue<String>> cacheQueue) {
		super();
		this.cacheQueue = cacheQueue;
	}
}
