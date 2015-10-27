package net.zsy.indexcreator.dispatch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.zsy.indexcreator.product.ProductIndex;

/**
 * 异步分发器，将从队列里获取的索引放入阻塞队列，由一个线程分发给对应的处理器
 *
 */
@SuppressWarnings("rawtypes")
public class AsyncDispatcher implements Dispatcher {

	// 异步分发队列
	private BlockingQueue<Index> indexs;

	// 已注册的处理器集合
	private Map<Class<? extends Index>, IndexHandler> indexhandlers;

	private AtomicBoolean inited = new AtomicBoolean(false);

	private AtomicBoolean running = new AtomicBoolean(false);
	// private AtomicBoolean shutdown = new AtomicBoolean(false);

	private Lock dispatchingLock = new ReentrantLock();

	private Thread dispatchThread;

	@Override
	public void init() {
		if (!inited.get()) {
			indexs = new LinkedBlockingQueue<Index>();
			indexhandlers = new HashMap<Class<? extends Index>, IndexHandler>();
			inited.set(true);
		}
	}

	@Override
	public void start() {
		if (!inited.get()) {
			throw new RuntimeException("Dispatcher must inited at the first.");
		}
		dispatchThread = new Thread(createRunnable());
		dispatchThread.start();
		running.set(true);
	}

	@Override
	public void stop() {
		if (running.get()) {
			dispatchThread.interrupt();
			running.set(false);
		}
	}

	/**
	 * 注册某Index类型的处理器
	 */
	@Override
	public void register(Class<? extends Index> index, IndexHandler indexHandler) {
		if (!inited.get()) {
			throw new RuntimeException("Dispatcher must inited at the first.");
		}

		indexhandlers.put(index, indexHandler);
	}

	/**
	 * 创建异步分发线程
	 * 
	 * @return
	 */
	Runnable createRunnable() {
		return new Runnable() {

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				while (running.get() && !Thread.currentThread().isInterrupted()) {
					dispatchingLock.lock();
					try {
						Index index = indexs.take();
						indexhandlers.get(index.getClass()).handle(index);
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
						running.set(false);
					} finally {
						dispatchingLock.unlock();
					}
				}

			}
		};
	}

	ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 将从消息队列里获取的消息构造为Index对象放入Blockingqueue
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void add(String message) {
		try {
			Map<String, String> data = objectMapper.readValue(message.getBytes(), Map.class);
			Object type = data.get("type");
			if ("product".equals(type)) {
				indexs.put(new ProductIndex(data));
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
