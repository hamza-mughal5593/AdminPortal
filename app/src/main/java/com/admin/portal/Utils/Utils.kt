package com.admin.portal.Utils

import java.text.ParseException
import java.text.SimpleDateFormat

class Utils {
    companion object{
        fun millisecondsDate(date: String?): Long {
            //String date_ = date;
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            try {
                val mDate = sdf.parse(date)
                val timeInMilliseconds = mDate.time
                println("Date in milli :: $timeInMilliseconds")
                return timeInMilliseconds
            } catch (e: ParseException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
            return 0
        }
    }

}

