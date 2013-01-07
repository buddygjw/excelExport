package me.vivia.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 渠道统计 统计频率：日周、月
 * 
 * @author jkp
 * 
 */
public class ChannelStat implements Serializable {

	private static final long serialVersionUID = -1345070020813111258L;

	/** 编号 */
	private Integer id;

	/** 统计日期 */
	private Date statDate;

	/** 统计类型 具体参考com.novagame.report.constant.StatType常量类 */
	private Integer statType;

	/** 渠道编号 */
	private Integer channel;

	/** 累积用户数 */
	private Integer totalCount;

	/** 新增账号数 */
	private Integer registerCount;

	/** 新增账号中转化为有效账号的数量 */
	private Integer validCount;

	/** 新增账号中未创建角色的账号数量 */
	private Integer emptyCount;

	/** 独立付费用户数 */
	private Integer payCount;

	/** 付费总额 */
	private Long payAmount;

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

	public Integer getStatType() {
		return statType;
	}

	public void setStatType(Integer statType) {
		this.statType = statType;
	}

	public Integer getChannel() {
		return channel;
	}

	public void setChannel(Integer channel) {
		this.channel = channel;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getRegisterCount() {
		return registerCount;
	}

	public void setRegisterCount(Integer registerCount) {
		this.registerCount = registerCount;
	}

	public Integer getValidCount() {
		return validCount;
	}

	public void setValidCount(Integer validCount) {
		this.validCount = validCount;
	}

	public Integer getEmptyCount() {
		return emptyCount;
	}

	public void setEmptyCount(Integer emptyCount) {
		this.emptyCount = emptyCount;
	}

	public Integer getPayCount() {
		return payCount;
	}

	public void setPayCount(Integer payCount) {
		this.payCount = payCount;
	}

	public Long getPayAmount() {
		return payAmount;
	}

	public void setPayAmount(Long payAmount) {
		this.payAmount = payAmount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelStat [id=");
		builder.append(id);
		builder.append(", statDate=");
		builder.append(statDate);
		builder.append(", statType=");
		builder.append(statType);
		builder.append(", channel=");
		builder.append(channel);
		builder.append(", totalCount=");
		builder.append(totalCount);
		builder.append(", registerCount=");
		builder.append(registerCount);
		builder.append(", validCount=");
		builder.append(validCount);
		builder.append(", emptyCount=");
		builder.append(emptyCount);
		builder.append(", payCount=");
		builder.append(payCount);
		builder.append(", payAmount=");
		builder.append(payAmount);
		builder.append("]");
		return builder.toString();
	}

}
