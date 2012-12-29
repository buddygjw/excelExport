package me.vivia.service;

import java.util.Date;
import java.util.Random;

import me.vivia.bean.RetentionStat;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

@Service
public class StatService {

	private Random random = new Random();

	/**
	 * 获取指定渠道的留存统计数据
	 * 
	 * @param channel
	 * @param date
	 *            账号注册日期
	 * @return
	 */
	public RetentionStat getChannelRetentionStat(int channel, Date date) {
		RetentionStat rs = new RetentionStat();
		rs.setStatDate(date);
		rs.setRegisterCount(random.nextInt(10000));
		Date now = new Date();
		for (int i = 0; i < 10; i++) {
			Date theDate = DateUtils.addDays(date, i < 6 ? i + 1
					: (i - 6) * 10 - 1);
			if (DateUtils.isSameDay(now, theDate))
				break;
			rs.getRetentionCount()[i] = random.nextInt(2000);
		}
		return rs;
	}

}
