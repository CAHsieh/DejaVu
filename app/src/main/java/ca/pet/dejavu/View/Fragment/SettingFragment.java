package ca.pet.dejavu.View.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.pet.dejavu.R;
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
    }

}
