package net.zsy.indexcreator.dispatch;

public interface IndexHandler<T extends Index> {

	void handle(T t);
}
