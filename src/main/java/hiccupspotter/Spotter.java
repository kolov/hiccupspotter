package hiccupspotter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Hello world!
 */
public class Spotter {

    private static final int CHECK_PERIOD = 20000;
    private static final int CHECK_DEVIATION = 5000;
    private static final int THRESHOLD_SLOW = 3000;
    private static final int THRESHOLD_VERY_SLOW = 15000;

    private byte[] buf = new byte[100000];
    private Random r = new Random();
    private DateFormat df = new SimpleDateFormat("dd/MM/yy kk:mm:ss");


    public static void main(String[] args) throws MalformedURLException {
        URL url = new URL("http://www.google.com");
        new Spotter().go(url);
    }

    public void go(URL url) {
        println("Checking " + url + " every " + CHECK_PERIOD + "ms");
        println("Press Ctrl-D to exit");
        startKeyboardThread(Thread.currentThread());
        Stats stats = new Stats();

        while (true) {
            long duration = readUrl(url);
            stats.process(duration);
            println(df.format(new Date()) + ": " + (duration == -1 ? " timeout" : Long.toString(duration) + "ms"));
            if (duration > THRESHOLD_SLOW) {
                System.out.println("!");
            }

            if (stopRequested(CHECK_PERIOD, CHECK_DEVIATION)) {
                break;
            }
        }
        println("Exit");
        stats.print();

    }

    private void println(String txt) {
        System.out.println(txt);
    }

    private void startKeyboardThread(final Thread otherThread) {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        int c = System.in.read();
                        if (c == -1) {
                            break;
                        }
                    } catch (IOException e) {
                        //ignore
                    }
                }
                otherThread.interrupt();
            }
        }).start();
    }


    private boolean stopRequested(int period, int deviation) {
        try {
            Thread.sleep(period - deviation / 2 + r.nextInt(deviation));
        } catch (InterruptedException e) {
            return true;
        }
        return false;
    }

    private long readUrl(URL url) {
        HttpURLConnection connection = null;
        long t1 = System.currentTimeMillis();
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream is = connection.getInputStream();
            is.read(buf);

        } catch (IOException e1) {
            return -1;
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
        return System.currentTimeMillis() - t1;
    }

    private class Stats {
        long min = Integer.MAX_VALUE;
        long max = Integer.MIN_VALUE;
        private int count = 0;
        private long createdAt = System.currentTimeMillis();
        private int failed = 0;
        private int slow = 0;
        private int verySlow = 0;

        public void process(long time) {
            if (time > -1) {
                if (time > max) {
                    max = time;
                }
                if (time < min) {
                    min = time;
                }
                if (time > THRESHOLD_SLOW) {
                    slow++;
                }
                if (time > THRESHOLD_VERY_SLOW) {
                    verySlow++;
                }

            } else {
                failed++;
            }
            count++;
        }

        public void print() {
            long now = System.currentTimeMillis();
            println("" + count + " requests" + " in " + (now - createdAt) + "ms");
            println("min/max: " + min + "/" + max);
            println("failed: " + failed);
            println("slow: " + slow);
            println("verySlow: " + verySlow);
        }
    }

}
