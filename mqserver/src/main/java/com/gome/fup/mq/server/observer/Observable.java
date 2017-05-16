package com.gome.fup.mq.server.observer;

/**
 * 被观察者
 *
 * @author fupeng-ds
 */
public interface Observable {

	// 数据发生了变化
	void setChange();

	// 通知所有的观察者
	void notifyObservers();

	void addObserver(Observer observer);
	
	void delObserver(Observer observer);
}
