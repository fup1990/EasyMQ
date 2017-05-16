package com.gome.fup.mq.common.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * 
 *
 * @author fupeng-ds
 */
@SuppressWarnings("serial")
public class Message implements Serializable{

	private String id;
	
	private String msg;
	
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Message(String msg) {
		super();
		this.id = UUID.randomUUID().toString();
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "Message [id=" + id + ", msg=" + msg + "]";
	}
	
	
}
