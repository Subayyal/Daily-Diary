package com.example.subayyal.dailynotes.HelperClasses;

import android.content.Context;
import android.widget.Toast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by subayyal on 3/25/2018.
 */

public class Utils {
    private static String selectedDate;
    private static String user_id;

    public static String getUser_id() {
        return user_id;
    }

    public static void setUser_id(String user_id) {
        Utils.user_id = user_id;
    }


    public static String getSelectedDate() {
        return selectedDate;
    }

    public static void setSelectedDate(String selectedDate) {
        Utils.selectedDate = selectedDate;
    }

    public static void setSelectedDate(long milliseconds) {
        selectedDate = new SimpleDateFormat("MM-dd-yyyy").format(new Date(milliseconds));
    }

    public static void setSelectedDate(Date date) {
        selectedDate = new SimpleDateFormat("MM-dd-yyyy").format(date);
    }

    public static void createToast(Context context, String message) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    public static long convertStringDateToMilliseconds(String date) {
        try {
            Date d = new SimpleDateFormat("MM-dd-yyyy").parse(date);
            Timestamp ts = new Timestamp(d.getTime());
            long t = ts.getTime();
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
