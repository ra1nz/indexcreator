package net.zsy.indexcreator.dispatch;

import java.util.concurrent.BlockingQueue;

public abstract class AsyncIndexHandler {

	protected final BlockingQueue<String> queue;

	public AsyncIndexHandler(BlockingQueue<String> queue) {
		this.queue = queue;
	}
}
