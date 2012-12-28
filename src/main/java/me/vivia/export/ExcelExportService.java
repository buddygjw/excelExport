package me.vivia.export;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

/**
 * Excel导出服务
 * 
 * @author jkp
 * 
 */
@Service
public class ExcelExportService {

	private static final Logger logger = LoggerFactory
			.getLogger(ExcelExportService.class);

	@Autowired
	private MessageSource messageSource;

	/**
	 * 填充sheet数据
	 * 
	 * @param sheet
	 * @param className
	 * @param fields
	 * @param cellStyle
	 * @param list
	 * @param locale
	 */
	private void fillSheetWithData(Sheet sheet, String className,
			List<Field> fields, CellStyle cellStyle, List<Object> list,
			Locale locale) {
		if (locale == null) {
			locale = Locale.CHINA;
		}

		int rowIndex = 0;
		Row row = sheet.createRow(rowIndex++);
		// 列名
		int columnIndex = 0;
		// 输出列名
		for (Field field : fields) {
			row.createCell(columnIndex++).setCellValue(
					messageSource.getMessage(className + "." + field.getName(),
							null, locale));
		}
		// 输出数据
		if (list != null && list.size() > 0) {
			for (Object o : list) {
				columnIndex = 0;
				row = sheet.createRow(rowIndex++);
				for (Field field : fields) {
					if (TypeUtils.isAssignable(field.getType(), Number.class)) {
						// 数字类型字段
						try {
							row.createCell(columnIndex++).setCellValue(
									new Double(FieldUtils.readField(field, o,
											true).toString()));
						} catch (IllegalAccessException e) {
							logger.error(e.getMessage(), e);
						}
					} else if (TypeUtils.isAssignable(field.getType(),
							Date.class)) {
						// 日期类型字段
						try {
							Cell cell = row.createCell(columnIndex++);
							cell.setCellValue((Date) FieldUtils.readField(
									field, o, true));
							cell.setCellStyle(cellStyle);
						} catch (IllegalAccessException e) {
							logger.error(e.getMessage(), e);
						}
					} else {
						// 其他类型
						try {
							row.createCell(columnIndex++).setCellValue(
									FieldUtils.readField(field, o, true)
											.toString());
						} catch (IllegalAccessException e) {
							logger.error(e.getMessage(), e);
						}
					}
				}

			}
		}
	}

	/**
	 * 获取需要导出的属性
	 * 
	 * @param clazz
	 * @param properties
	 * @return
	 */
	private List<Field> getFieldsToBeExported(Class clazz, String[] properties) {
		List<Field> fields = new ArrayList<Field>();
		if (properties == null || properties.length == 0) {
			// 未指定输出哪些属性则默认全部输出
			fields = Arrays.asList(clazz.getDeclaredFields());
		} else {
			// 指定了输出的属性
			for (String fieldName : properties) {
				Field field = FieldUtils.getDeclaredField(clazz, fieldName,
						true);
				if (field != null) {
					fields.add(field);
				}
			}
			// 如果指定的属性都不存在，也把所以属性输出
			if (fields.isEmpty()) {
				logger.error("xxxxxx 指定属性{}在{}不存在，导出所有属性", properties,
						clazz.getName());
				fields = Arrays.asList(clazz.getDeclaredFields());
			}
		}
		return fields;
	}

	/**
	 * 生成Excel的workbook供下载
	 * 
	 * @param clazz
	 *            要导出的数据Bean的类型
	 * @param list
	 *            要导出的数据
	 * @param properties
	 *            要导出的数据Bean的字段，可以用来排序或指定导出某些属性，null时将导出所有属性
	 * @param dateformat
	 *            Date类型输出格式(excel格式)，默认为yyyy-mm-dd,完全格式为yyyy-mm-dd HH:MM:SS
	 * @param locale
	 *            当前Locale，用于列名(属性名)的国际化输出
	 * @return
	 */
	public Workbook export(Class clazz, List list, String[] properties,
			String dateformat, Locale locale) {
		if (dateformat == null) {
			dateformat = "yyyy-mm-dd";
		}

		List<Field> fields = getFieldsToBeExported(clazz, properties);

		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		String className = Character.toLowerCase(clazz.getSimpleName()
				.charAt(0)) + clazz.getSimpleName().substring(1);
		Sheet sheet = wb.createSheet(messageSource.getMessage(className, null,
				locale));

		// 日期单元格样式
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(
				dateformat));

		// 填充数据
		fillSheetWithData(sheet, className, fields, cellStyle, list, locale);

		return wb;
	}

	/**
	 * 增加一张sheet
	 * 
	 * @param wb
	 *            要增加到的workbook对象
	 * @param sheetTitle
	 *            sheet标题
	 * @param clazz
	 *            要导出的数据Bean的类型
	 * @param list
	 *            要导出的数据
	 * @param properties
	 *            要导出的数据Bean的字段，可以用来排序或指定导出某些属性，null时将导出所有属性
	 * @param dateformat
	 *            Date类型输出格式(excel格式)，默认为yyyy-mm-dd,完全格式为yyyy-mm-dd HH:MM:SS
	 * @param locale
	 *            当前Locale，用于列名(属性名)的国际化输出
	 */
	public void appendDataToSheet(Workbook wb, String sheetTitle, Class clazz,
			List list, String[] properties, String dateformat, Locale locale) {
		if (dateformat == null) {
			dateformat = "yyyy-mm-dd";
		}

		List<Field> fields = getFieldsToBeExported(clazz, properties);

		CreationHelper createHelper = wb.getCreationHelper();
		String className = Character.toLowerCase(clazz.getSimpleName()
				.charAt(0)) + clazz.getSimpleName().substring(1);
		Sheet sheet = wb
				.createSheet(StringUtils.isEmpty(sheetTitle) ? messageSource
						.getMessage(className, null, locale) : sheetTitle);

		// 日期单元格样式
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(
				dateformat));

		// 填充数据
		fillSheetWithData(sheet, className, fields, cellStyle, list, locale);
	}
}
