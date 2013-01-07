package me.vivia.export;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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

import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;

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
	 * 将值以数值方式填入单元格
	 * 
	 * @param row
	 * @param index
	 * @param obj
	 */
	private void fillCellAsNumber(Row row, int index, Object obj,
			Class constantClass, String formular, Locale locale) {
		if (obj == null) {
			row.createCell(index).setCellValue("");
		} else {
			if (constantClass != null) {
				String className = Character.toLowerCase(constantClass
						.getSimpleName().charAt(0))
						+ constantClass.getSimpleName().substring(1);
				row.createCell(index).setCellValue(
						messageSource.getMessage(className + "." + obj, null,
								obj.toString(), locale));
			} else if (StringUtils.isNotBlank(formular)) {
				try {
					row.createCell(index).setCellValue(
							new ExpressionBuilder(formular)
									.withVariable("value",
											new Double(obj.toString())).build()
									.calculate());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (UnknownFunctionException e) {
					e.printStackTrace();
				} catch (UnparsableExpressionException e) {
					e.printStackTrace();
				}
			} else {
				row.createCell(index).setCellValue(new Double(obj.toString()));
			}
		}
	}

	/**
	 * 将值以日期方式填入单元格
	 * 
	 * @param row
	 * @param index
	 * @param obj
	 * @param cellStyle
	 */
	private void fillCellAsDate(Row row, int index, Object obj,
			CellStyle cellStyle) {
		Cell cell = row.createCell(index);
		cell.setCellValue((Date) obj);
		cell.setCellStyle(cellStyle);
	}

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
			Map<String, Class> escapeMap, Map<String, String> formularMap,
			Locale locale) {
		if (locale == null) {
			locale = Locale.CHINA;
		}
		if (escapeMap == null) {
			escapeMap = new HashMap<String, Class>();
		}
		if (formularMap == null) {
			formularMap = new HashMap<String, String>();
		}

		int rowIndex = 0;
		Row row = sheet.createRow(rowIndex++);
		// 列名
		int columnIndex = 0;
		// 输出列名
		for (Field field : fields) {
			Cell cell = row.createCell(columnIndex++);
			cell.setCellValue(messageSource.getMessage(
					className + "." + field.getName(), null, locale));
			cell.getCellStyle().setAlignment(CellStyle.ALIGN_CENTER);
		}
		// 输出数据
		if (list != null && list.size() > 0) {
			for (Object o : list) {
				columnIndex = 0;
				row = sheet.createRow(rowIndex++);
				for (Field field : fields) {
					String fieldName = Character.toLowerCase(field.getName()
							.charAt(0)) + field.getName().substring(1);
					if (TypeUtils.isAssignable(field.getType(), Number.class)) {
						// 数字类型字段
						try {
							fillCellAsNumber(row, columnIndex++,
									FieldUtils.readField(field, o, true),
									escapeMap.get(fieldName),
									formularMap.get(fieldName), locale);
						} catch (IllegalAccessException e) {
							logger.error(e.getMessage(), e);
						}
					} else if (TypeUtils.isAssignable(field.getType(),
							Date.class)) {
						// 日期类型字段
						try {
							fillCellAsDate(row, columnIndex++,
									FieldUtils.readField(field, o, true),
									cellStyle);
						} catch (IllegalAccessException e) {
							logger.error(e.getMessage(), e);
						}
					} else if (TypeUtils.isArrayType(field.getType())) {
						// 数组类型
						try {
							Object[] array = (Object[]) FieldUtils.readField(
									field, o, true);
							if (TypeUtils.isAssignable(field.getType()
									.getComponentType(), Number.class)) {
								for (Object obj : array) {
									fillCellAsNumber(row, columnIndex++, obj,
											escapeMap.get(fieldName),
											formularMap.get(fieldName), locale);
								}
							} else if (TypeUtils.isAssignable(field.getType()
									.getComponentType(), Date.class)) {
								for (Object obj : array) {
									fillCellAsDate(row, columnIndex++, obj,
											cellStyle);
								}
							} else {
								for (Object obj : array) {
									row.createCell(columnIndex++).setCellValue(
											obj.toString());
								}
							}
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
			// 如果指定的属性都不存在，也把所有属性输出
			if (fields.isEmpty()) {
				logger.error("xxxxxx 指定属性{}在{}不存在，导出所有属性", properties,
						clazz.getName());
				fields = Arrays.asList(clazz.getDeclaredFields());
			}
		}
		// 去掉serialVersionUID属性
		List<Field> theFields = new ArrayList<Field>();
		for (Field field : fields) {
			if (!field.getName().equals("serialVersionUID")) {
				theFields.add(field);
			}
		}
		return theFields;
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
	 * @param escapeMap
	 *            转义map,key为属性名，value为转义常量类
	 * @param formularMap
	 *            计算公式map，key为属性名，value为计算表达式(属性值占位符为value)
	 * @param dateformat
	 *            Date类型输出格式(excel格式)，默认为yyyy-mm-dd,完全格式为yyyy-mm-dd HH:MM:SS
	 * @param locale
	 *            当前Locale，用于列名(属性名)的国际化输出
	 * @return
	 */
	public Workbook export(Class clazz, List list, String[] properties,
			Map<String, Class> escapeMap, Map<String, String> formularMap,
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
		fillSheetWithData(sheet, className, fields, cellStyle, list, escapeMap,
				formularMap, locale);

		return wb;
	}

	/**
	 * 导出excel并输出到客户端
	 * 
	 * @param response
	 * @param clazz
	 *            要导出的数据Bean的类型
	 * @param list
	 *            要导出的数据
	 * @param properties
	 *            要导出的数据Bean的字段，可以用来排序或指定导出某些属性，null时将导出所有属性
	 * @param escapeMap
	 *            转义map,key为属性名，value为转义常量类
	 * @param formularMap
	 *            计算公式map，key为属性名，value为计算表达式(属性值占位符为value)
	 * @param dateformat
	 *            Date类型输出格式(excel格式)，默认为yyyy-mm-dd,完全格式为yyyy-mm-dd HH:MM:SS
	 * @param locale
	 *            当前Locale，用于列名(属性名)的国际化输出
	 * @throws IOException
	 */
	public void executeDownload(HttpServletResponse response, Class clazz,
			List list, String[] properties, Map<String, Class> escapeMap,
			Map<String, String> formularMap, String dateformat, Locale locale)
			throws IOException {
		response.setContentType("application/vnd.ms-excel");
		response.addHeader("Content-Disposition",
				"attachment;filename=workbook.xls");
		export(clazz, list, properties, escapeMap, formularMap, dateformat,
				locale).write(response.getOutputStream());
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
	 * @param escapeMap
	 *            转义map,key为属性名，value为转义常量类
	 * @param formularMap
	 *            计算公式map，key为属性名，value为计算表达式(属性值占位符为value)
	 * @param dateformat
	 *            Date类型输出格式(excel格式)，默认为yyyy-mm-dd,完全格式为yyyy-mm-dd HH:MM:SS
	 * @param locale
	 *            当前Locale，用于列名(属性名)的国际化输出
	 */
	public void appendDataToSheet(Workbook wb, String sheetTitle, Class clazz,
			List list, String[] properties, Map<String, Class> escapeMap,
			Map<String, String> formularMap, String dateformat, Locale locale) {
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
		fillSheetWithData(sheet, className, fields, cellStyle, list, escapeMap,
				formularMap, locale);
	}
}