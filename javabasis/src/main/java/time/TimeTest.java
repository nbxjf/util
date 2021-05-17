package time;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 * Created by Jeff_xu on 2021/3/29.
 *
 * @author Jeff_xu
 */
public class TimeTest {

    public static void main(String[] args) {
        DateTime allowNotifyStartTime = DateTime.now().withHourOfDay(18).withMinuteOfHour(0).withSecondOfMinute(0);
        int seconds = Seconds.secondsBetween(DateTime.now(), allowNotifyStartTime).getSeconds();
        System.out.println(seconds);
    }
}
