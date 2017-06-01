package com.gome.fup.mq.server.handler;

import com.gome.fup.mq.common.model.Listener;
import com.gome.fup.mq.common.model.ListenerInCache;
import com.gome.fup.mq.common.util.Cache;
import com.gome.fup.mq.common.util.Constant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by fupeng-ds on 2017/5/27.
 */
public class HeartServerHandler extends ChannelInboundHandlerAdapter{

    private final Logger logger = Logger.getLogger(this.getClass());

    private int loss_connect_times = 0;

    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                if (loss_connect_times > 3) {
                    logger.info("心跳检查失败，关闭连接!");
                    ctx.channel().close();
                    //把断开连接的监听从缓存中删除
                    delListenerInCache(getHost(ctx.channel().remoteAddress().toString()));
                }
                loss_connect_times++;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            logger.info(ctx.channel().remoteAddress() + "客户端关闭了连接");
            ctx.channel().close();
            delListenerInCache(getHost(ctx.channel().remoteAddress().toString()));
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    private String getHost(String url) {
        String substring = url.substring(1);
        if (null == substring || "".equals(substring)) {
            return "";
        }
        String[] split = substring.split(":");
        if (null == split || split.length != 2) {
            return "";
        }
        return split[0];
    }

    private void delListenerInCache(String host) {
        /*if (Cache.getCache().hasKey(Constant.LISTENER_IN_CACHE)) {
            ListenerInCache listenerInCache = (ListenerInCache) Cache.getCache().get(Constant.LISTENER_IN_CACHE);
            Map<String, List<Listener>> map = listenerInCache.getMap();
            for (Map.Entry<String, List<Listener>> entry : map.entrySet()) {
                List<Listener> listeners = entry.getValue();
                Iterator<Listener> iterator = listeners.iterator();
                while (iterator.hasNext()) {
                    Listener listener = iterator.next();
                    if (listener.getAddr().contains(host)) {
                        iterator.remove();
                    }
                }
                map.put(entry.getKey(),listeners);
            }
        }*/
    }
}
