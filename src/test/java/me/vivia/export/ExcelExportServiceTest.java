package me.vivia.export;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import me.vivia.bean.ActiveStat;
import me.vivia.bean.OnlineStat;
import me.vivia.service.ActiveStatService;
import me.vivia.service.OnlineStatService;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
	private ExcelExportService excelExportService;

	@Test
	public void testExportExcel() throws ParseException {
		// 默认参数导出
		Workbook wb = excelExportService.export(ActiveStat.class,
				activeStatService.getActiveStat(
						DateUtils.parseDate("2012-12-01", "yyyy-MM-dd"),
						DateUtils.parseDate("2012-12-28", "yyyy-MM-dd")), null,
				null, null);

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("workbook.xls");
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
	public void testExportExcelWithProperties() throws ParseException {
		// 指定参数导出
		Workbook wb = excelExportService.export(ActiveStat.class,
				activeStatService.getActiveStat(
						DateUtils.parseDate("2012-12-01", "yyyy-MM-dd"),
						DateUtils.parseDate("2012-12-28", "yyyy-MM-dd")),
				new String[] { "statDate", "_1dayActiveCount",
						"_7dayActiveCount" }, null, null);

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("workbook1.xls");
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
	public void testExportExcelWithPropertiesWithoutI18N()
			throws ParseException {
		// 导出列名未完全国际化的
		Workbook wb = excelExportService.export(OnlineStat.class,
				onlineStatService.getLast24HoursOnlineStat(), new String[] {
						"statTime", "onlineCount" }, "yyyy-mm-dd HH:MM:SS",
				null);

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("workbook3.xls");
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
	public void testExportExcelWithMultiSheet() throws ParseException {
		// 导出多个sheet
		Workbook wb = new HSSFWorkbook();
		Map<String, List<OnlineStat>> map = onlineStatService
				.getOnlineStatByServer(
						DateUtils.parseDate("2012-12-26", "yyyy-MM-dd"),
						DateUtils.parseDate("2012-12-28", "yyyy-MM-dd"));
		for (Map.Entry<String, List<OnlineStat>> entry : map.entrySet()) {
			excelExportService.appendDataToSheet(wb, entry.getKey(),
					OnlineStat.class, entry.getValue(), null,
					"yyyy-mm-dd HH:MM:SS", null);
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("workbook4.xls");
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
}
