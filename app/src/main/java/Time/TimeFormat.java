package Time;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeFormat {


    public TimeFormat()
    {

    }

    public String getFormattedDate(long time)
    {
        return formatDate(time);
    }

    //transform long ms value to human readable date
    private String formatDate(long time)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        String minuteW = minute + "", hourW = hour + "", secondsW = seconds + "", monthW = month + 1 + "";
        if (minute < 10)
            minuteW = "0" + minute + "";
        if (hour < 10)
            hourW = "0" + hour;
        if (seconds < 10)
            secondsW = "0" + seconds;
        if (month < 10)
            monthW = "0" + month;
        String finalTime = hourW + ":" + minuteW + ":" + secondsW;
        String finalDate = day + "/" + monthW + "/" + year;
        return finalDate + " " + finalTime;
    }

    public String getFormattedTime(long time)
    {
        return formattedTime(time);
    }

    //transforms long ms value to human readable time
    private String formattedTime(long time)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int seconds = calendar.get(Calendar.SECOND);
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR);
        String minuteW = minute + "", hourW = hour + "", secondsW = seconds + "";
        if (minute < 10)
            minuteW = "0" + minute + "";
        if (hour < 10)
            hourW = "0" + hour;
        if (seconds < 10)
            secondsW = "0" + seconds;
        return hourW + ":" + minuteW + ":" + secondsW;
    }
}
