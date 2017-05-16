package com.gome.fup.mq.receiver.test;

import com.gome.fup.mq.common.listener.MessageReceiver;
import com.gome.fup.mq.common.model.Message;

/**
 * 
 *
 * @author fupeng-ds
 */
public class ReceiveTest2 extends MessageReceiver{

	/* (non-Javadoc)
	 * @see com.gome.fup.mq.common.listener.MessageListener#onMessage(com.gome.fup.mq.common.model.Message)
	 */
	public void onMessage(Message message) {
		System.out.println(this.groupName + "-----" + message.getMsg());
	}


}
