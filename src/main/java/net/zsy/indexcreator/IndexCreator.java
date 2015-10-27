package net.zsy.indexcreator;

import net.zsy.indexcreator.dispatch.AsyncDispatcher;
import net.zsy.indexcreator.dispatch.Dispatcher;
import net.zsy.indexcreator.mq.Accepter;
import net.zsy.indexcreator.mq.MessageQueues;
import net.zsy.indexcreator.mq.Sender;
import net.zsy.indexcreator.product.ProductIndex;
import net.zsy.indexcreator.product.ProductAsyncIndexHandler;
/**
 * 程序入口
 *
 */
public class IndexCreator {
	public static void main(String[] args) {
		final Accepter accepter = MessageQueues.getAccepter();
		final Sender sender = MessageQueues.getSender();

		final Dispatcher dispatcher = new AsyncDispatcher();
		
		dispatcher.init();
		accepter.init();
		dispatcher.init();
		
		dispatcher.register(ProductIndex.class, new ProductAsyncIndexHandler(sender.getQueue()));

		Thread mainthread = new Thread() {

			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					dispatcher.add(accepter.accept());
				}
			}

		};

		dispatcher.start();
		accepter.start();
		sender.start();
		mainthread.start();
	}
}
