package com.gome.fup.mq.common.handler;

import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.util.Constant;
import com.gome.fup.mq.common.util.RequestUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by fupeng-ds on 2017/5/27.
 */
public class HeartClientHandler extends ChannelInboundHandlerAdapter{

    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
                Request request = RequestUtil.buildRequst("Heartbeat", Constant.REQUEST_TYPE_HEART);
                System.out.println(request.toString());
                ctx.writeAndFlush(request);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            logger.info("远程服务关闭了连接");
            ctx.channel().close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
