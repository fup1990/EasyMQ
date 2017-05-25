package com.gome.fup.mq.server.handler;

import com.gome.fup.mq.common.util.ResponseUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.log4j.Logger;

import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.http.Response;
import com.gome.fup.mq.common.model.Listener;
import com.gome.fup.mq.common.util.Cache;
import com.gome.fup.mq.common.util.Constant;
import com.gome.fup.mq.common.util.KryoUtil;
import com.gome.fup.mq.server.observer.QueueObserver;
import com.gome.fup.mq.server.queue.Queue;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

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
	protected void channelRead0(ChannelHandlerContext ctx,
			Request request) throws Exception {
		Response response = null;
		if (request.getType() == Constant.REQUEST_TYPE_MSG) {	//接收到消息
			response = putRequestInQueue(ctx, request);
		}
		if (request.getType() == Constant.REQUEST_TYPE_LISTENER) {	//接收到消费监听信息
			response = cacheListener(ctx, request);
		}
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private Response putRequestInQueue(ChannelHandlerContext ctx, Request request) throws Exception{
		String groupName = request.getGroupName();
		Queue<String> queue = cacheQueue.get(groupName);
		if (queue == null) {
			queue = new Queue<String>();
			queue.setGroupName(groupName);
			queue.addObserver(new QueueObserver());
			cacheQueue.put(groupName, queue);
		}

		logger.debug("MQ服务器接收到消息，并将消息存入队列中。");
		queue.put(request.getMsg());

		return ResponseUtil.success("mq already recieved message!!", request.getGroupName());
	}

	private Response cacheListener(ChannelHandlerContext ctx, Request request) throws Exception {
		String msg = request.getMsg();
		// byte[] bytes = msg.getBytes("UTF-8");
		byte[] bytes = Base64.decode(msg);
		Map<String, List<Listener>> multimap = KryoUtil.byteToObj(bytes, HashMap.class);
			/*
			 * Multiset<String> keys = multimap.keys(); for (String groupName :
			 * keys) { Collection<Listener> collection =
			 * multimap.get(groupName); cache.set(groupName, collection); }
			 */
		for (Map.Entry<String, List<Listener>> entry : multimap.entrySet()) {
			Cache.getCache().set(entry.getKey(), entry.getValue());
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
