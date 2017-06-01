package com.gome.fup.mq.register;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.gome.fup.mq.common.handler.ClientHandler;
import com.gome.fup.mq.common.handler.DecoderHandler;
import com.gome.fup.mq.common.handler.EncoderHandler;
import com.gome.fup.mq.common.handler.HeartClientHandler;
import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.model.Listener;
import com.gome.fup.mq.common.util.AddressUtil;
import com.gome.fup.mq.common.util.Constant;
import com.gome.fup.mq.common.util.KryoUtil;
import com.gome.fup.mq.common.util.RequestUtil;
import com.gome.fup.mq.sender.QueueSender;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 *
 * @author fupeng-ds
 */
public abstract class AbstractRegister implements ApplicationContextAware{

	private QueueSender sender;

	protected String localAddr;

	protected ApplicationContext applicationContext;

	protected void sendListenerToMQServer(Map<String, List<Listener>> multimap) {
		Request request = RequestUtil.buildRequst(multimap, Constant.REQUEST_TYPE_LISTENER);
		sender.send(request);
	}

	protected void startCliendServer() {
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
									.addLast(new ClientHandler(applicationContext));
						}
					}).option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);
			String[] split = AddressUtil.getServerAddr(localAddr);
			String host = split[0];
			Integer port = Integer.parseInt(split[1]);
			ChannelFuture future = bootstrap.bind(host, port).sync();
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	public QueueSender getSender() {
		return sender;
	}

	public void setSender(QueueSender sender) {
		this.sender = sender;
	}

	public String getLocalAddr() {
		return localAddr;
	}

	public void setLocalAddr(String localAddr) {
		this.localAddr = localAddr;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
