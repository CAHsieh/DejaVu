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

    private static WeakReference<MyApplication> mApplication;

    public static int currentVisibleType;

    public static Context getContext() {
        assert mApplication != null;
        MyApplication application = mApplication.get();
        return application.getApplicationContext();
    }

    public static SharedPreferences getSharedPreferences() {
        assert mApplication != null;
        Context context = mApplication.get().getApplicationContext();
        return context.getSharedPreferences(SPConst.SP_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = new WeakReference<>(this);
    }
}
