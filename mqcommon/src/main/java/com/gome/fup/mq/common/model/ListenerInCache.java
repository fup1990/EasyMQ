package com.gome.fup.mq.common.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fupeng-ds on 2017/5/27.
 */
public class ListenerInCache implements Serializable{

    private Map<String, List<Listener>> map = new ConcurrentHashMap<String, List<Listener>>();

    public Map<String, List<Listener>> getMap() {
        return map;
    }

    public void setMap(Map<String, List<Listener>> map) {
        this.map = map;
    }
}
