package ca.pet.dejavu.View;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.MessageDialog;

import ca.pet.dejavu.Presenter.IMainPresenter;
import ca.pet.dejavu.Presenter.MainPresenter;
import ca.pet.dejavu.R;

/**
 * View
 * 內容不可包含Model的使用
 * 僅透過Presenter做交流
 */
public class MainActivity extends AppCompatActivity implements IMainView {

    private IMainPresenter mainPresenter = null;

    private Intent newDataIntent;
    private ContentAdapter adapter = null;

    private ProgressDialog progressDialog = null;
    private FloatingActionButton sendButton = null;
    private TextView noContentText = null;
    private Snackbar snackbar;

    /**
     * 初始化presenter及UI元件
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainPresenter = new MainPresenter(this);//註冊Presenter
        initUI();
    }

    /**
     * 列出所有資料並判斷是否有新資料傳入，
     * 在onStart做避免要跑progressDialog時還沒完成畫面佈局。
     */
    @Override
    protected void onStart() {
        super.onStart();
        //        mainPresenter.addNewUrl("asdfasf","https://youtu.be/dv13gl0a-FA");
        mainPresenter.queryAll();

        //newDataIntent只有在先開啟過再分享進來時才會有內容
        //newDataIntent無內容的話可能代表是第一次分享進來，此時getIntent會有內容。
        //若兩者皆沒內容，代表是直接開啟App
        Intent intent = newDataIntent == null ? getIntent() : newDataIntent;
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType(); //傳入intent的mime type
            if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                String title = intent.getStringExtra(Intent.EXTRA_TITLE);
                if (title == null) {
                    title = "";
                }
                newDataIntent = null;
                mainPresenter.addNewUrl(title, text);
            }
        }
    }

    /**
     * launchMode使用singleTask
     * 當在開啟狀態有新的分享進來時
     * 會跑此段來接收新的Intent
     *
     * @param intent new intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        newDataIntent = intent;
    }

    /**
     * 檢查progressDialog狀態
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        mainPresenter.cancelTextCrawler();
    }

    /**
     * 顯示snack
     *
     * @param message 要顯示的訊息
     */
    @Override
    public void showSnack(String message) {
        if (snackbar == null) {
            snackbar = Snackbar
                    .make(findViewById(R.id.list_content), message, Snackbar.LENGTH_SHORT);
        }
        if (!snackbar.isShown()) {
            snackbar.setText(message);
            snackbar.show();
        }
    }

    /**
     * 顯示ProgressDialog
     */
    @Override
    public void showProgress() {
        dismissProgress(); //顯示前先確認關閉目前的ProgressDialog(若存在)
        progressDialog = ProgressDialog.show(this, getString(R.string.title_progress_loading), getString(R.string.msg_progress_parsing_url));
    }

    /**
     * 關閉ProgressDialog
     */
    @Override
    public void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    /**
     * 顯示修改標題用Dialog
     *
     * @param originTitle 原始標題
     */
    @Override
    public void showTitleDialog(String originTitle) {
        @SuppressLint("InflateParams") final View item = LayoutInflater.from(this).inflate(R.layout.dialog_edittitle, null);
        if (originTitle != null) {
            ((EditText) item.findViewById(R.id.dialog_edit_title)).setText(originTitle);
        }

        new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                .setView(item)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String title = ((EditText) item.findViewById(R.id.dialog_edit_title)).getText().toString();
                    mainPresenter.editTitle(title);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * 控制無內容TextView的顯示狀態
     *
     * @param display true為顯示，false為隱藏。
     */
    @Override
    public void displayNoContentTextView(boolean display) {
        if (display) {
            noContentText.setVisibility(View.VISIBLE);
        } else {
            noContentText.setVisibility(View.GONE);
        }
    }

    /**
     * 使用facebook sdk分享網頁資訊
     *
     * @param content ShareLinkContent
     */
    @Override
    public void showMessengerDialog(ShareLinkContent content) {
        MessageDialog.show(this, content);
    }

    /**
     * 更新recycleView
     */
    @Override
    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    /**
     * 更新recycleView
     * 移除用，根據傳入index值來使用有default動畫的方法
     */
    @Override
    public void notifyItemRemoved(int position) {
        adapter.notifyItemRemoved(position);
    }

    /**
     * 更新recycleView
     * 更新特定內容用，根據傳入index值來使用有default動畫的方法
     */
    @Override
    public void notifyItemChanged(int position) {
        adapter.notifyItemChanged(position);
    }

    @Override
    public void notifyInsertCompleted() {
        adapter.offsetSelectPosition();
    }

    /**
     * 初始化UI元件。
     */
    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar_main);

        toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        toolbar.inflateMenu(R.menu.menu_toolbar_main);
        toolbar.setNavigationOnClickListener(onNavigationIconClick);
        toolbar.setOnMenuItemClickListener(onToolBarItemClick);
        SearchView searchView = (SearchView) toolbar.getMenu().findItem(R.id.menu_item_search).getActionView();
        searchView.setOnQueryTextListener((MainPresenter) mainPresenter);

        sendButton = findViewById(R.id.fab_d);
        sendButton.setOnClickListener(onSendClick);
        noContentText = findViewById(R.id.txt_no_content);

        adapter = new ContentAdapter(mainPresenter);
        RecyclerView recyclerView = findViewById(R.id.list_content);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 開啟抽屜
     */
    private void openDrawer() {
        ((DrawerLayout) findViewById(R.id.main_drawer)).openDrawer(Gravity.START);
    }

    /**
     * 傳送按鈕的觸發事件
     * 使用mainPresenter.onSendClick來進行後續處理
     */
    private View.OnClickListener onSendClick = (View v)
            -> mainPresenter.onSendClick((String) v.getTag());

    /**
     * NavigationIcon被點擊時的觸發事件，
     * 觸發反應為openDrawer。
     */
    private View.OnClickListener onNavigationIconClick = (v) -> openDrawer();

    /**
     * ToolBar上按鈕或其內容點擊時的觸發事件。
     */
    private Toolbar.OnMenuItemClickListener onToolBarItemClick = (item) -> {
        int itemId = item.getItemId();

        //判斷不同的Item
        switch (itemId) {
            case R.id.menu_item_messenger:
                sendButton.setImageResource(R.drawable.ic_action_send_messenger);
                sendButton.setTag(getString(R.string.tag_messenger));
                showSnack(getString(R.string.snack_message_change_app_messenger));
                break;
            case R.id.menu_item_line:
                sendButton.setImageResource(R.drawable.ic_action_send_line);
                sendButton.setTag(getString(R.string.tag_line));
                showSnack(getString(R.string.snack_message_change_app_line));
                break;
            case R.id.menu_item_search: // 點擊SearchView

                //自訂SearchView場景變換的動畫
                //簡易版本。
                Transition changeBounds = new ChangeBounds();
                changeBounds.setDuration(100);
                Transition fade_in = new Fade(Fade.IN);
                fade_in.setDuration(100);
                TransitionSet transitionSet = new TransitionSet();
                transitionSet.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);
                transitionSet.addTransition(changeBounds).addTransition(fade_in);

                TransitionManager.beginDelayedTransition(findViewById(R.id.toolbar_main), transitionSet);
                item.expandActionView();//切換至輸入狀態。
                break;
        }
        return true;
    };
}
