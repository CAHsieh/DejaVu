package ca.pet.dejavu.Utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.lang.ref.WeakReference;

/**
 * Created by CAMac on 2017/11/21.
 * extend Application, used to let other class get application context easily.
 */

public class MyApplication extends Application {

    private static WeakReference<Context> mContext;

    public static int currentVisibleType;

    public static Context getContext() {
        assert mContext != null;
        return mContext.get();
    }

    public static SharedPreferences getSharedPreferences() {
        assert mContext != null;
        return mContext.get().getSharedPreferences(SPConst.SP_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = new WeakReference<>(getApplicationContext());
    }
}
