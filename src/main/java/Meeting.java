import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class Meeting {
    final String id;
    final String pwd;
    final Date begin;
    final Date end;
    final int weekday;

    public Meeting(String id, String pwd, String begin, String end) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        this.id = id;
        this.pwd = pwd;
        this.begin = formatter.parse(begin);
        this.end = formatter.parse(end);
        weekday = 0;
    }

    public Meeting(String id, String pwd, String begin, String end, int weekday) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        this.id = id;
        this.pwd = pwd;
        this.begin = formatter.parse(begin);
        this.end = formatter.parse(end);
        this.weekday = weekday;
    }

    long getIntervalFrom(Date now) {
        Instant endIns = end.toInstant();
        Instant nowIns = now.toInstant();
        return endIns.minusMillis(nowIns.toEpochMilli()).toEpochMilli();
    }
}
