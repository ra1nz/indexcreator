package net.zsy.indexcreator.product;

import java.util.Map;

import net.zsy.indexcreator.dispatch.AbstractIndex;
/**
 * 商品类型的索引创建消息定义
 *
 */
public class ProductIndex extends AbstractIndex {

	int id;

	int objId;

	public ProductIndex(Map<String, String> data) {
		super("product");
		initdata(data);
	}

	void initdata(Map<String, String> data) {

		if (!data.containsKey("id") || !data.containsKey("objId")) {
			throw new RuntimeException("id and objId is necessary. ");
		}
		id = Integer.valueOf(data.get("id").toString());
		objId = Integer.valueOf(data.get("objId").toString());

	}

	public int getId() {
		return id;
	}

	public int getObjId() {
		return objId;
	}
}
