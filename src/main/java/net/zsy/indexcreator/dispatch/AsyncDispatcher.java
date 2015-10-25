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

@SuppressWarnings("rawtypes")
public class AsyncDispatcher implements Dispatcher {

	private BlockingQueue<Index> indexs;

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
		}
		inited.set(true);
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

	@Override
	public void register(Class<? extends Index> index, IndexHandler indexHandler) {
		if (!inited.get()) {
			throw new RuntimeException("Dispatcher must inited at the first.");
		}

		indexhandlers.put(index, indexHandler);
	}

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

	@SuppressWarnings("unchecked")
	@Override
	public void add(String message) {
		try {
			Map<String, String> data = objectMapper.readValue(message.getBytes(), Map.class);
			Object type = data.get("type");
			if("product".equals(type)){
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
