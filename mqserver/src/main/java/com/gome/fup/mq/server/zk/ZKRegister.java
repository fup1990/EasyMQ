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

    private final static int SESSIONTIMEOUT = 10000;

    @Override
    public void afterPropertiesSet() throws Exception {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    hasRootPath();
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
                                                cacheIps(group);
                                            }
                                        } catch (KeeperException e) {
                                            e.printStackTrace();
                                            logger.error(e.getMessage());
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                            logger.error(e.getMessage());
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
                                            cacheIps(group);
                                        }
                                    }
                                });
                            }
                        } catch (KeeperException e) {
                            e.printStackTrace();
                            logger.error(e.getMessage());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            logger.error(e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    zooKeeper.close();
                    executorService.shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
        }));
    }

    private void cacheIps(String group) {
        try {
            List<String> ips = zooKeeper.getChildren(PATH + "/" + group, false);
            //将队列名称与ip存入缓存
            cache.set(group, ips);
            for (String ip : ips) {
                byte[] bytes = zooKeeper.getData(PATH + '/' + group + "/" + ip, false, null);
                cache.set(group + "_" + ip, KryoUtil.byteToObj(bytes, ArrayList.class));
            }
        } catch (KeeperException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
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

    private void creatNode(String path,byte[] bytes, CreateMode createMode) throws KeeperException, InterruptedException {
        Stat exists = zooKeeper.exists(path, false);
        if (null == exists) {
            zooKeeper.create(path,bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE,createMode);
        }
    }

    private void hasRootPath() {
        try {
            zooKeeper = new ZooKeeper("/", SESSIONTIMEOUT, null);
            Stat exists = zooKeeper.exists(PATH, false);
            if (null == exists) {
                zooKeeper.create(PATH,null, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
