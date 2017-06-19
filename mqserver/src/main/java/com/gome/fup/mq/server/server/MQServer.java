package com.gome.fup.mq.server.server;

import com.gome.fup.mq.sender.QueueSender;
import com.gome.fup.mq.server.handler.HeartServerHandler;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.handler.timeout.IdleStateHandler;
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
 */
public class MQServer implements Runnable, InitializingBean {

	private final Logger logger = Logger.getLogger(this.getClass());

	private String serverAddr;

	private Map<String, Queue<String>> cacheQueue = new ConcurrentHashMap<String, Queue<String>>();

	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	private Disruptor<QueueSender> disruptor;

	private int size = 1<<10;

	private ExecutorService pool = Executors.newFixedThreadPool(16);

	public void afterPropertiesSet() throws Exception {
		executorService.submit(this);
		disruptor = new Disruptor<QueueSender>(new EventFactory<QueueSender>() {
			@Override
			public QueueSender newInstance() {
				return new QueueSender();
			}
		}, size, pool);
		disruptor.handleEventsWith(new EventHandler<QueueSender>() {
			@Override
			public void onEvent(QueueSender queueSender, long l, boolean b) throws Exception {
				queueSender.send();
			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				disruptor.shutdown();
				pool.shutdown();
			}
		}));
	}

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}


	@Override
	public void run() {
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
									//.addLast(new IdleStateHandler(5,0,0, TimeUnit.SECONDS))
									.addLast(new DecoderHandler(Request.class))
									.addLast(new EncoderHandler())
									.addLast(new MQHandler(cacheQueue, disruptor));
									//.addLast(new HeartServerHandler());
						}
					}).option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);
			String[] split = AddressUtil.getServerAddr(serverAddr);
			if (null == split) {
				return;
			}
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
			executorService.shutdown();
		}
	}


}
