package net.zsy.indexcreator.dispatch;

/**
 * 分发器接口
 *
 */
public interface Dispatcher {

	void init();

	void start();

	void stop();
	
	void add(String message);

	void register(Class<? extends Index> clazz, @SuppressWarnings("rawtypes") IndexHandler indexHandler);
}
