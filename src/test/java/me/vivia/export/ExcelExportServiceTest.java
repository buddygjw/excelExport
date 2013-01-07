package me.vivia.export;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.vivia.bean.ActiveStat;
import me.vivia.bean.ChannelStat;
import me.vivia.bean.OnlineStat;
import me.vivia.bean.RetentionStat;
import me.vivia.constant.StatType;
import me.vivia.service.ActiveStatService;
import me.vivia.service.OnlineStatService;
import me.vivia.service.StatService;

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

	@Autowired
	private ActiveStatService activeStatService;
	@Autowired
	private OnlineStatService onlineStatService;
	@Autowired
	private StatService statService;
	@Autowired
	private ExcelExportService excelExportService;

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

	@Test
	public void testExportExcel() throws ParseException {
		// 默认参数导出
		Workbook wb = excelExportService.export(ActiveStat.class,
				activeStatService.getActiveStat(
						DateUtils.parseDate("2012-12-01", "yyyy-MM-dd"),
						DateUtils.parseDate("2012-12-28", "yyyy-MM-dd")), null,
				null, null, null, null);
		writeToFile(wb, "workbook.xls");
	}

	@Test
	public void testExportExcelWithProperties() throws ParseException {
		// 指定参数导出
		Workbook wb = excelExportService.export(ActiveStat.class,
				activeStatService.getActiveStat(
						DateUtils.parseDate("2012-12-01", "yyyy-MM-dd"),
						DateUtils.parseDate("2012-12-28", "yyyy-MM-dd")),
				new String[] { "statDate", "_1dayActiveCount",
						"_7dayActiveCount" }, null, null, null, null);

		writeToFile(wb, "workbook1.xls");
	}

	@Test
	public void testExportExcelWithPropertiesWithoutI18N()
			throws ParseException {
		// 导出列名未完全国际化的
		Workbook wb = excelExportService.export(OnlineStat.class,
				onlineStatService.getLast24HoursOnlineStat(), new String[] {
						"statTime", "onlineCount" }, null, null,
				"yyyy-mm-dd HH:MM:SS", null);
		writeToFile(wb, "workbook2.xls");
	}

	@Test
	public void testExportExcelWithMultiSheet() throws ParseException {
		// 导出多个sheet
		Workbook wb = new HSSFWorkbook();
		Map<String, List<OnlineStat>> map = onlineStatService
				.getOnlineStatByServer(
						DateUtils.parseDate("2012-12-01", "yyyy-MM-dd"),
						DateUtils.parseDate("2012-12-28", "yyyy-MM-dd"));
		for (Map.Entry<String, List<OnlineStat>> entry : map.entrySet()) {
			excelExportService.appendDataToSheet(wb, entry.getKey(),
					OnlineStat.class, entry.getValue(), new String[] {
							"statTime", "onlineCount" }, null, null,
					"yyyy-mm-dd HH:MM:SS", null);
		}
		writeToFile(wb, "workbook3.xls");
	}

	@Test
	public void testExportExcelWithArrayProperty() throws ParseException {
		// 导出带数组属性的
		Workbook wb = excelExportService
				.export(RetentionStat.class,
						Arrays.asList(new RetentionStat[] { statService
								.getChannelRetentionStat(0, DateUtils
										.parseDate("2012-12-01", "yyyy-MM-dd")) }),
						null, null, null, "yyyy-mm-dd", null);
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
				map, null, "yyyy-mm-dd", null);
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
				map, null, "yyyy-mm-dd", null);
		writeToFile(wb, "workbook6.xls");
		System.out.println("耗时：" + (System.currentTimeMillis() - start) + "毫秒");
	}

	@Test
	public void testExportExcelWithPropertyFormular() throws ParseException {
		// 导出带计算表达式
		long start = System.currentTimeMillis();
		int length = 10000;
		List<ChannelStat> list = new ArrayList<ChannelStat>(length);
		while (length > 0) {
			ChannelStat cs = new ChannelStat();
			cs.setId(4);
			cs.setStatDate(DateUtils.parseDate("2012-12-01", "yyyy-MM-dd"));
			cs.setChannel(1);
			cs.setEmptyCount(5);
			cs.setPayAmount(1L * new Random().nextInt(100000));
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
		Map<String, String> formularMap = new HashMap<String, String>();
		formularMap.put("payAmount", "value/100.0");
		Workbook wb = excelExportService.export(ChannelStat.class, list, null,
				map, formularMap, "yyyy-mm-dd", null);
		writeToFile(wb, "workbook7.xls");
		System.out.println("耗时：" + (System.currentTimeMillis() - start) + "毫秒");
	}
}
