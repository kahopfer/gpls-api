package com.designteam1.helpers;

import java.util.Calendar;
import java.util.Date;

public class LineItemHelpers {

    public boolean inSameDay(Date checkIn, Date checkOut) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(checkIn);
        cal2.setTime(checkOut);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
