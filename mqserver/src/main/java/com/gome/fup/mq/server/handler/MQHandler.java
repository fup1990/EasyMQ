package com.gome.fup.mq.server.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	protected synchronized void channelRead0(ChannelHandlerContext ctx,
			Request request) throws Exception {
		if (request.getType().intValue() == Constant.REQUEST_TYPE_MSG) {
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
			Response response = new Response();
			response.setData("mq already recieved message!!");
			response.setGroupName(request.getGroupName());
			response.setStatus(Constant.SECCUESS);
			ctx.writeAndFlush(response)
					.addListener(ChannelFutureListener.CLOSE);
		}
		if (request.getType().intValue() == Constant.REQUEST_TYPE_LISTENER) {
			String msg = request.getMsg();
			// byte[] bytes = msg.getBytes("UTF-8");
			byte[] bytes = Base64.decode(msg);
			Map<String, List<Listener>> multimap = KryoUtil.byteToObj(bytes,
					HashMap.class);
			/*
			 * Multiset<String> keys = multimap.keys(); for (String groupName :
			 * keys) { Collection<Listener> collection =
			 * multimap.get(groupName); cache.set(groupName, collection); }
			 */
			for (Map.Entry<String, List<Listener>> entry : multimap.entrySet()) {
				Cache.getCache().set(entry.getKey(), entry.getValue());
			}
			logger.debug("MQ服务器接收到消息消费者的监听记录。");

			Response response = new Response();
			response.setData("Listener already Registed in server cache!!");
			response.setGroupName(request.getGroupName());
			response.setStatus(Constant.SECCUESS);
			ctx.writeAndFlush(response)
					.addListener(ChannelFutureListener.CLOSE);
		}
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
