package net.zsy.indexcreator.mq;

public interface Accepter extends MessageQueue {

	String accept();

	void kill(String host);
}
