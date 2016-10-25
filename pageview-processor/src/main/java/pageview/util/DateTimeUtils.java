package pageview.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by duhc on 12/08/2015.
 */
public class DateTimeUtils {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_HOUR_PATTERN = "yyyy-MM-dd-HH";

    public static String format(Date date, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    public static Date parse(String dateStr, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date truncate(Date date, int field) {
        return DateUtils.truncate(date, field);
    }

    public static int get(String dateStr, String pattern, int field) {
        int value = -1;
        Date date = parse(dateStr, pattern);
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            value = calendar.get(field);
        }
        return value;
    }

    public static int get(Date date, int field) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(field);
    }

    public static List<String> getDateBackList(int backNumber, int field, String datePattern) {
        List<String> dates = Lists.newArrayListWithCapacity(backNumber);

        Date currentTime = new Date();
        for (int i = 0; i < backNumber; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentTime);
            calendar.add(field, 0 - i);
            String date = DateTimeUtils.format(calendar.getTime(), datePattern);
            dates.add(date);
        }
        return dates;
    }
}
