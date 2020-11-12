package com.itenlee.search.analysis.help;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author tenlee
 * @date 2020/10/20
 */
public class DateUtil {
    /**
     * 计算当前时间 距离 开始时间的 差
     *
     * @param startTime 开始时间，HH:MM:ss格式。
     * @return 秒
     */
    public static long calcTimeGap(String startTime) {
        String[] parseTime = startTime.split(":");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDate.now().atTime(Integer.parseInt(parseTime[0]), Integer.parseInt(parseTime[1]),
                Integer.parseInt(parseTime[2]));

        if (start.isBefore(now)) {
            start = start.plusDays(1);
        }
        ZoneOffset zone = ZoneOffset.ofHours(8);
        return start.toEpochSecond(zone) - now.toEpochSecond(zone);
    }
}
