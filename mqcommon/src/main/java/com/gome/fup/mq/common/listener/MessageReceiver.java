package com.gome.fup.mq.common.listener;

/**
 * 消息接收的抽象类
 *
 * @author fupeng-ds
 */
public abstract class MessageReceiver implements MessageListener{

	protected String groupName;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	
}
