package ca.pet.dejavu.View.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import java.lang.ref.WeakReference;

import ca.pet.dejavu.Presenter.IMainPresenter;
import ca.pet.dejavu.R;
import ca.pet.dejavu.Utils.MyApplication;
import ca.pet.dejavu.Utils.SPConst;
import ca.pet.dejavu.View.ContentAdapter;

/**
 * Created by CAHSIEH on 2017/12/7.
 * 主內容Fragment，
 * 使用單例模式。
 */

public class ContentFragment extends BaseFragment {

    private static ContentFragment instance;

    private IMainPresenter mainPresenter = null;

    private WeakReference<View> mainView;
    private ContentAdapter adapter;

    public ContentFragment() {
        fragmentTag = SPConst.FRAGMENT_TAG_CONTENT;
    }

    synchronized public static ContentFragment getInstance() {
        if (null == instance) {
            synchronized (ContentFragment.class) {
                if (null == instance) {
                    instance = new ContentFragment();
                }
            }
        }
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainView = new WeakReference<>(view);

        RecyclerView recyclerView = view.findViewById(R.id.list_content);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.fab_d).setOnClickListener(onSendClick);

        SharedPreferences preferences = MyApplication.getSharedPreferences();
        int currentVisibleType = preferences.getInt(SPConst.SP_FIELD_DEFAULT_SEND_PLATFORM, R.id.menu_item_messenger);
        setFAB(currentVisibleType, view.findViewById(R.id.fab_d));
    }

    /**
     * 傳送按鈕的觸發事件
     * 使用mainPresenter.onSendClick來進行後續處理
     */
    private View.OnClickListener onSendClick = (View v) -> {
        boolean isSuccess = mainPresenter.onSendClick((String) v.getTag());
        if (isSuccess) {
            adapter.reset();
            adapter.notifyDataSetChanged();
        }
    };

    public void setAdapter(ContentAdapter adapter) {
        this.adapter = adapter;
    }

    public void setPresenter(IMainPresenter mainPresenter) {
        this.mainPresenter = mainPresenter;
    }

    public void setSendPlatform(int id) {
        View view = mainView.get();
        if (null != view) {
            setFAB(id, view.findViewById(R.id.fab_d));
        }
    }

    public void setNoContentTextIsDisplay(boolean display) {
        View view = mainView.get();
        if (null != view) {

            ViewStub viewStub = view.findViewById(R.id.viewstub_nocontent);

            if (viewStub != null && display) {
                //尚未被加載過
                viewStub.inflate();
            } else if (viewStub == null) {
                if (display) {
                    view.findViewById(R.id.txt_no_content).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.txt_no_content).setVisibility(View.GONE);
                }
            }
        }
    }

    private void setFAB(int id, FloatingActionButton sendButton) {
        switch (id) {
            case R.id.menu_item_messenger:
                sendButton.setImageResource(R.drawable.ic_action_send_messenger);
                sendButton.setTag(getString(R.string.tag_messenger));
                break;
            case R.id.menu_item_line:
                sendButton.setImageResource(R.drawable.ic_action_send_line);
                sendButton.setTag(getString(R.string.tag_line));
                break;
        }
    }
}
