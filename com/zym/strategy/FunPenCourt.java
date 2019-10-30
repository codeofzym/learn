package learn.zym.com.learn.strategy;

import android.content.Context;
import android.util.Log;

import learn.zym.com.learn.R;
import learn.zym.com.learn.utils.HttpsUtils;

/**
 * @author ZYM
 * @since 2019-10-29
 *
 * tools of crawing data from biquge
 * need to start in a thread of child
 * */
public class FunPenCourt {
    private static final String TAG = "FunPenCourt";

    //defaulte encode for web page
    private static final String ENCODE = "GBK";

    private static final String DEFAULT_URL = "https://www.biqugex.com/";
    private static final String CONTENT_FLAG = "<div id=\"content\" class=\"showtxt\">";
    private static final String TITLE_FLAG = "<div class=\"content\">";

    private final String mNextFlag;
    private final String mPreFlag;

    private String mNextUrl;
    private String mPreUrl;

    private String mTitle;

    public FunPenCourt(Context context) {
        if(context == null) {
            mNextFlag ="";
            mPreFlag ="";
            return;
        }

        mNextFlag = context.getString(R.string.next_chapter);
        mPreFlag = context.getString(R.string.pre_chapter);
    }

    private void parseTitle(StringBuffer head) {
//        Log.i(TAG, "" + head);
        int start = head.indexOf(TITLE_FLAG);
        if(start < 0) {
            return;
        }
        start = head.indexOf("<h1>", start);
        if(start < 0) {
            return;
        }

        int end = head.indexOf("</h1>", start);
        if(end < 0) {
            return;
        }
        mTitle = head.substring(start + "<h1>".length(), end);
        Log.i(TAG, "mTitle:" + mTitle);
    }

    private String formatContent(StringBuffer buf) {
        String result = new String(buf);
        result = result.replaceAll("<br />", "\n");
        result = result.replaceAll("&nbsp;", " ");
//        Log.i(TAG, "" + result);
        return result;
    }

    private void parsePreUrl(StringBuffer buf) {
        int end = buf.indexOf(mPreFlag);
        if(end  < 0) {
            return;
        }

        int start = buf.indexOf("book_", end - 40);
        if(start < 0) {
            return;
        }
        mPreUrl = buf.substring(start, end - 2);
//        Log.i(TAG, "mPreUrl:" + mPreUrl);
    }

    private void parseNextUrl(StringBuffer buf) {
        int end = buf.indexOf(mNextFlag);
        if(end  < 0) {
            return;
        }

        int start = buf.indexOf("book_", end - 40);
        if(start < 0) {
            return;
        }
        mNextUrl = buf.substring(start, end - 2);
//        Log.i(TAG, "mNextUrl:" + mNextUrl);
    }

    /**
     * parse current of web page form url
     * */
    public String parseWebPage(String url) {
        StringBuffer result = HttpsUtils.crawlingFromWebWithoutJS(url, ENCODE);
        if(result == null || result.length() < 1) {
            return null;
        }

        int start = result.indexOf(CONTENT_FLAG);
        if(start < 0) {
            return null;
        }

        StringBuffer head = new StringBuffer();
        head.append(result.substring(0, start));
        parseTitle(head);

        result.delete(0, start + CONTENT_FLAG.length());
        int end = result.indexOf(url) - 1;
        if(end < 0) {
            return null;
        }

        StringBuffer tail = new StringBuffer();
        tail.append(result.substring(end));
        result.delete(end, result.length());

        parsePreUrl(tail);
        parseNextUrl(tail);

        return formatContent(result);
    }

    /**
     * parse pre chapter of web page form url
     *
     * called after for method @See{parseWebPage}
     * */
    public String getPreChapter() {
        if(mPreUrl == null) {
            return null;
        }
        return parseWebPage(DEFAULT_URL + mPreUrl);
    }

    /**
     * parse next chapter of web page form url
     *
     * called after for method @See{parseWebPage}
     * */
    public String getNextChapter() {
        if(mNextUrl == null) {
            return null;
        }
        Log.i(TAG, "mNextUrl:" + mNextUrl);
        return parseWebPage(DEFAULT_URL + mNextUrl);
    }

}
