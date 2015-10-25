package net.zsy.indexcreator.dispatch;

public interface Dispatcher {

	void init();

	void start();

	void stop();
	
	void add(String message);

	void register(Class<? extends Index> clazz, @SuppressWarnings("rawtypes") IndexHandler indexHandler);
}
