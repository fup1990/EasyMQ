package com.gome.fup.mq.common.util;

import com.google.common.base.Strings;

/**
 * 
 *
 * @author fupeng-ds
 */
public class AddressUtil {

	public static String[] getServerAddr(String addr) {
		if(Strings.isNullOrEmpty(addr)) {
			return null;
		}
		return addr.split(":");
	}
}
