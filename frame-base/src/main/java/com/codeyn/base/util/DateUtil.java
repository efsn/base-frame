package com.codeyn.base.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * The Class DateUtil.
 */
public class DateUtil {

    /**
     * Gets the current date time.
     * 
     * @return the current date time
     */
    public static Date getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    /**
     * Gets the the day before.
     * 
     * @return the the day before
     */
    public static Date getTheDayBefore() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    /**
     * Gets the dates by range.
     * 
     * @param date1
     *            the date1
     * @param date2
     *            the date2
     * @return the dates by range
     */
    public static List<Date> getDatesByRange(Date date1, Date date2) {
        List<Date> result = new ArrayList<Date>();
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        if (date1.before(date2)) {
            startCalendar.setTime(date1);
            endCalendar.setTime(date2);
            result.add(date1);
        } else {
            startCalendar.setTime(date2);
            endCalendar.setTime(date1);
            result.add(date2);
        }
        startCalendar.add(Calendar.DAY_OF_YEAR, 1);
        while (startCalendar.before(endCalendar)) {
            result.add(startCalendar.getTime());
            startCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return result;
    }

    /**
     * Gets the date start.
     * 
     * @param date1
     *            the date1
     * @param date2
     *            the date2
     * @return the date start
     */
    public static String getDateStart(Date date1, Date date2) {
        if (date1.before(date2)) {
            return new SimpleDateFormat("yyyy-MM-dd").format(date1)
                    + " 00:00:00";
        } else {
            return new SimpleDateFormat("yyyy-MM-dd").format(date2)
                    + " 00:00:00";
        }
    }

    /**
     * Gets the date end.
     * 
     * @param date1
     *            the date1
     * @param date2
     *            the date2
     * @return the date end
     */
    public static String getDateEnd(Date date1, Date date2) {
        if (date1.before(date2)) {
            return new SimpleDateFormat("yyyy-MM-dd").format(date2)
                    + " 23:59:59";
        } else {
            return new SimpleDateFormat("yyyy-MM-dd").format(date1)
                    + " 23:59:59";
        }
    }

    /**
     * Gets the date start.
     * 
     * @param date
     *            the date
     * @return the date start
     */
    public static String getDateStart(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date) + " 00:00:00";
    }

    /**
     * Gets the date end.
     * 
     * @param date
     *            the date
     * @return the date end
     */
    public static String getDateEnd(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date) + " 23:59:59";
    }
    
    /**
     * 判断系统当前时间是否在给定的日期之前
     * @param date
     * @return
     */
    public static boolean isBefore(Date date) {
        return getCurrentDateTime().before(date);
    }
    
    /**
     * 根据日历的规则，在给定日期的指定日历字段添加或减去指定的时间量
     * @author lichengjun
     * @since  SQQ-tick V100R001, 2014年4月14日
     * @param date
     * @param calendarField
     * @param amount
     * @return
     */
    public static Date add(Date date, int calendarField, int amount) {
        if(date == null) {
            throw new IllegalArgumentException("The date must not be null");
        } else {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(calendarField, amount);
            return c.getTime();
        }
    }
    
    
    /**
     * 判断系统当前时间是否在给定的日期之后
     * @param date
     * @return
     */
    public static boolean isAfter(Date date) {
        return !isBefore(date);
    }
    
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static Date getTimeDate(String time) {
        //format "yyyy-MM-dd HH:mm:00"
        String string = sdf.format(getCurrentDateTime());
        string += " " + time;
        Date date = null;
        try {
            date = sdf2.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    
    public static String format(Date date) {
        return sdf2.format(date);
    }
    
    public static Date stringToDateTime(String dateStr) {
        Date date = null;
        try {
            date = sdf2.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date stringToDate(String dateStr) {
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        System.out.println(DateUtil.getCurrentDateTime());
        System.out.println(DateUtil.getTheDayBefore());
        System.out.println(DateUtil.getDatesByRange(DateUtil.getTheDayBefore(),
                DateUtil.getCurrentDateTime()));
        System.out.println(DateUtil.getDatesByRange(
                DateUtil.getCurrentDateTime(), DateUtil.getTheDayBefore()));
        System.out.println(DateUtil.getDateStart(DateUtil.getCurrentDateTime(),
                DateUtil.getTheDayBefore()));
        System.out.println(DateUtil.getDateEnd(DateUtil.getCurrentDateTime(),
                DateUtil.getTheDayBefore()));
        System.out
                .println(DateUtil.getDateStart(DateUtil.getCurrentDateTime()));
        System.out.println(DateUtil.getDateEnd(DateUtil.getCurrentDateTime()));
        
        System.out.println(sdf2.format(getTimeDate("18:00")));
    }
}
