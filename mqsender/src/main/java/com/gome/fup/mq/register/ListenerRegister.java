package com.gome.fup.mq.register;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gome.fup.mq.common.util.KryoUtil;
import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.gome.fup.mq.common.listener.MessageReceiver;
import com.gome.fup.mq.common.model.Listener;

/**
 * 向服务端注册监听信息
 *
 * @author fupeng-ds
 */
public class ListenerRegister extends AbstractRegister implements Runnable, InitializingBean{
	
	private final Logger logger = Logger.getLogger(this.getClass());

	private ZooKeeper zooKeeper;

	private String zkServer;

	private final static String PATH = "/EasyMQ";

	private final static int SESSIONTIMEOUT = 5000;

	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	//private Multimap<String, Listener> multimap = ArrayListMultimap.create();
	private Map<String, Set<String>> multimap = new HashMap<String, Set<String>>();
	
	public void afterPropertiesSet() throws Exception {
		executorService.submit(this);
	}

	public void run() {
		//获取监听实现
		getListeners();
		//发送给mq服务端
		//sendListenerToMQServer(multimap);
		//将监听信息注册到zk
		registerToZK(multimap);
		//启动本地服务
		startCliendServer();
	}

	private void registerToZK(Map<String, Set<String>> multimap){
		try {
			zooKeeper = new ZooKeeper(zkServer, SESSIONTIMEOUT, new Watcher() {
                public void process(WatchedEvent event) {
                    logger.info("与zookeeper建立连接");
                }
            });
			for (Map.Entry<String, Set<String>> entry : multimap.entrySet()) {
				String key = entry.getKey();
				creatNode(PATH + "/" + key, null, CreateMode.PERSISTENT);
				Set<String> set = entry.getValue();
				ArrayList<String> list = new ArrayList<String>();
				list.addAll(set);
				byte[] bytes = KryoUtil.objToByte(list);
				creatNode(PATH + "/" + key + "/" + localAddr, bytes, CreateMode.EPHEMERAL);
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}

	}

	private void creatNode(String path,byte[] bytes, CreateMode createMode) throws KeeperException, InterruptedException {
		Stat exists = zooKeeper.exists(path, false);
		if (null == exists) {
			zooKeeper.create(path,bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE,createMode);
		}
	}

	private void getListeners() {
		logger.debug("获取消息监听。");
		//获取所有继承了监听的实现
		Map<String, MessageReceiver> map = applicationContext.getBeansOfType(MessageReceiver.class);
		for(Map.Entry<String, MessageReceiver> entry : map.entrySet()) {
			MessageReceiver receiver = entry.getValue();
			//Listener listener = new Listener(receiver.getClass().getName(), localAddr);
			//multimap.put(receiver.getGroupName(), listener);
			Set<String> set;
			if(multimap.containsKey(receiver.getGroupName())) {
				set = multimap.get(receiver.getGroupName());
				set.add(receiver.getClass().getName());
			} else {
				set = new HashSet<String>();
				set.add(receiver.getClass().getName());
				multimap.put(receiver.getGroupName(), set);
			}
		}
	}

	public String getZkServer() {
		return zkServer;
	}

	public void setZkServer(String zkServer) {
		this.zkServer = zkServer;
	}
}
