package com.gome.fup.mq.server.callback;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gome.fup.mq.common.handler.DecoderHandler;
import com.gome.fup.mq.common.handler.EncoderHandler;
import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.http.Response;

/**
 * 将消息回调给监听
 *
 * @author fupeng-ds
 */
public class CallBack {

	private static CallBack callBack = new CallBack();

	private ExecutorService executorService = Executors.newFixedThreadPool(16);

	public void callback(final String host, final int port, final Request request) {
		executorService.submit(new Runnable() {
			
			@Override
			public void run() {
				EventLoopGroup group = new NioEventLoopGroup();
				try {
					Bootstrap bootstrap = new Bootstrap();
					bootstrap.group(group).channel(NioSocketChannel.class)
							.handler(new ChannelInitializer<SocketChannel>() {

								@Override
								protected void initChannel(SocketChannel ch)
										throws Exception {
									ch.pipeline()
											.addLast(new EncoderHandler())
											.addLast(new DecoderHandler(Response.class));
								}
							}).option(ChannelOption.SO_KEEPALIVE, true);
					ChannelFuture future = bootstrap.connect(host, port).sync();
					future.channel().writeAndFlush(request).sync();
					future.channel().closeFuture().sync();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					group.shutdownGracefully();
				}
			}
		});
	}

	public static CallBack getCallBack() {
		return callBack;
	}

	private CallBack() {
	}
}
