package com.gome.fup.mq.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

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
	protected synchronized void channelRead0(ChannelHandlerContext ctx, Request request)
			throws Exception {
		if(request.getType() == Constant.REQUEST_TYPE_CALLBACK) {
			Object msg = request.getMsg();
			if(msg instanceof String) {				
				String[] split = ((String)msg).split(":");
				if(split != null && split.length == 2) {				
					String className = split[0];
					String data = split[1];
					Map<String, MessageReceiver> map = applicationContext.getBeansOfType(MessageReceiver.class);
					for(Map.Entry<String, MessageReceiver> entry : map.entrySet()) {
						if(className.equals(entry.getValue().getClass().getName())) {
							MessageReceiver receiver = entry.getValue();
							logger.debug("消费端接收到消息，并执行。");
							receiver.onMessage(new Message(data));
							break;
						}
					}
				}
			}
		}
	}

	public ClientHandler(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

}
