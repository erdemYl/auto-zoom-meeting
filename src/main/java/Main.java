import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    /** Background cmd process to handle join/shutdown */
    private static Process joinProc;


    /** Converted current date to hh:mm */
    private static Date nowH;


    /** Current date */
    private static final Date now = new Date();


    /** Current calendar */
    private static final Calendar calendar = Calendar.getInstance();


    /** Date parser to hour */
    private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");


    public static void main(String[] args) {
        // init calendar
        calendar.setTime(now);
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);

        try {
            // find meetings
            List<Meeting> meetings = getMeetings(new File(args[0]));

            // join if available
            getAvailableFrom(meetings, weekday).ifPresent(
                    Main::join
            );
        } catch (FileNotFoundException ignored) {}
    }


    private static List<Meeting> getMeetings(File meetingsFile) throws FileNotFoundException {

        // read meeting file
        FileReader fr = new FileReader(meetingsFile);
        BufferedReader br = new BufferedReader(fr);
        String times = br.lines().reduce("", (a, b) -> a + b);

        // parse meeting times
        String[] parsedTimes = times.split("#");

        // parse each time
        List<Meeting> meetings = new LinkedList<>();
        for (String time : parsedTimes) {
            String[] infos = time.split(",");
            String id = infos[0].substring(3);
            String pwd = infos[1].substring(4);
            String begin = infos[2].substring(6);
            String end = infos[3].substring(4);
            String day = infos[4].substring(4);
            try {
                Meeting m = day.isEmpty()
                        ? new Meeting(id, pwd, begin, end)
                        : new Meeting(id, pwd, begin, end, parseWeekday(day));
                meetings.add(m);
            } catch (ParseException ignored) {}
        }

        return meetings;
    }


    private static Optional<Meeting> getAvailableFrom(List<Meeting> meetings, int weekday) {
        try {
            // current date to hour
            nowH = formatter.parse(formatter.format(now));

            for (Meeting m : meetings) {
                if (nowH.after(m.begin) && nowH.before(m.end)) {
                    if (m.weekday == 0 || m.weekday == calendar.get(Calendar.DAY_OF_WEEK))
                        return Optional.of(m);
                }
            }

        } catch (ParseException ignored) {}

        return Optional.empty();
    }


    private static void join(Meeting meeting) {
        // superuser.com/questions/1563255
        String meetingUrl = "\"zoommtg://zoom.us/join?action=join&confno=" + meeting.id + "&pwd=" + meeting.pwd + "\"";
        String joinCmd = "%HOMEPATH%\\AppData\\Roaming\\Zoom\\bin\\Zoom.exe --url=" + meetingUrl;

        // start joining
        execute(joinCmd);

        // schedule shutdown to end of meeting
        TimerTask shutdown = new TimerTask() {
            @Override
            public void run() {
                shutdown();
                System.exit(0);
            }
        };

        Timer timer = new Timer();
        timer.schedule(shutdown, meeting.getIntervalFrom(nowH));
    }


    private static void shutdown() {
        execute("shutdown -s -t 0");
        System.exit(0);
    }


    private static void execute(String cmd) {
        try {
            if (joinProc == null) {
                joinProc = Runtime.getRuntime().exec("cmd");
            }
            BufferedWriter writer = joinProc.outputWriter();
            writer.write(cmd);
            writer.newLine();
            writer.flush();
        } catch (IOException ignored) {}
    }


    private static int parseWeekday(String day) {
        switch (day.toLowerCase()) {
            case "montag" -> {
                return Calendar.MONDAY;
            }
            case "dienstag" -> {
                return Calendar.TUESDAY;
            }
            case "mittwoch" -> {
                return Calendar.WEDNESDAY;
            }
            case "donnerstag" -> {
                return Calendar.THURSDAY;
            }
            case "freitag" -> {
                return Calendar.FRIDAY;
            }
            case "samstag" -> {
                return Calendar.SATURDAY;
            }
            case "sonntag" -> {
                return Calendar.SUNDAY;
            }
            default -> {
                return 0;
            }
        }
    }
}
