package at.yawk.fimfiction.android;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * @author Yawkat
 */
public class Connectivity {
    public static boolean bigDownloads(Context context) {
        return !((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).isActiveNetworkMetered();
    }
}
