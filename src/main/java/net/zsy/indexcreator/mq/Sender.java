package net.zsy.indexcreator.mq;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * 消息队列发送器接口
 *
 */
public interface Sender extends MessageQueue {

	BlockingQueue<String> getQueue();

	void send(String message);

	void send(List<String> message);

}
