Excel Export
===

a general excel export module.

quick demo
---

	...
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
	...

messageSource配置：

![](http://d.pr/i/2WWW+)

export result:

![](http://d.pr/i/p6pZ+)
