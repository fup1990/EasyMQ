package com.gome.fup.mq.server.queue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.gome.fup.mq.common.util.Cache;
import com.gome.fup.mq.sender.QueueSender;
import com.gome.fup.mq.server.observer.Observable;
import com.gome.fup.mq.server.observer.Observer;
import com.gome.fup.mq.server.observer.QueueObserver;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * 自定义消息队列
 *
 * @author fupeng-ds
 */
@SuppressWarnings("serial")
public class Queue<E> extends LinkedBlockingQueue<E> implements Observable{
	
	private boolean change = false;
	
	private List<Observer> list;

	private Disruptor<QueueSender> disruptor;
	
	//组名
	private String groupName;

	public synchronized void setChange() {
		this.change = true;
	}

	public void notifyObservers() {
		Object[] objs;
		synchronized(this) {
			 if(!change) {
				 return;
			 }
		}
		objs = list.toArray();
		for(int i = objs.length - 1; i >=0; i--) {
			((Observer)objs[i]).update(this, groupName);
		}
	}

	public Queue(Disruptor<QueueSender> disruptor, String groupName) {
		super();
		this.groupName = groupName;
		this.disruptor = disruptor;
		this.list = new LinkedList<>();
		addObserver(new QueueObserver(disruptor));
	}

	@Override
	public void put(E e) throws InterruptedException {
		super.put(e);
		if (Cache.getCache().hasKey(groupName)) {
			setChange();
			notifyObservers();
		}
	}

	public synchronized void addObserver(Observer observer) {
		if(observer != null) {
			if(!list.contains(observer)) {
				list.add(observer);
			}
		}
	}

	public synchronized void delObserver(Observer observer) {
		if(observer != null) {
			if(list.contains(observer)) {
				list.remove(observer);
			}
		}
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	

}
