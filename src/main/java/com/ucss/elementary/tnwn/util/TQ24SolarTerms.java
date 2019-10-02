package com.ucss.elementary.tnwn.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 24节气
 * 注：程序中使用到的计算节气公式、节气世纪常量等相关信息参照http://www.360doc.com/content/11/0106/22/5281066_84591519.shtml
 */
public class TQ24SolarTerms {
    private static final double D = 0.2422;
    private final static HashMap<String, Integer[]> INCREASE_OFFSETMAP = new HashMap<String, Integer[]>();//+1偏移
    private final static HashMap<String, Integer[]> DECREASE_OFFSETMAP = new HashMap<String, Integer[]>();//-1偏移

    /**
     * 24节气
     **/
    private static enum SolarTermsEnum {
        // --立春
        LICHUN,
        // --雨水
        YUSHUI,
        // --惊蛰
        JINGZHE,
        // 春分
        CHUNFEN,
        // 清明
        QINGMING,
        // 谷雨
        GUYU,
        // 立夏
        LIXIA,
        // 小满
        XIAOMAN,
        // 芒种
        MANGZHONG,
        // 夏至
        XIAZHI,
        // 小暑
        XIAOSHU,
        // 大暑
        DASHU,
        // 立秋
        LIQIU,
        // 处暑
        CHUSHU,
        // 白露
        BAILU,
        // 秋分
        QIUFEN,
        // 寒露
        HANLU,
        // 霜降
        SHUANGJIANG,
        // 立冬
        LIDONG,
        // 小雪
        XIAOXUE,
        // 大雪
        DAXUE,
        // 冬至
        DONGZHI,
        // 小寒
        XIAOHAN,
        // 大寒
        DAHAN;
    }

