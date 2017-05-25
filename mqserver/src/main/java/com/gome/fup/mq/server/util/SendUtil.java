package com.gome.fup.mq.server.util;

import com.gome.fup.mq.common.exception.NoServerAddrException;
import com.gome.fup.mq.common.http.Request;
import com.gome.fup.mq.common.model.Listener;
import com.gome.fup.mq.common.util.AddressUtil;
import com.gome.fup.mq.common.util.Constant;
import com.gome.fup.mq.server.callback.CallBack;
import com.gome.fup.mq.server.queue.Queue;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by fupeng-ds on 2017/5/25.
 */
public class SendUtil {

    private static Logger logger = Logger.getLogger(SendUtil.class);

    public static void sendMsgToListener(Queue<String> queue, List<Listener> list) {
        try {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String msg = queue.take();
                if(list != null && list.size() != 0) {
                    for (Listener listener : list) {
                        logger.debug("将消息发送给消费者。");
                        try {
                            sendMsg(listener, msg);
                        } catch (NoServerAddrException e) {
                            e.printStackTrace();
                            logger.error(e.getMessage());
                        }
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
