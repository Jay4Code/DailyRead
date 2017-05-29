package com.lga.util.date;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Jay on 2017/5/29.
 */

public class DateUtil {

    /**
     * 格式化时间，格式：yyyyMMdd
     *
     * @return string
     */
    public static String getFormatDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();

        return sdf.format(calendar.getTime());
    }
}
