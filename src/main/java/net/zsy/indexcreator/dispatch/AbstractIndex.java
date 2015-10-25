package net.zsy.indexcreator.dispatch;

public abstract class AbstractIndex implements Index {

	private String type;

	public AbstractIndex(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}

}
