package com.gome.fup.mq.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import com.gome.fup.mq.common.http.Response;

/**
 * 
 *
 * @author fupeng-ds
 */
public class ResponseHandler extends SimpleChannelInboundHandler<Response>{

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Response response)
			throws Exception {
		
	}

}
