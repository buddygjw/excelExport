package me.vivia.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import me.vivia.bean.ActiveStat;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

@Service
public class ActiveStatService {
	
	private Random random = new Random();

	/**
	 * 获取指定时间段、指定类型的活跃统计数据
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public List<ActiveStat> getActiveStat(Date from, Date to) {
		List<ActiveStat> list = new ArrayList<ActiveStat>();
		int index = 1;
		while(from.getTime()<to.getTime()) {
			ActiveStat as = new ActiveStat();
			as.setId(index++);
			as.set1dayActiveCount(random.nextInt(1000));
			as.set3dayActiveCount(random.nextInt(1000));
			as.set7dayActiveCount(random.nextInt(1000));
			as.set30dayActiveCount(random.nextInt(1000));
			as.set1dayValidActiveCount(random.nextInt(1000));
			as.set3dayValidActiveCount(random.nextInt(1000));
			as.set7dayValidActiveCount(random.nextInt(1000));
			as.set30dayValidActiveCount(random.nextInt(1000));
			as.setStatDate(from);
			from = DateUtils.addDays(from, 1);
			list.add(as);
		}
		return list;
	}

}
