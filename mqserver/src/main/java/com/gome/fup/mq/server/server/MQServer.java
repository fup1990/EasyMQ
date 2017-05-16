package com.gome.fup.mq.server.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.gome.fup.mq.common.handler.DecoderHandler;
import com.gome.fup.mq.common.handler.EncoderHandler;
import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.util.AddressUtil;
import com.gome.fup.mq.server.handler.MQHandler;
import com.gome.fup.mq.server.queue.Queue;

/**
 * 
 *
 * @author fupeng-ds
 * @param <E>
 */
public class MQServer implements InitializingBean {

	private final Logger logger = Logger.getLogger(this.getClass());

	private String serverAddr;

	private Map<String, Queue<String>> cacheQueue = new ConcurrentHashMap<String, Queue<String>>();
	
	public void afterPropertiesSet() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap
					.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel socketChannel)
								throws Exception {
							socketChannel.pipeline()
									.addLast(new DecoderHandler(Request.class))
									.addLast(new EncoderHandler())
									.addLast(new MQHandler(cacheQueue));
						}
					}).option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);
			String[] split = AddressUtil.getServerAddr(serverAddr);
			String host = split[0];
			Integer port = Integer.parseInt(split[1]);
			ChannelFuture future = bootstrap.bind(host, port).sync();
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	
}
