package com.gome.fup.mq.server.zk;

import com.gome.fup.mq.common.util.Cache;
import com.gome.fup.mq.common.util.KryoUtil;
import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fupeng-ds on 2017/6/15.
 */
public class ZKRegister implements InitializingBean{

    private final Logger logger = Logger.getLogger(this.getClass());

    private final static String PATH = "/EasyMQ";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private String host;

    private ZooKeeper zooKeeper;

    private Cache cache = Cache.getCache();

    private final static int SESSIONTIMEOUT = 5000;

    @Override
    public void afterPropertiesSet() throws Exception {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    zooKeeper = new ZooKeeper(host, SESSIONTIMEOUT, new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getType() == Event.EventType.NodeCreated) {
                                logger.info("NodeCreated");
                            }
                            if (event.getType() == Event.EventType.NodeDeleted) {
                                logger.info("NodeDeleted");
                            }
                            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                                logger.info("NodeChildrenChanged");
                            }
                            if (event.getType() == Event.EventType.NodeDataChanged) {
                                logger.info("NodeDataChanged");
                            }
                        }
                    });
                    while (true) {  //永久监听
                        //监听队列变化
                        try {
                            List<String> groups = zooKeeper.getChildren(PATH, new Watcher() {
                                @Override
                                public void process(WatchedEvent event) {
                                    if (event.getType() == Event.EventType.NodeChildrenChanged) {   //子节点有变化
                                        try {
                                            List<String> groups = zooKeeper.getChildren(event.getPath(), false);
                                            for (String group : groups) {
                                                List<String> ips = zooKeeper.getChildren(event.getPath() + "/" + group, false);
                                                //将队列名称与ip存入缓存
                                                cache.set(group, ips);
                                                for (String ip : ips) {
                                                    byte[] bytes = zooKeeper.getData(PATH + '/' + group + "/" + ip, false, null);
                                                    cache.set(ip, KryoUtil.byteToObj(bytes, Set.class));
                                                }
                                            }
                                        } catch (KeeperException e) {
                                            e.printStackTrace();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            for (final String group : groups) {
                                //监听ip信息变化
                                zooKeeper.getChildren(PATH + "/" + group, new Watcher() {
                                    @Override
                                    public void process(WatchedEvent event) {
                                        if (event.getType() == Event.EventType.NodeChildrenChanged) {
                                            try {
                                                List<String> ips = zooKeeper.getChildren(event.getPath(), false);
                                                //将队列名称与ip存入缓存
                                                cache.set(group, ips);
                                                for (String ip : ips) {
                                                    byte[] bytes = zooKeeper.getData(PATH + '/' + group + "/" + ip, false, null);
                                                    cache.set(ip, KryoUtil.byteToObj(bytes, ArrayList.class));
                                                }
                                            } catch (KeeperException e) {
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            }
                        } catch (KeeperException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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

}
