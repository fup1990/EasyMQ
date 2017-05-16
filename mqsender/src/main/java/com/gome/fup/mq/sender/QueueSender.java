package com.gome.fup.mq.sender;

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
import com.gome.fup.mq.common.util.AddressUtil;

/**
 * 
 *
 * @author fupeng-ds
 */
public class QueueSender {

	private ExecutorService executorService = Executors.newFixedThreadPool(10); 
	
	private String serverAddr;

	private Response response;

	// private final Object obj = new Object();

	public void send(final Request request) {
		executorService.submit(new Runnable() {
			
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
									// .addLast(QueueSender.this);
								}
							}).option(ChannelOption.SO_KEEPALIVE, true);
					String[] addr = AddressUtil.getServerAddr(serverAddr);
					String host = addr[0];
					Integer port = Integer.parseInt(addr[1]);
					// 链接服务器
					ChannelFuture future = bootstrap.connect(host, port).sync();
					// 将request对象写入outbundle处理后发出
					future.channel().writeAndFlush(request).sync();

					// synchronized (obj) {
					// 用线程等待的方式决定是否关闭连接
					// 其意义是：先在此阻塞，等待获取到服务端的返回后，被唤醒，从而关闭网络连接
					// obj.wait();
					// }

					// if (response != null) {
					// 服务器同步连接断开时,这句代码才会往下执行
					future.channel().closeFuture().sync();
					// }
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					group.shutdownGracefully();
				}
			}
		});
	}

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	/*@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Response response)
			throws Exception {
		this.response = response;
		synchronized (obj) {
			obj.notifyAll();
		}
	}*/

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

}
