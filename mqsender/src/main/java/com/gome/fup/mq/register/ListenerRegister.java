package com.gome.fup.mq.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
public class ListenerRegister extends AbstractRegister implements ApplicationContextAware, InitializingBean{
	
	private final Logger logger = Logger.getLogger(this.getClass());

	//private Multimap<String, Listener> multimap = ArrayListMultimap.create();
	private Map<String, List<Listener>> multimap = new HashMap<String, List<Listener>>();
	
	private ApplicationContext applicationContext;
	
	private String localAddr;

	public void afterPropertiesSet() throws Exception {
		logger.debug("获取消息监听。");
		//获取所有继承了监听的实现
		Map<String, MessageReceiver> map = applicationContext.getBeansOfType(MessageReceiver.class);
		for(Map.Entry<String, MessageReceiver> entry : map.entrySet()) {
			MessageReceiver receiver = entry.getValue();
			Listener listener = new Listener(receiver.getClass().getName(), localAddr);
			//multimap.put(receiver.getGroupName(), listener);
			List<Listener> list;
			if(multimap.containsKey(receiver.getGroupName())) {
				list = multimap.get(receiver.getGroupName());
				list.add(listener);
			} else {
				list = new ArrayList<Listener>();
				list.add(listener);
				multimap.put(receiver.getGroupName(), list);
			}
		}
		logger.debug("开始向MQ服务器注册监听。");
		//发送给mq服务端
		sendListenerToMQServer(multimap);
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public String getLocalAddr() {
		return localAddr;
	}

	public void setLocalAddr(String localAddr) {
		this.localAddr = localAddr;
	}
	
	
}
