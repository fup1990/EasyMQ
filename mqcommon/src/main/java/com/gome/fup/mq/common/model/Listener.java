package com.gome.fup.mq.common.model;

import java.io.Serializable;

/**
 * 
 *
 * @author fupeng-ds
 */
@SuppressWarnings("serial")
public class Listener implements Serializable{

	private String name;
	
	private String addr;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public Listener(String name, String addr) {
		super();
		this.name = name;
		this.addr = addr;
	}

	public Listener() {
		super();
	}
	
	
}
