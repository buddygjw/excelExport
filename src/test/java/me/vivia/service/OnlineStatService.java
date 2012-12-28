package me.vivia.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.vivia.bean.OnlineStat;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

@Service
public class OnlineStatService {

	private Random random = new Random();

	/**
	 * 获取最近24小时的在线统计数据
	 * 
	 * @return
	 */
	public List<OnlineStat> getLast24HoursOnlineStat() {
		Calendar c = Calendar.getInstance();
		Date to = DateUtils.truncate(DateUtils.setMinutes(c.getTime(),
				c.get(Calendar.MINUTE) / 10 * 10), Calendar.MINUTE);
		Date from = DateUtils.addHours(to, -24);
		List<OnlineStat> list = new ArrayList<OnlineStat>();
		while (from.getTime() < to.getTime()) {
			OnlineStat os = new OnlineStat();
			os.setStatTime(from);
			os.setOnlineCount(random.nextInt(10000));
			list.add(os);
			from = DateUtils.addMinutes(from, 10);
		}
		return list;
	}

	/**
	 * 获取指定游戏服在指定时间段内的在线统计数据
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public Map<String, List<OnlineStat>> getOnlineStatByServer(Date from,
			Date to) {
		Map<String, List<OnlineStat>> map = new HashMap<String, List<OnlineStat>>();
		String servers[] = new String[] { "A服", "B服" };
		for (int i = 0; i < servers.length; i++) {
			int index = 1;
			String name = servers[i];
			List<OnlineStat> list = new ArrayList<OnlineStat>();
			while (index % 100 != 0) {
				OnlineStat os = new OnlineStat();
				os.setId(index++);
				os.setServerId(i);
				os.setStatTime(from);
				os.setOnlineCount(random.nextInt(10000));
				list.add(os);
				from = DateUtils.addMinutes(from, 10);
			}
			map.put(name, list);
		}
		return map;
	}

}
