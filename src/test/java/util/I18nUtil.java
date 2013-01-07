package util;

import java.lang.reflect.Field;

import me.vivia.bean.RetentionStat;
import me.vivia.constant.StatType;

/**
 * 国际化辅助工具类
 * 
 * @author jkp
 * 
 */
public class I18nUtil {

	public static void exportBeanI18n(Class clazz) {
		String className = Character.toLowerCase(clazz.getSimpleName()
				.charAt(0)) + clazz.getSimpleName().substring(1);
		System.out.println(className + "=");
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().equals("serialVersionUID"))
				continue;
			System.out.println(className + "." + field.getName() + "=");
		}
		System.out.println();
	}

	public static void exportConstantI18n(Class clazz) {
		String className = Character.toLowerCase(clazz.getSimpleName()
				.charAt(0)) + clazz.getSimpleName().substring(1);
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().equals("serialVersionUID"))
				continue;
			System.out.println(className + "." + field.getName() + "=");
			try {
				System.out.println(className + "." + field.get(null) + "=");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		System.out.println();
	}

	public static void main(String[] args) {
		exportBeanI18n(RetentionStat.class);
		exportConstantI18n(StatType.class);
	}

}
