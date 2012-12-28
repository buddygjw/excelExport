package me.vivia.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 在线量统计
 * 
 * @author jkp
 * 
 */
public class OnlineStat implements Serializable {

	private static final long serialVersionUID = -215695678603794592L;

	private Integer id;

	private Date statTime;

	private Integer serverId;

	private Integer onlineCount;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getStatTime() {
		return statTime;
	}

	public void setStatTime(Date statTime) {
		this.statTime = statTime;
	}

	public Integer getServerId() {
		return serverId;
	}

	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}

	public Integer getOnlineCount() {
		return onlineCount;
	}

	public void setOnlineCount(Integer onlineCount) {
		this.onlineCount = onlineCount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OnlineStat [id=");
		builder.append(id);
		builder.append(", statTime=");
		builder.append(statTime);
		builder.append(", serverId=");
		builder.append(serverId);
		builder.append(", onlineCount=");
		builder.append(onlineCount);
		builder.append("]");
		return builder.toString();
	}

}
