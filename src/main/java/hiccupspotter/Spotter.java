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


    private byte[] buf = new byte[100000];
    private Random r = new Random();
    private DateFormat df = new SimpleDateFormat("dd/MM/yy kk:mm:ss");



    public static void main(String[] args) throws MalformedURLException {
        URL url = new URL("http://www.google.com");
        System.out.println("Fetching " + url);
        System.out.println("Press Ctrl-D to exit");
        new Spotter().go(url);
    }

    public void go(URL url) {

        startKeyboardThread(Thread.currentThread());
        Stats stats = new Stats();

        while (true) {
            long duration = readUrl(url);
             stats.process(duration);
            System.out.println(df.format(new Date()) + ": " + (duration == -1 ? " timeout" : Long.toString(duration) + "ms"));
            if (duration > 2000) {
                System.out.println("!");
            }

            if (sleep(20000, 5000)) {
                break;
            }
        }
        stats.print();

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


    private boolean sleep(int period, int deviation) {
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

            public void process(long time) {
                if (time > -1) {
                    if (time > max) {
                        max = time;
                    }
                    if (time < min) {
                        min = time;
                    }
                    if (time > 15000) {
                        slow++;
                    }

                } else {
                    failed++;
                }
                count++;
            }

            public void print() {
                long now = System.currentTimeMillis();
                System.out.println("" + count + " requests" + " in " + (now - createdAt) + "ms");
                System.out.println("Total: " + count);
                System.out.println("min/max: " + min + "/" + max);
                System.out.println("Failed: " + failed);
                System.out.println(">15s: " + slow);
            }
        }

}
