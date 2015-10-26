package net.zsy.indexcreator.dispatch;

public interface Index {

	String getType();

	String toString();

	static final String TYPE = "type";
	static final String ID = "id";
	static final String TIMESTAMP = "timestamp";
	static final String DATA = "data";
}
