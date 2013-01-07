Excel Export
===

a general excel export module.

quick demo
---

<code>

	...
	int length = 10000;
	List<ChannelStat> list = new ArrayList<ChannelStat>(length);
	while (length > 0) {
		ChannelStat cs = new ChannelStat();
		cs.setId(4);
		cs.setStatDate(DateUtils.parseDate("2012-12-01", "yyyy-MM-dd"));
		cs.setChannel(1);
		cs.setEmptyCount(5);
		cs.setPayAmount(1L * new Random().nextInt(100000));		//单位为分，转出到excel时需要以元为单位输出
		cs.setPayCount(70);
		cs.setRegisterCount(500);
		cs.setStatType(StatType.DAILY_STAT);	//根据值转义：0-日统计 1-周统计 2-月统计
		cs.setTotalCount(10330);
		cs.setValidCount(430);
		list.add(cs);
		length--;
	}
	Map<String, Class> map = new HashMap<String, Class>();	//常量类转义，需要spring的messageSource
	map.put("statType", StatType.class);	//statType字段值根据StatType.class的值进行转义
	Map<String, String> formularMap = new HashMap<String, String>();	//属性计算公式map
	formularMap.put("payAmount", "value/100.0");	//payAmount字段值除以100.0后(即把分转为元)再输出
	Workbook wb = excelExportService.export(ChannelStat.class, list, null,
			map, formularMap, "yyyy-mm-dd", null);
	writeToFile(wb, "workbook7.xls");
	...

</code>

export result:

![](http://d.pr/i/XAnW+)