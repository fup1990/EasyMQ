package com.gome.fup.mq.common.listener;

import com.gome.fup.mq.common.model.Message;

/**
 * 消息监听
 *
 * @author fupeng-ds
 */
public interface MessageListener {

	void onMessage(Message message);
}
