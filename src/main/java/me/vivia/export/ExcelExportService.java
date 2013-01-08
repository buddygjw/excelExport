package me.vivia.export;

import java.io.IOException;
import java.lang.reflect.Field;
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

	private static final char basicLabel[] = new char[] { 'A', 'B', 'C', 'D',
			'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
			'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	@Autowired
	private MessageSource messageSource;

	private String getColumnLabel(int columnIndex) {
		String str = String
				.valueOf(basicLabel[columnIndex % basicLabel.length]);

		int index = columnIndex / basicLabel.length;
		while (index > 0) {
			str = basicLabel[index - 1] + str;
			index /= basicLabel.length;
		}

		return str;
	}

	/**
	 * 将值以数值方式填入单元格
	 * 
	 * @param row
	 * @param index
	 * @param property
	 *            输出的属性名
	 * @param obj
	 *            输出的属性值
	 * @param constantClass
	 *            属性值对应的转义常量类(可选)
	 * @param formular
	 *            计算公式(可选)
	 * @param cellStyle
	 *            单元格样式
	 * @param locale
	 */
	private void fillCellAsNumber(Row row, int index, String property,
			Object obj, Class constantClass, String formular,
			CellStyle cellStyle, Locale locale) {
		Cell cell = null;
		if (obj == null) {
			cell = row.createCell(index);
			cell.setCellValue("");
		} else {
			if (constantClass != null) {
				String className = Character.toLowerCase(constantClass
						.getSimpleName().charAt(0))
						+ constantClass.getSimpleName().substring(1);
				cell = row.createCell(index);
				cell.setCellValue(messageSource.getMessage(className + "."
						+ obj, null, obj.toString(), locale));
			} else if (StringUtils.isNotBlank(formular)) {
				try {
					cell = row.createCell(index);
					cell.setCellValue(new ExpressionBuilder(formular)
							.withVariable(property, new Double(obj.toString()))
							.build().calculate());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				cell = row.createCell(index);
				cell.setCellValue(new Double(obj.toString()));
			}
		}
		if (cellStyle != null && cell != null) {
			cell.setCellStyle(cellStyle);
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
	 * @param wb
	 * @param sheet
	 * @param className
	 * @param properties
	 * @param fieldMap
	 * @param cellStyle
	 * @param list
	 * @param locale
	 */
	private void fillSheetWithData(Workbook wb, Sheet sheet, String className,
			String[] properties, Map<String, Field> fieldMap,
			List<Object> list, Map<String, Class> escapeMap,
			Map<String, String> formularMap, Map<String, String> formatMap,
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

		// 公式列结果保留两位小数
		CreationHelper createHelper = wb.getCreationHelper();
		CellStyle cellStyleNumber = wb.createCellStyle();
		cellStyleNumber.setDataFormat(createHelper.createDataFormat()
				.getFormat("0.##"));
		// 默认日期格式化
		CellStyle cellStyleDate = wb.createCellStyle();
		cellStyleDate.setDataFormat(createHelper.createDataFormat().getFormat(
				"yyyy-mm-dd"));
		// 自定义格式化
		Map<String, CellStyle> styleMap = new HashMap<String, CellStyle>();
		if (formatMap != null && formatMap.size() > 0) {
			for (String property : formatMap.keySet()) {
				CellStyle cellStyle = wb.createCellStyle();
				cellStyle.setDataFormat(createHelper.createDataFormat()
						.getFormat(formatMap.get(property)));
				styleMap.put(property, cellStyle);
			}
		}

		// 各属性列在excel中的列标
		Map<String, String> labelMap = new HashMap<String, String>();
		for (int i = 0; i < properties.length; i++) {
			String property = properties[i];
			labelMap.put(property, getColumnLabel(i));
		}
		// 对公式进行分解的正则表达式
		String regex = "[\\+\\-\\*/\\(\\)]";

		int rowIndex = 0;
		Row row = sheet.createRow(rowIndex++);
		// 列名
		int columnIndex = 0;
		// 输出列名
		for (String property : properties) {
			Cell cell = row.createCell(columnIndex++);
			cell.setCellValue(messageSource.getMessage(className + "."
					+ property, null, locale));
			cell.getCellStyle().setAlignment(CellStyle.ALIGN_CENTER);
		}
		// 输出数据
		if (list != null && list.size() > 0) {
			for (Object o : list) {
				columnIndex = 0;
				row = sheet.createRow(rowIndex++);
				for (String property : properties) {
					Field field = fieldMap.get(property);
					if (field != null) { // field不为空说明是将属性值输出或对该属性值进行简单计算
						String fieldName = Character.toLowerCase(field
								.getName().charAt(0))
								+ field.getName().substring(1);
						if (TypeUtils.isAssignable(field.getType(),
								Number.class)) {
							// 数字类型字段
							try {
								fillCellAsNumber(row, columnIndex++, property,
										FieldUtils.readField(field, o, true),
										escapeMap.get(fieldName),
										formularMap.get(fieldName),
										styleMap.get(property), locale);
							} catch (IllegalAccessException e) {
								logger.error(e.getMessage(), e);
							}
						} else if (TypeUtils.isAssignable(field.getType(),
								Date.class)) {
							// 日期类型字段
							CellStyle cellStyle = styleMap.get(property);
							try {
								fillCellAsDate(row, columnIndex++,
										FieldUtils.readField(field, o, true),
										cellStyle == null ? cellStyleDate
												: cellStyle);
							} catch (IllegalAccessException e) {
								logger.error(e.getMessage(), e);
							}
						} else if (TypeUtils.isArrayType(field.getType())) {
							// 数组类型
							try {
								Object[] array = (Object[]) FieldUtils
										.readField(field, o, true);
								if (TypeUtils.isAssignable(field.getType()
										.getComponentType(), Number.class)) {
									for (Object obj : array) {
										fillCellAsNumber(row, columnIndex++,
												property, obj,
												escapeMap.get(fieldName),
												formularMap.get(fieldName),
												styleMap.get(property), locale);
									}
								} else if (TypeUtils.isAssignable(field
										.getType().getComponentType(),
										Date.class)) {
									CellStyle cellStyle = styleMap
											.get(property);
									for (Object obj : array) {
										fillCellAsDate(
												row,
												columnIndex++,
												obj,
												cellStyle == null ? cellStyleDate
														: cellStyle);
									}
								} else {
									for (Object obj : array) {
										row.createCell(columnIndex++)
												.setCellValue(obj.toString());
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
					} else { // 否则将视为按计算公式对多个属性值进行计算

						String formular = formularMap.get(property);
						for (String str : formular.split(regex)) {
							String label = labelMap.get(str.trim());
							if (label != null) {
								formular = formular.replaceAll(str, label
										+ rowIndex);
							}
						}
						CellStyle cellStyle = styleMap.get(property);
						Cell cell = row.createCell(columnIndex++);
						cell.setCellFormula(formular);
						cell.setCellStyle(cellStyle == null ? cellStyleNumber
								: cellStyle);
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
	private Map<String, Field> getFieldsToBeExported(Class clazz,
			String[] properties) {
		Map<String, Field> fieldMap = new HashMap<String, Field>();
		if (properties == null || properties.length == 0) {
			// 未指定输出哪些属性则默认全部输出
			for (Field field : clazz.getDeclaredFields()) {
				fieldMap.put(field.getName(), field);
			}
		} else {
			// 指定了输出的属性
			for (String fieldName : properties) {
				Field field = FieldUtils.getDeclaredField(clazz, fieldName,
						true);
				if (field != null) {
					fieldMap.put(field.getName(), field);
				}
			}
			// 如果指定的属性都不存在，也把所有属性输出
			// if (fieldMap.isEmpty()) {
			// logger.error("xxxxxx 指定属性{}在{}不存在，导出所有属性", properties,
			// clazz.getName());
			// for (Field field : clazz.getDeclaredFields()) {
			// fieldMap.put(field.getName(), field);
			// }
			// }
		}
		// 去掉serialVersionUID属性
		fieldMap.remove("serialVersionUID");
		return fieldMap;
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
	 *            计算公式map，key为属性名，value为计算表达式(属性值占位符为属性名)
	 * @param formatMap
	 *            各属性对应的格式化表达式(excel自定义单元格格式表达式)
	 * @param locale
	 *            当前Locale，用于列名(属性名)的国际化输出
	 * @return
	 */
	public Workbook export(Class clazz, List list, String[] properties,
			Map<String, Class> escapeMap, Map<String, String> formularMap,
			Map<String, String> formatMap, Locale locale) {

		Map<String, Field> fieldMap = getFieldsToBeExported(clazz, properties);
		if (properties == null) {
			properties = fieldMap.keySet().toArray(new String[fieldMap.size()]);
		}

		Workbook wb = new HSSFWorkbook();
		String className = Character.toLowerCase(clazz.getSimpleName()
				.charAt(0)) + clazz.getSimpleName().substring(1);
		Sheet sheet = wb.createSheet(messageSource.getMessage(className, null,
				locale));

		// 填充数据
		fillSheetWithData(wb, sheet, className, properties, fieldMap, list,
				escapeMap, formularMap, formatMap, locale);

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
	 *            计算公式map，key为属性名，value为计算表达式(属性值占位符为属性名)
	 * @param formatMap
	 *            各属性对应的格式化表达式(excel自定义单元格格式表达式)
	 * @param locale
	 *            当前Locale，用于列名(属性名)的国际化输出
	 * @throws IOException
	 */
	public void executeDownload(HttpServletResponse response, Class clazz,
			List list, String[] properties, Map<String, Class> escapeMap,
			Map<String, String> formularMap, Map<String, String> formatMap,
			Locale locale) throws IOException {
		response.setContentType("application/vnd.ms-excel");
		response.addHeader("Content-Disposition",
				"attachment;filename=workbook.xls");
		export(clazz, list, properties, escapeMap, formularMap, formatMap,
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
	 *            计算公式map，key为属性名，value为计算表达式(属性值占位符为属性名)
	 * @param formatMap
	 *            各属性对应的格式化表达式(excel自定义单元格格式表达式)
	 * @param locale
	 *            当前Locale，用于列名(属性名)的国际化输出
	 */
	public void appendDataToSheet(Workbook wb, String sheetTitle, Class clazz,
			List list, String[] properties, Map<String, Class> escapeMap,
			Map<String, String> formularMap, Map<String, String> formatMap,
			Locale locale) {
		Map<String, Field> fieldMap = getFieldsToBeExported(clazz, properties);

		CreationHelper createHelper = wb.getCreationHelper();
		String className = Character.toLowerCase(clazz.getSimpleName()
				.charAt(0)) + clazz.getSimpleName().substring(1);
		Sheet sheet = wb
				.createSheet(StringUtils.isEmpty(sheetTitle) ? messageSource
						.getMessage(className, null, locale) : sheetTitle);

		// 填充数据
		fillSheetWithData(wb, sheet, className, properties, fieldMap, list,
				escapeMap, formularMap, formatMap, locale);
	}
}