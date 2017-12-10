package ca.pet.dejavu.View.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by CAHSIEH on 2017/12/7.
 */

public class BaseFragment extends Fragment {

    protected String fragmentTag;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public String getFragmentTag() {
        return fragmentTag;
    }
}
