package me.vivia.bean;

import java.util.Date;

/**
 * 活跃统计 统计频率：天
 * 
 * @author jkp
 * 
 */
public class ActiveStat {

	/** 编号 */
	private Integer id;

	/** 统计日期 */
	private Date statDate;

	/** 当日活跃用户数 */
	private Integer _1dayActiveCount;

	/** 3日活跃用户数 */
	private Integer _3dayActiveCount;

	/** 7日活跃用户数 */
	private Integer _7dayActiveCount;

	/** 30日活跃用户数 */
	private Integer _30dayActiveCount;

	/** 当日有效活跃用户数 */
	private Integer _1dayValidActiveCount;

	/** 3日有效活跃用户数 */
	private Integer _3dayValidActiveCount;

	/** 7日有效活跃用户数 */
	private Integer _7dayValidActiveCount;

	/** 30日有效活跃用户数 */
	private Integer _30dayValidActiveCount;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getStatDate() {
		return statDate;
	}

	public void setStatDate(Date statDate) {
		this.statDate = statDate;
	}

	public Integer get1dayActiveCount() {
		return _1dayActiveCount;
	}

	public void set1dayActiveCount(Integer _1dayActiveCount) {
		this._1dayActiveCount = _1dayActiveCount;
	}

	public Integer get3dayActiveCount() {
		return _3dayActiveCount;
	}

	public void set3dayActiveCount(Integer _3dayActiveCount) {
		this._3dayActiveCount = _3dayActiveCount;
	}

	public Integer get7dayActiveCount() {
		return _7dayActiveCount;
	}

	public void set7dayActiveCount(Integer _7dayActiveCount) {
		this._7dayActiveCount = _7dayActiveCount;
	}

	public Integer get30dayActiveCount() {
		return _30dayActiveCount;
	}

	public void set30dayActiveCount(Integer _30dayActiveCount) {
		this._30dayActiveCount = _30dayActiveCount;
	}

	public Integer get1dayValidActiveCount() {
		return _1dayValidActiveCount;
	}

	public void set1dayValidActiveCount(Integer _1dayValidActiveCount) {
		this._1dayValidActiveCount = _1dayValidActiveCount;
	}

	public Integer get3dayValidActiveCount() {
		return _3dayValidActiveCount;
	}

	public void set3dayValidActiveCount(Integer _3dayValidActiveCount) {
		this._3dayValidActiveCount = _3dayValidActiveCount;
	}

	public Integer get7dayValidActiveCount() {
		return _7dayValidActiveCount;
	}

	public void set7dayValidActiveCount(Integer _7dayValidActiveCount) {
		this._7dayValidActiveCount = _7dayValidActiveCount;
	}

	public Integer get30dayValidActiveCount() {
		return _30dayValidActiveCount;
	}

	public void set30dayValidActiveCount(Integer _30dayValidActiveCount) {
		this._30dayValidActiveCount = _30dayValidActiveCount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ActiveStat [id=");
		builder.append(id);
		builder.append(", statDate=");
		builder.append(statDate);
		builder.append(", _1dayActiveCount=");
		builder.append(_1dayActiveCount);
		builder.append(", _3dayActiveCount=");
		builder.append(_3dayActiveCount);
		builder.append(", _7dayActiveCount=");
		builder.append(_7dayActiveCount);
		builder.append(", _30dayActiveCount=");
		builder.append(_30dayActiveCount);
		builder.append(", _1dayValidActiveCount=");
		builder.append(_1dayValidActiveCount);
		builder.append(", _3dayValidActiveCount=");
		builder.append(_3dayValidActiveCount);
		builder.append(", _7dayValidActiveCount=");
		builder.append(_7dayValidActiveCount);
		builder.append(", _30dayValidActiveCount=");
		builder.append(_30dayValidActiveCount);
		builder.append("]");
		return builder.toString();
	}

}
