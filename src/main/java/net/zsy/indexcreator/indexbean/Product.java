package net.zsy.indexcreator.indexbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class Product implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private String title;
	private boolean sellable;

	private Map<String, String> attrs;
	private List<Price> prices;

	public Product() {
		this.id = RandomUtils.nextInt(200000, 299999);
		this.title = RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(5, 50));
		this.sellable = RandomUtils.nextInt(0, 1) == 1;

		this.attrs = new HashMap<String, String>();
		for (int i = 0; i < RandomUtils.nextInt(5, 20); i++) {
			attrs.put("attr_" + i, "value_" + id + "_" + i);
		}
		this.prices = new ArrayList<Price>();
		for (int i = 0; i < RandomUtils.nextInt(1, 3); i++) {
			prices.add(new Price());
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isSellable() {
		return sellable;
	}

	public void setSellable(boolean sellable) {
		this.sellable = sellable;
	}

	public Map<String, String> getAttrs() {
		return attrs;
	}

	public void setAttrs(Map<String, String> attrs) {
		this.attrs = attrs;
	}

	public List<Price> getPrices() {
		return prices;
	}

	public void setPrices(List<Price> prices) {
		this.prices = prices;
	}

}
