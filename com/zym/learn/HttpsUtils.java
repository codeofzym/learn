package learn.zym.com.learn.utils;


import android.util.Log;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author ZYM
 * @since 2019-10-29
 *
 * utils of https for obtain data for web page
 * */
public class HttpsUtils {
    private static final String TAG = "HttpsUtils";
    private static final int TIME_OUT = 5 * 1000;
    private static final String REQUEST_GET = "GET";
    private static final String REQUEST_POST = "POST";
    private static final String ENCODE = "GBK";

    public static StringBuffer crawlingFromWebWithoutJS(String url) {
        StringBuffer result = new StringBuffer();
        if(url == null) {
            return result;
        }

        try {
            HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
            con.setConnectTimeout(TIME_OUT);
            con.setReadTimeout(TIME_OUT);
            con.setDoInput(true);
            con.setDoOutput(false);
            con.setDefaultUseCaches(false);
            con.setRequestMethod(REQUEST_GET);
            con.connect();
            if(con.getResponseCode() == 200) {
                byte[] buf = new byte[4 *1024];
                int count = 0;

                InputStream is = con.getInputStream();
                while((count = is.read(buf, 0, buf.length)) > -1) {
                    result.append(new String(buf, 0, count, ENCODE));
                }
                is.close();
                con.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "" + result.toString());
        return result;
    }
}
