package com.hotel.hotel_system.util;

import java.time.LocalDate;

public class DateUtils {

    public static LocalDate getMonthStartDate(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    public static LocalDate getMonthEndDate(int year, int month) {
        return getMonthStartDate(year, month).withDayOfMonth(
                getMonthStartDate(year, month).lengthOfMonth()
        );
    }
}
