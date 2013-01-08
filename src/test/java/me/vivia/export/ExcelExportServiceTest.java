package me.vivia.export;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.vivia.bean.ActiveStat;
import me.vivia.bean.ChannelStat;
import me.vivia.bean.OnlineStat;
import me.vivia.bean.RetentionStat;
import me.vivia.constant.StatType;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-test.xml")
public class ExcelExportServiceTest {

	Random random = new Random();

	@Autowired
	private ExcelExportService excelExportService;

	/**
	 * 将workbook写到文件中
	 * 
	 * @param wb
	 * @param fileName
	 */
	private void writeToFile(Workbook wb, String fileName) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fileName);
			wb.write(fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获取模拟活跃统计数据
	 * 
	 * @return
	 * @throws ParseException
	 */
	private List<ActiveStat> getActiveStatDate() throws ParseException {
		List<ActiveStat> list = new ArrayList<ActiveStat>();
		Date from = DateUtils.parseDate("2012-12-01", "yyyy-MM-dd");
		Date to = DateUtils.parseDate("2012-12-28", "yyyy-MM-dd");
		int index = 1;
		while (from.getTime() < to.getTime()) {
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

	/**
	 * 获取模拟在线量统计数据
	 * 
	 * @return
	 */
	private List<OnlineStat> getLast24HoursOnlineStat() {
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
	 * 获取分服在线量统计数据
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	private Map<String, List<OnlineStat>> getOnlineStatByServer(Date from,
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

	@Test
	public void testExportExcel() throws ParseException {
		// 默认参数导出
		Workbook wb = excelExportService.export(ActiveStat.class,
				getActiveStatDate(), null, null, null, null, null);
		writeToFile(wb, "workbook.xls");
	}

	@Test
	public void testExportExcelWithProperties() throws ParseException {
		// 指定参数导出
		Workbook wb = excelExportService.export(ActiveStat.class,
				getActiveStatDate(), new String[] { "statDate",
						"_1dayActiveCount", "_7dayActiveCount" }, null, null,
				null, null);

		writeToFile(wb, "workbook1.xls");
	}

	@Test
	public void testExportExcelWithPropertiesWithoutI18N()
			throws ParseException {
		// 导出列名未完全国际化的
		Map<String, String> formatMap = new HashMap<String, String>();
		formatMap.put("statTime", "yyyy-mm-dd HH:MM:SS");
		Workbook wb = excelExportService.export(OnlineStat.class,
				getLast24HoursOnlineStat(), new String[] { "statTime",
						"onlineCount" }, null, null, formatMap, null);
		writeToFile(wb, "workbook2.xls");
	}

	@Test
	public void testExportExcelWithMultiSheet() throws ParseException {
		// 导出多个sheet
		Map<String, String> formatMap = new HashMap<String, String>();
		formatMap.put("statTime", "yyyy-mm-dd HH:MM:SS");
		Workbook wb = new HSSFWorkbook();
		Map<String, List<OnlineStat>> map = getOnlineStatByServer(
				DateUtils.parseDate("2012-12-01", "yyyy-MM-dd"),
				DateUtils.parseDate("2012-12-28", "yyyy-MM-dd"));
		for (Map.Entry<String, List<OnlineStat>> entry : map.entrySet()) {
			excelExportService.appendDataToSheet(wb, entry.getKey(),
					OnlineStat.class, entry.getValue(), new String[] {
							"statTime", "onlineCount" }, null, null, formatMap,
					null);
		}
		writeToFile(wb, "workbook3.xls");
	}

	@Test
	public void testExportExcelWithArrayProperty() throws ParseException {
		// 导出带数组属性的
		Date date = DateUtils.parseDate("2012-12-01", "yyyy-MM-dd");
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
		Workbook wb = excelExportService.export(RetentionStat.class,
				Arrays.asList(new RetentionStat[] { rs }), new String[] {
						"statDate", "registerCount", "retentionCount" }, null,
				null, null, null);
		writeToFile(wb, "workbook4.xls");
	}

	@Test
	public void testExportExcelWithEscapeProperty() throws ParseException {
		// 导出带转义属性的
		List<ChannelStat> list = new ArrayList<ChannelStat>();
		int count = 10;
		Random r = new Random();
		while (count > 0) {
			ChannelStat cs = new ChannelStat();
			cs.setId(r.nextInt(50));
			cs.setStatDate(DateUtils.addDays(
					DateUtils.parseDate("2012-12-01", "yyyy-MM-dd"), count));
			cs.setChannel(r.nextInt(20));
			cs.setEmptyCount(r.nextInt(12222));
			cs.setPayAmount(1L * r.nextInt(12222));
			cs.setPayCount(r.nextInt(12222));
			cs.setRegisterCount(r.nextInt(12222));
			cs.setStatType(r.nextInt(5));
			cs.setTotalCount(r.nextInt(12222));
			cs.setValidCount(r.nextInt(12222));
			list.add(cs);
			count--;
		}

		Map<String, Class> map = new HashMap<String, Class>();
		map.put("statType", StatType.class);
		Workbook wb = excelExportService.export(ChannelStat.class, list, null,
				map, null, null, null);
		writeToFile(wb, "workbook5.xls");
	}

	@Test
	@Repeat(20)
	public void testExportExcelWithMassiveData() throws ParseException {
		// 大数据导出
		long start = System.currentTimeMillis();
		int length = 10000;
		List<ChannelStat> list = new ArrayList<ChannelStat>(length);
		while (length > 0) {
			ChannelStat cs = new ChannelStat();
			cs.setId(4);
			cs.setStatDate(DateUtils.parseDate("2012-12-01", "yyyy-MM-dd"));
			cs.setChannel(1);
			cs.setEmptyCount(5);
			cs.setPayAmount(6000L);
			cs.setPayCount(70);
			cs.setRegisterCount(500);
			cs.setStatType(StatType.DAILY_STAT);
			cs.setTotalCount(10330);
			cs.setValidCount(430);
			list.add(cs);
			length--;
		}

		Map<String, Class> map = new HashMap<String, Class>();
		map.put("statType", StatType.class);
		Workbook wb = excelExportService.export(ChannelStat.class, list, null,
				map, null, null, null);
		writeToFile(wb, "workbook6.xls");
		System.out.println("耗时：" + (System.currentTimeMillis() - start) + "毫秒");
	}

	@Test
	public void testExportExcelWithPropertyFormular() throws ParseException {
		// 导出带计算表达式和格式化表达式
		long start = System.currentTimeMillis();
		int length = 10000;
		int i = 0;
		int baseTotalCount = 2000;
		int baseValidCount = 1200;
		List<ChannelStat> list = new ArrayList<ChannelStat>(length);
		while (i < length) {
			baseTotalCount += random.nextInt(100);
			baseValidCount += random.nextInt(70);
			ChannelStat cs = new ChannelStat();
			cs.setId(random.nextInt(length));
			cs.setStatDate(DateUtils.addDays(
					DateUtils.parseDate("2012-12-01", "yyyy-MM-dd"), i));
			cs.setChannel(1);
			cs.setRegisterCount(random.nextInt(1000));
			cs.setEmptyCount(random.nextInt(cs.getRegisterCount() + 1));
			cs.setPayAmount(1L * (new Random().nextInt(10000000) + 1));// 单位为分，转出到excel时需要以元为单位输出
			cs.setPayCount(random.nextInt(200) + 1);
			cs.setStatType(StatType.DAILY_STAT); // 根据值转义：0-日统计 1-周统计 2-月统计
			cs.setTotalCount(baseTotalCount);
			cs.setValidCount(baseValidCount);
			list.add(cs);
			i++;
		}
		Map<String, Class> map = new HashMap<String, Class>();// 常量类转义，需要spring的messageSource
		map.put("statType", StatType.class); // statType字段值根据StatType.class的值进行转义
		Map<String, String> formularMap = new HashMap<String, String>();// 属性计算公式map
		formularMap.put("payAmount", "payAmount/100.0"); // payAmount字段值除以100.0后(即把分转为元)再输出
		formularMap.put("payARPU", "payAmount/payCount"); // 公式计算
		formularMap.put("registerARPU", "payAmount/registerCount"); // 公式计算
		formularMap.put("validARPU", "payAmount/validCount"); // 公式计算
		formularMap.put("validRate", "validCount/totalCount"); // 公式计算
		Map<String, String> formatMap = new HashMap<String, String>();
		formatMap.put("payAmount", "￥##,###.00"); // 千分位分隔
		formatMap.put("payARPU", "￥##,###.00"); // 千分位分隔
		formatMap.put("registerARPU", "￥##,###.00"); // 千分位分隔
		formatMap.put("validARPU", "￥##,###.00"); // 千分位分隔
		formatMap.put("validRate", "0.00%"); // 显示为百分比
		formatMap.put("registerCount", "[red][>=900];[blue][<100];[black]"); // 颜色区分:>=900红色，100以内为蓝色，其他为黑色
		Workbook wb = excelExportService.export(ChannelStat.class, list,
				new String[] { "statDate", "statType", "totalCount",
						"registerCount", "validCount", "validRate",
						"emptyCount", "payCount", "payAmount", "payARPU",
						"registerARPU", "validARPU" }, map, formularMap,
				formatMap, null);
		writeToFile(wb, "workbook7.xls");
		System.out.println("耗时：" + (System.currentTimeMillis() - start) + "毫秒");
	}

}
