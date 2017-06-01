package com.gome.fup.mq.common.util;

import com.gome.fup.mq.common.http.Request;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


/**
 * Created by fupeng-ds on 2017/5/27.
 */
public class RequestUtil {

    public static Request buildRequst(Object data, int type) {
        Request request = new Request();
        try {
            byte[] bytes = KryoUtil.objToByte(data);
            request.setMsg(Base64.encode(bytes));
            request.setType(type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }
}
