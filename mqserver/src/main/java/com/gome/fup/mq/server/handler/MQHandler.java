package com.gome.fup.mq.server.handler;

import com.gome.fup.mq.common.util.*;
import com.gome.fup.mq.sender.QueueSender;
import com.lmax.disruptor.dsl.Disruptor;
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

	private Disruptor<QueueSender> disruptor;

	@SuppressWarnings("unchecked")
	@Override
	protected void channelRead0(ChannelHandlerContext ctx,
			Request request) throws Exception {
		Response response;
		if (request.getType() == Constant.REQUEST_TYPE_MSG) {	//接收到消息
			response = putRequestInQueue(request);
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}
	}

	private Response putRequestInQueue(Request request) throws Exception{
		String groupName = request.getGroupName();
		Queue<String> queue = cacheQueue.get(groupName);
		if (queue == null) {
			synchronized(this) {
				queue = new Queue<>(disruptor, groupName);
				cacheQueue.put(groupName, queue);
			}
		}
		logger.info("MQ服务器接收到消息，并将消息存入队列中。");
		queue.put(request.getMsg());
		return ResponseUtil.success("mq already recieved message!!", request.getGroupName());
	}

	public Map<String, Queue<String>> getCacheQueue() {
		return cacheQueue;
	}

	public void setCacheQueue(Map<String, Queue<String>> cacheQueue) {
		this.cacheQueue = cacheQueue;
	}

	public MQHandler(Map<String, Queue<String>> cacheQueue, Disruptor<QueueSender> disruptor) {
		super();
		this.cacheQueue = cacheQueue;
		this.disruptor = disruptor;
	}
}
