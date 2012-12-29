package me.vivia.bean;

import java.util.Date;

/**
 * 留存统计 统计频率：日
 * 
 * @author jkp
 * 
 */
public class RetentionStat {

	/** 编号 */
	// private Integer id;

	/** 统计日期 */
	private Date statDate;

	/** 新增用户数 */
	private Integer registerCount;

	/** 次日、三日、四日、五日、六日、七日、10日、20日、30日、40日留存数 */
	private Integer retentionCount[];

	public RetentionStat() {
		retentionCount = new Integer[10];
	}

	public Date getStatDate() {
		return statDate;
	}

	public void setStatDate(Date statDate) {
		this.statDate = statDate;
	}

	public Integer getRegisterCount() {
		return registerCount;
	}

	public void setRegisterCount(Integer registerCount) {
		this.registerCount = registerCount;
	}

	public Integer[] getRetentionCount() {
		return retentionCount;
	}

	public void setRetentionCount(Integer[] retentionCount) {
		this.retentionCount = retentionCount;
	}

}
