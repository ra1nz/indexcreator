package net.zsy.indexcreator.indexbean;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.RandomUtils;

public class Price implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private int priceType;
	private int priceValue;
	private Date beginDatetime;
	private Date endDatetime;

	public Price() {

		this.id = RandomUtils.nextInt(100000, 999999);
		this.priceType = 1;
		this.priceValue = RandomUtils.nextInt(100, 999900);
		this.beginDatetime = new Date();
		this.endDatetime = new Date(beginDatetime.getTime() + 259200000l);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPriceType() {
		return priceType;
	}

	public void setPriceType(int priceType) {
		this.priceType = priceType;
	}

	public int getPriceValue() {
		return priceValue;
	}

	public void setPriceValue(int priceValue) {
		this.priceValue = priceValue;
	}

	public Date getBeginDatetime() {
		return beginDatetime;
	}

	public void setBeginDatetime(Date beginDatetime) {
		this.beginDatetime = beginDatetime;
	}

	public Date getEndDatetime() {
		return endDatetime;
	}

	public void setEndDatetime(Date endDatetime) {
		this.endDatetime = endDatetime;
	}

}
