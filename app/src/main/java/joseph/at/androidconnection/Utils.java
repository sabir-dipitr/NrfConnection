package joseph.at.androidconnection;

public class Utils {

    public static void letMeSleepfor(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
