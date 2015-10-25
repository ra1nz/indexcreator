package net.zsy.indexcreator.mq;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public interface Sender extends MessageQueue {

	BlockingQueue<String> getQueue();

	void send(String message);

	void send(List<String> message);

}
