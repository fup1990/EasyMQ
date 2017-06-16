package com.gome.fup.mq.server.util;

import com.gome.fup.mq.common.exception.NoServerAddrException;
import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.model.Listener;
import com.gome.fup.mq.common.util.AddressUtil;
import com.gome.fup.mq.common.util.Cache;
import com.gome.fup.mq.common.util.Constant;
import com.gome.fup.mq.common.util.RandomUtil;
import com.gome.fup.mq.server.callback.CallBack;
import com.gome.fup.mq.server.queue.Queue;
import org.apache.log4j.Logger;
import sun.misc.Cleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fupeng-ds on 2017/5/25.
 */
public class SendUtil {

    private static Logger logger = Logger.getLogger(SendUtil.class);

    public static void sendMsgToListener(Queue<String> queue, String groupName) {
        try {
            List<String> ips = (List<String>) Cache.getCache().get(groupName);
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String msg = queue.take();
                logger.debug("将消息发送给消费者。");

                sendMsg(getLisenter(ips), msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    private static Listener getLisenter(List<String> ips) {
        int random = RandomUtil.random(ips.size());
        Set<String> classNames = (Set<String>) Cache.getCache().get(ips.get(random));
        String[] objects = (String[]) classNames.toArray();
        return new Listener(objects[random],ips.get(random));
    }

    private static void sendMsg(Listener listener, String msg) throws NoServerAddrException {
        String[] arr = AddressUtil.getServerAddr(listener.getAddr());
        if (null == arr) throw new NoServerAddrException("服务地址未加载异常");
        String host = arr[0];
        int port = Integer.parseInt(arr[1]);
        Request request = new Request();
        request.setListenerName(listener.getName());
        request.setMsg(msg);
        request.setType(Constant.REQUEST_TYPE_CALLBACK);
        CallBack.getCallBack().callback(host, port, request);
    }

}
