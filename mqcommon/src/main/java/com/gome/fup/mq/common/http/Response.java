package com.gome.fup.mq.common.http;

import java.io.Serializable;
import java.util.UUID;

/**
 * 
 *
 * @author fupeng-ds
 */
@SuppressWarnings("serial")
public class Response implements Serializable{
	
	private String id;
	
	private String groupName;
	
	private String data;
	
	private int status;
	
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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Response() {
		super();
		this.id = UUID.randomUUID().toString();
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Response [id=" + id + ", groupName=" + groupName + ", data="
				+ data + ", status=" + status + "]";
	}

	
	
}