    static {
        DECREASE_OFFSETMAP.put(SolarTermsEnum.YUSHUI.name(), new Integer[]{2026});//雨水
        INCREASE_OFFSETMAP.put(SolarTermsEnum.CHUNFEN.name(), new Integer[]{2084});//春分
        INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAOMAN.name(), new Integer[]{2008});//小满
        INCREASE_OFFSETMAP.put(SolarTermsEnum.MANGZHONG.name(), new Integer[]{1902});//芒种
        INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAZHI.name(), new Integer[]{1928});//夏至
        INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAOSHU.name(), new Integer[]{1925, 2016});//小暑
        INCREASE_OFFSETMAP.put(SolarTermsEnum.DASHU.name(), new Integer[]{1922});//大暑
        INCREASE_OFFSETMAP.put(SolarTermsEnum.LIQIU.name(), new Integer[]{2002});//立秋
        INCREASE_OFFSETMAP.put(SolarTermsEnum.BAILU.name(), new Integer[]{1927});//白露
        INCREASE_OFFSETMAP.put(SolarTermsEnum.QIUFEN.name(), new Integer[]{1942});//秋分
        INCREASE_OFFSETMAP.put(SolarTermsEnum.SHUANGJIANG.name(), new Integer[]{2089});//霜降
        INCREASE_OFFSETMAP.put(SolarTermsEnum.LIDONG.name(), new Integer[]{2089});//立冬
        INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAOXUE.name(), new Integer[]{1978});//小雪
        INCREASE_OFFSETMAP.put(SolarTermsEnum.DAXUE.name(), new Integer[]{1954});//大雪
        DECREASE_OFFSETMAP.put(SolarTermsEnum.DONGZHI.name(), new Integer[]{1918, 2021});//冬至
        INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAOHAN.name(), new Integer[]{1982});//小寒
        DECREASE_OFFSETMAP.put(SolarTermsEnum.XIAOHAN.name(), new Integer[]{2019});//小寒
        INCREASE_OFFSETMAP.put(SolarTermsEnum.DAHAN.name(), new Integer[]{2082});//大寒
    }

    // 定义一个二维数组，第一维数组存储的是20世纪的节气C值，第二维数组存储的是21世纪的节气C值,0到23个，依次代表立春、雨水...大寒节气的C值
    private static final double[][] CENTURY_ARRAY = {
            {4.6295, 19.4599, 6.3826, 21.4155, 5.59, 20.888, 6.318, 21.86,
                    6.5, 22.2, 7.928, 23.65, 8.35, 23.95, 8.44, 23.822, 9.098,
                    24.218, 8.218, 23.08, 7.9, 22.6, 6.11, 20.84},
            {3.87, 18.73, 5.63, 20.646, 4.81, 20.1, 5.52, 21.04, 5.678, 21.37,
                    7.108, 22.83, 7.5, 23.13, 7.646, 23.042, 8.318, 23.438,
                    7.438, 22.36, 7.18, 21.94, 5.4055, 20.12}};

    /**
     * @param year 年份
     * @param name 节气的名称
     * @return 返回节气是相应月份的第几天
     */
    public static int getSolarTermNum(int year, String name) {

        double centuryValue = 0;//节气的世纪值，每个节气的每个世纪值都不同
        name = name.trim().toUpperCase();
        int ordinal = SolarTermsEnum.valueOf(name).ordinal();

        int centuryIndex = -1;
        if (year >= 1901 && year <= 2000) {//20世纪
            centuryIndex = 0;
        } else if (year >= 2001 && year <= 2100) {//21世纪
            centuryIndex = 1;
        } else {
            throw new RuntimeException("不支持此年份：" + year + "，目前只支持1901年到2100年的时间范围");
        }
        centuryValue = CENTURY_ARRAY[centuryIndex][ordinal];//获取节气的C值
        int dateNum = 0;
        /**
         * 计算 num =[Y*D+C]-L这是传说中的寿星通用公式
         * 公式解读：年数的后2位乘0.2422加C(即：centuryValue)取整数后，减闰年数
         */
        int y = year % 100;//步骤1:取年分的后两位数
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {//闰年
            if (ordinal == SolarTermsEnum.XIAOHAN.ordinal() || ordinal == SolarTermsEnum.DAHAN.ordinal()
                    || ordinal == SolarTermsEnum.LICHUN.ordinal() || ordinal == SolarTermsEnum.YUSHUI.ordinal()) {
                //注意：凡闰年3月1日前闰年数要减一，即：L=[(Y-1)/4],因为小寒、大寒、立春、雨水这两个节气都小于3月1日,所以 y = y-1
                y = y - 1;//步骤2
            }
        }
        dateNum = (int) (y * D + centuryValue) - (int) (y / 4);//步骤3，使用公式[Y*D+C]-L计算
        dateNum += specialYearOffset(year, name);//步骤4，加上特殊的年分的节气偏移量
        return dateNum;
    }

    /**
     * 特例,特殊的年分的节气偏移量,由于公式并不完善，所以算出的个别节气的第几天数并不准确，在此返回其偏移量
     *
     * @param year 年份
     * @param name 节气名称
     * @return 返回其偏移量
     */
    public static int specialYearOffset(int year, String name) {
        int offset = 0;
        offset += getOffset(DECREASE_OFFSETMAP, year, name, -1);
        offset += getOffset(INCREASE_OFFSETMAP, year, name, 1);

        return offset;
    }

    public static int getOffset(HashMap<String, Integer[]> map, int year, String name, int offset) {
        int off = 0;
        Integer[] years = map.get(name);
        if (null != years) {
            for (int i : years) {
                if (i == year) {
                    off = offset;
                    break;
                }
            }
        }
        return off;
    }

    public static String solarTermToString(int year) {
        StringBuffer sb = new StringBuffer();
        sb.append("---").append(year);
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {//闰年
            sb.append(" 闰年");
        } else {
            sb.append(" 平年");
        }

        sb.append("\n")
                .append("立春：2月").append(getSolarTermNum(year, SolarTermsEnum.LICHUN.name()))
                .append("日,雨水：2月").append(getSolarTermNum(year, SolarTermsEnum.YUSHUI.name()))
                .append("日,惊蛰:3月").append(getSolarTermNum(year, SolarTermsEnum.JINGZHE.name()))
                .append("日,春分:3月").append(getSolarTermNum(year, SolarTermsEnum.CHUNFEN.name()))
                .append("日,清明:4月").append(getSolarTermNum(year, SolarTermsEnum.QINGMING.name()))
                .append("日,谷雨:4月").append(getSolarTermNum(year, SolarTermsEnum.GUYU.name()))
                .append("日,立夏:5月").append(getSolarTermNum(year, SolarTermsEnum.LIXIA.name()))
                .append("日,小满:5月").append(getSolarTermNum(year, SolarTermsEnum.XIAOMAN.name()))
                .append("日,芒种:6月").append(getSolarTermNum(year, SolarTermsEnum.MANGZHONG.name()))
                .append("日,夏至:6月").append(getSolarTermNum(year, SolarTermsEnum.XIAZHI.name()))
                .append("日,小暑:7月").append(getSolarTermNum(year, SolarTermsEnum.XIAOSHU.name()))
                .append("日,大暑:7月").append(getSolarTermNum(year, SolarTermsEnum.DASHU.name()))
                .append("日,\n立秋:8月").append(getSolarTermNum(year, SolarTermsEnum.LIQIU.name()))
                .append("日,处暑:8月").append(getSolarTermNum(year, SolarTermsEnum.CHUSHU.name()))
                .append("日,白露:9月").append(getSolarTermNum(year, SolarTermsEnum.BAILU.name()))
                .append("日,秋分:9月").append(getSolarTermNum(year, SolarTermsEnum.QIUFEN.name()))
                .append("日,寒露:10月").append(getSolarTermNum(year, SolarTermsEnum.HANLU.name()))
                .append("日,霜降:10月").append(getSolarTermNum(year, SolarTermsEnum.SHUANGJIANG.name()))
                .append("日,立冬:11月").append(getSolarTermNum(year, SolarTermsEnum.LIDONG.name()))
                .append("日,小雪:11月").append(getSolarTermNum(year, SolarTermsEnum.XIAOXUE.name()))
                .append("日,大雪:12月").append(getSolarTermNum(year, SolarTermsEnum.DAXUE.name()))
                .append("日,冬至:12月").append(getSolarTermNum(year, SolarTermsEnum.DONGZHI.name()))
                .append("日,小寒:1月").append(getSolarTermNum(year, SolarTermsEnum.XIAOHAN.name()))
                .append("日,大寒:1月").append(getSolarTermNum(year, SolarTermsEnum.DAHAN.name()));

        return sb.toString();
    }

    public static Map<String,String> solarTermToMap(int year) {
       Map<String,String> result=new LinkedHashMap<>();
        result.put("立春","2月"+getSolarTermNum(year, SolarTermsEnum.LICHUN.name())+"日");
        result.put("雨水","2月"+getSolarTermNum(year, SolarTermsEnum.YUSHUI.name())+"日");
        result.put("惊蛰","3月"+getSolarTermNum(year, SolarTermsEnum.JINGZHE.name())+"日");
        result.put("春分","3月"+getSolarTermNum(year, SolarTermsEnum.CHUNFEN.name())+"日");

        result.put("清明","4月"+getSolarTermNum(year, SolarTermsEnum.QINGMING.name())+"日");
        result.put("谷雨","4月"+getSolarTermNum(year, SolarTermsEnum.GUYU.name())+"日");
        result.put("立夏","5月"+getSolarTermNum(year, SolarTermsEnum.LIXIA.name())+"日");
        result.put("小满","5月"+getSolarTermNum(year, SolarTermsEnum.XIAOMAN.name())+"日");

        result.put("芒种","6月"+getSolarTermNum(year, SolarTermsEnum.MANGZHONG.name())+"日");
        result.put("夏至","6月"+getSolarTermNum(year, SolarTermsEnum.XIAZHI.name())+"日");
        result.put("小暑","7月"+getSolarTermNum(year, SolarTermsEnum.XIAOSHU.name())+"日");
        result.put("大暑","7月"+getSolarTermNum(year, SolarTermsEnum.DASHU.name())+"日");

        result.put("立秋","8月"+getSolarTermNum(year, SolarTermsEnum.LIQIU.name())+"日");
        result.put("处暑","8月"+getSolarTermNum(year, SolarTermsEnum.CHUSHU.name())+"日");
        result.put("白露","9月"+getSolarTermNum(year, SolarTermsEnum.BAILU.name())+"日");
        result.put("秋分","9月"+getSolarTermNum(year, SolarTermsEnum.QIUFEN.name())+"日");

        result.put("寒露","10月"+getSolarTermNum(year, SolarTermsEnum.HANLU.name())+"日");
        result.put("霜降","10月"+getSolarTermNum(year, SolarTermsEnum.SHUANGJIANG.name())+"日");
        result.put("立冬","11月"+getSolarTermNum(year, SolarTermsEnum.LIDONG.name())+"日");
        result.put("小雪","11月"+getSolarTermNum(year, SolarTermsEnum.XIAOXUE.name())+"日");

        result.put("大雪","12月"+getSolarTermNum(year, SolarTermsEnum.DAXUE.name())+"日");
        result.put("冬至","12月"+getSolarTermNum(year, SolarTermsEnum.DONGZHI.name())+"日");
        result.put("小寒","1月"+getSolarTermNum(year, SolarTermsEnum.XIAOHAN.name())+"日");
        result.put("大寒","1月"+getSolarTermNum(year, SolarTermsEnum.DAHAN.name())+"日");
        return result;
    }

    public static long getLastDayByMin(){
        long min=0L;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        String last = format.format(ca.getTime())+" 23:59:59";
        try {
            long second=sdf.parse(last).getTime()-System.currentTimeMillis();
            min=second/(1000*60);

        } catch (ParseException e) {
            e.printStackTrace();
            min=0L;
        }
        return min;
    }

}
