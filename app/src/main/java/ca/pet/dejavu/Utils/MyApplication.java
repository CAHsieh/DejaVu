package ca.pet.dejavu.Utils;

import android.app.Application;
import android.content.Context;

/**
 * Created by CAMac on 2017/11/21.
 */

public class MyApplication extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
