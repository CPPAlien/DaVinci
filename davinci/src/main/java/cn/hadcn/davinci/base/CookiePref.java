package cn.hadcn.davinci.base;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * CookiePref for saving cookie
 * Created by 90Chris on 2015/12/13.
 */
public class CookiePref {
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor editor;
    private static CookiePref sCookiePref = null;

    public static CookiePref getInstance(Context context) {
        if ( sCookiePref == null ) {
            sCookiePref = new CookiePref(context);
        }
        return sCookiePref;
    }

    private CookiePref(Context context) {
        mPrefs = context.getSharedPreferences("CookiePref", Context.MODE_PRIVATE);
        editor = mPrefs.edit();
    }

    public void saveCookie(String cookie) {
        editor.putString("Cookie", cookie);
        editor.apply();
    }

    public String getCookie() {
        return mPrefs.getString("Cookie", null);
    }
}
