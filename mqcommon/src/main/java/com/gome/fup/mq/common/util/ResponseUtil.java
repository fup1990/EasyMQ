package com.gome.fup.mq.common.util;

import com.gome.fup.mq.common.http.Response;

/**
 * Created by fupeng-ds on 2017/5/23.
 */
public class ResponseUtil {

    public static Response success(String msg, String groupName) {
        Response response = new Response();
        response.setData(msg);
        response.setGroupName(groupName);
        response.setStatus(Constant.SUCCESS);
        return response;
    }
}
