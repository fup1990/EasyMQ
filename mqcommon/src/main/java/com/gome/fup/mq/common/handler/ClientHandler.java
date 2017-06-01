package com.gome.fup.mq.common.handler;

import com.gome.fup.mq.common.http.Response;
import com.gome.fup.mq.common.util.ResponseUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.listener.MessageReceiver;
import com.gome.fup.mq.common.model.Message;
import com.gome.fup.mq.common.util.Constant;

/**
 * 执行消息监听
 *
 * @author fupeng-ds
 */
public class ClientHandler extends SimpleChannelInboundHandler<Request> {
	
	private final Logger logger = Logger.getLogger(this.getClass());

	private ApplicationContext applicationContext;
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Request request)
			throws Exception {
		Response response = null;
		if(request.getType() == Constant.REQUEST_TYPE_CALLBACK) {
			if(request.getMsg() instanceof String) {
				String className = request.getListenerName();
				String data = request.getMsg();
				Map<String, MessageReceiver> map = applicationContext.getBeansOfType(MessageReceiver.class);
				for(Map.Entry<String, MessageReceiver> entry : map.entrySet()) {
					if(className.equals(entry.getValue().getClass().getName())) {
						MessageReceiver receiver = entry.getValue();
						logger.debug("消费端接收到消息，并执行。");
						receiver.onMessage(new Message(data));
						response = ResponseUtil.success("message received!!", request.getGroupName());
						break;
					}
				}
			}
		}
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	public ClientHandler(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
