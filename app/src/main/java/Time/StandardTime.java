package Time;

import java.util.Calendar;
import java.util.TimeZone;

public class StandardTime {

    private static StandardTime standardTime;

    private StandardTime()
    {

    }

    public static StandardTime getInstance() {
        if (standardTime == null)
            standardTime = new StandardTime();
        return standardTime;
    }

    public long getStandardTime()
    {
        TimeZone timeZone = TimeZone.getTimeZone("GMT-4");
        Calendar calendar = Calendar.getInstance(timeZone);
        return calendar.getTimeInMillis();
    }

    public long getCurrentTime()
    {
        return System.currentTimeMillis();
    }
}
