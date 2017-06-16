package com.gome.fup.mq.server.zk;

import com.gome.fup.mq.common.model.Listener;
import com.gome.fup.mq.common.util.Cache;
import com.gome.fup.mq.common.util.KryoUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * Created by fupeng-ds on 2017/6/15.
 */
public class ZKRegister implements InitializingBean{

    private final static String PATH = "/EasyMQ";

    private String host;

    private ZooKeeper zooKeeper;

    private final static int SESSIONTIMEOUT = 5000;

    @Override
    public void afterPropertiesSet() throws Exception {
        zooKeeper = new ZooKeeper(host, SESSIONTIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    try {
                        zooKeeper.getChildren(PATH,new GroupWatcher());
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Stat exists = zooKeeper.exists(PATH, true);
        if (null == exists) {
            //创建持久化的节点
            zooKeeper.create(PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    private String getGroup(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    private class GroupWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                try {
                    //path='/EasyMQ'
                    List<String> groups = zooKeeper.getChildren(event.getPath(), this);
                    Cache cache = Cache.getCache();
                    for (String group :groups) {
                        //path='/EasyMQ/groupName
                        List<String> ips = zooKeeper.getChildren(event.getPath() + "/" + group, new IpWatcher());
                        cache.set(group, ips);
                        for (String ip : ips) {
                            //path='/EasyMQ/groupName/ip'
                            byte[] bytes = zooKeeper.getData(event.getPath() + "/" + group + "/" + ip, false, null);
                            Set set = KryoUtil.byteToObj(bytes, Set.class);
                            cache.set(ip,set);
                        }
                    }
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class IpWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                try {
                    List<String> ips = zooKeeper.getChildren(event.getPath(), this);
                    Cache cache = Cache.getCache();
                    cache.set(getGroup(event.getPath()),ips);
                    for (String ip : ips) {
                        //path='/EasyMQ/groupName/ip'
                        byte[] bytes = zooKeeper.getData(event.getPath() + "/" + ip, false, null);
                        Set set = KryoUtil.byteToObj(bytes, Set.class);
                        cache.set(ip,set);
                    }
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
