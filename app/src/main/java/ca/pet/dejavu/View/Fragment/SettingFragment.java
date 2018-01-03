package ca.pet.dejavu.View.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import ca.pet.dejavu.R;
import ca.pet.dejavu.Utils.MyApplication;
import ca.pet.dejavu.Utils.SPConst;

/**
 * Created by CAHSIEH on 2017/12/7.
 * 設定頁面
 */

public class SettingFragment extends BaseFragment {

    private static SettingFragment instance;

    public SettingFragment() {
        fragmentTag = SPConst.FRAGMENT_TAG_SETTING;
    }

    synchronized public static SettingFragment getInstance() {
        if (null == instance) {
            synchronized (SettingFragment.class) {
                if (null == instance) {
                    instance = new SettingFragment();
                }
            }
        }
        return instance;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences preferences = MyApplication.getSharedPreferences();

        int currentLaunchPage = preferences.getInt(SPConst.SP_FIELD_DEFAULT_LAUNCH_PAGE, SPConst.VISIBLE_TYPE_LINK);
        int currentDefaultPlatform = preferences.getInt(SPConst.SP_FIELD_DEFAULT_SEND_PLATFORM, R.id.menu_item_messenger);

        SwitchCompat pageSwitch = view.findViewById(R.id.setting_sw_launch_page);
        SwitchCompat platformSwitch = view.findViewById(R.id.setting_sw_send_platform);

        pageSwitch.setChecked(currentLaunchPage == SPConst.VISIBLE_TYPE_IMAGE);
        platformSwitch.setChecked(currentDefaultPlatform == R.id.menu_item_messenger);

        pageSwitch.setOnCheckedChangeListener(onPageCheckChange);
        platformSwitch.setOnCheckedChangeListener(onPlatformCheckChange);
    }

    private CompoundButton.OnCheckedChangeListener onPageCheckChange = (v, isChecked) -> {
        SharedPreferences.Editor editor = MyApplication.getSharedPreferences().edit();
        if (isChecked) {
            editor.putInt(SPConst.SP_FIELD_DEFAULT_LAUNCH_PAGE, SPConst.VISIBLE_TYPE_IMAGE);
        } else {
            editor.putInt(SPConst.SP_FIELD_DEFAULT_LAUNCH_PAGE, SPConst.VISIBLE_TYPE_LINK);
        }
        editor.apply();
    };

    private CompoundButton.OnCheckedChangeListener onPlatformCheckChange = (v, isChecked) -> {
        SharedPreferences.Editor editor = MyApplication.getSharedPreferences().edit();
        if (isChecked) {
            editor.putInt(SPConst.SP_FIELD_DEFAULT_SEND_PLATFORM, R.id.menu_item_messenger);
        } else {
            editor.putInt(SPConst.SP_FIELD_DEFAULT_SEND_PLATFORM, R.id.menu_item_line);
        }
        editor.apply();
    };
}
