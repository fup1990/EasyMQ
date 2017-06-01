package com.gome.fup.mq.common.http;

import java.io.Serializable;
import java.util.UUID;

/**
 * 
 *
 * @author fupeng-ds
 */
@SuppressWarnings("serial")
public class Request implements Serializable{

	private String id;
	
	private String groupName;
	
	private String msg;
	
	private Integer type;

	private String listenerName;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Request() {
		super();
		this.id = UUID.randomUUID().toString();
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getListenerName() {
		return listenerName;
	}

	public void setListenerName(String listenerName) {
		this.listenerName = listenerName;
	}

	@Override
	public String toString() {
		return "Request{" +
				"id='" + id + '\'' +
				", groupName='" + groupName + '\'' +
				", msg='" + msg + '\'' +
				", type=" + type +
				", listenerName='" + listenerName + '\'' +
				'}';
	}
}
