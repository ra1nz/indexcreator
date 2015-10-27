package net.zsy.indexcreator.dispatch;

/**
 * 处理器接口
 *
 * @param <T>
 */
public interface IndexHandler<T extends Index> {

	void handle(T t);
}
