package ca.pet.dejavu.View.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.pet.dejavu.Presenter.IMainPresenter;
import ca.pet.dejavu.R;
import ca.pet.dejavu.View.ContentAdapter;

/**
 * Created by CAHSIEH on 2017/12/7.
 */

public class ContentFragment extends BaseFragment {

    private static ContentFragment instance;

    private IMainPresenter mainPresenter = null;

    private FloatingActionButton sendButton = null;

    private ContentAdapter adapter;

    public ContentFragment() {
        fragmentTag = "Content";
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
        return inflater.inflate(R.layout.content_layout_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.list_content);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        sendButton = view.findViewById(R.id.fab_d);
        sendButton.setOnClickListener(onSendClick);
    }

    /**
     * 傳送按鈕的觸發事件
     * 使用mainPresenter.onSendClick來進行後續處理
     */
    private View.OnClickListener onSendClick = (View v)
            -> mainPresenter.onSendClick((String) v.getTag());

    public void setAdapter(ContentAdapter adapter) {
        this.adapter = adapter;
    }

    public void setPresenter(IMainPresenter mainPresenter) {
        this.mainPresenter = mainPresenter;
    }

    public void setSendPlatform(int id) {
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
