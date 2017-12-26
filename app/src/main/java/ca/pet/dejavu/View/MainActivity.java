package ca.pet.dejavu.View;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.MessageDialog;

import java.util.List;

import ca.pet.dejavu.Presenter.IMainPresenter;
import ca.pet.dejavu.Presenter.MainPresenter;
import ca.pet.dejavu.R;
import ca.pet.dejavu.Utils.MyApplication;
import ca.pet.dejavu.Utils.SPConst;
import ca.pet.dejavu.View.Fragment.BaseFragment;
import ca.pet.dejavu.View.Fragment.ContentFragment;

/**
 * View
 * 內容不可包含Model的使用
 * 僅透過Presenter做交流
 */
public class MainActivity extends AppCompatActivity implements IMainView {

    private IMainPresenter mainPresenter = null;

    private BaseFragment currentFragment;

    private Intent newDataIntent;
    private ContentAdapter adapter = null;

    private ProgressDialog progressDialog = null;
    private Toolbar toolbar;
    private SearchView searchView;
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
        Intent intent = newDataIntent == null ? getIntent() : newDataIntent;
        setIntent(null);
        if (null == intent || intent.getAction() == null || !intent.getAction().equals(Intent.ACTION_SEND)) {
            mainPresenter.queryAll();
        } else {
            //newDataIntent只有在先開啟過再分享進來時才會有內容
            //newDataIntent無內容的話可能代表是第一次分享進來，此時getIntent會有內容。
            //若兩者皆沒內容，代表是直接開啟App
            String action = intent.getAction();
            String type = intent.getType(); //傳入intent的mime type
            if (Intent.ACTION_SEND.equals(action) && type != null) {

                if ("text/plain".equals(type)) {
                    mainPresenter.setQueryType(SPConst.VISIBLE_TYPE_LINK);

                    String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                    String title = intent.getStringExtra(Intent.EXTRA_TITLE);
                    if (title == null) {
                        title = "";
                    }
                    newDataIntent = null;
                    mainPresenter.addNewUrl(title, text);
                } else if (type.startsWith("image/")) {
                    mainPresenter.setQueryType(SPConst.VISIBLE_TYPE_IMAGE);

                    Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    newDataIntent = null;
                    mainPresenter.addNewImage(uri);
                }
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
                if (type.startsWith("image/")) {
                    mainPresenter.setQueryType(SPConst.VISIBLE_TYPE_IMAGE);

                    List<Uri> uriList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    newDataIntent = null;
                    mainPresenter.addNewImage(uriList.toArray(new Uri[uriList.size()]));
                }
            }
        }

        NavigationView navigationView = findViewById(R.id.main_navigation);
        navigationView.setNavigationItemSelectedListener(onNavigationItemSelected);
        if (SPConst.VISIBLE_TYPE_LINK == MyApplication.currentVisibleType) {
            navigationView.getMenu().findItem(R.id.navItem_url).setChecked(true);
        } else {
            navigationView.getMenu().findItem(R.id.navItem_image).setChecked(true);
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
        if (currentFragment.equals(ContentFragment.getInstance())) {
            ContentFragment.getInstance().setNoContentTextIsDisplay(display);
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
        mainPresenter.queryAll();
    }

    /**
     * 初始化UI元件。
     */
    private void initUI() {
        toolbar = findViewById(R.id.toolbar_main);
        toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        toolbar.inflateMenu(R.menu.menu_toolbar_main);
        toolbar.setNavigationOnClickListener(onNavigationIconClick);
        toolbar.setOnMenuItemClickListener(onToolBarItemClick);
        searchView = (SearchView) toolbar.getMenu().findItem(R.id.menu_item_search).getActionView();
        searchView.setOnQueryTextListener((MainPresenter) mainPresenter);

        SharedPreferences preferences = MyApplication.getSharedPreferences();

        int currentVisibleType = preferences.getInt(SPConst.SP_FIELD_START_TYPE, SPConst.VISIBLE_TYPE_LINK);
        mainPresenter.setQueryType(currentVisibleType);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        adapter = new ContentAdapter(mainPresenter);
        ContentFragment contentFragment = ContentFragment.getInstance();
        contentFragment.setAdapter(adapter);
        contentFragment.setPresenter(mainPresenter);
        currentFragment = contentFragment;
        transaction.add(R.id.main_container, currentFragment, currentFragment.getFragmentTag());
        transaction.commit();
    }

    /**
     * 開啟抽屜
     */
    private void openDrawer() {
        ((DrawerLayout) findViewById(R.id.main_drawer)).openDrawer(Gravity.START);
    }

    /**
     * NavigationIcon被點擊時的觸發事件，
     * 觸發反應為openDrawer。
     */
    private View.OnClickListener onNavigationIconClick = (v) -> openDrawer();

    /**
     * NavigationView項目的點擊事件
     */
    private NavigationView.OnNavigationItemSelectedListener onNavigationItemSelected = menuItem -> {

        //若SearchView為開啟狀態，將其清除。
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            toolbar.getMenu().findItem(R.id.menu_item_search).collapseActionView();
        }

        switch (menuItem.getItemId()) {
            case R.id.navItem_url:
                mainPresenter.setQueryType(SPConst.VISIBLE_TYPE_LINK);
                mainPresenter.queryAll();
                break;
            case R.id.navItem_image:
                mainPresenter.setQueryType(SPConst.VISIBLE_TYPE_IMAGE);
                mainPresenter.queryAll();
                break;
            case R.id.navItem_setting:
                showSnack("navItem_setting");
                break;
        }
        return true;
    };

    /**
     * ToolBar上按鈕或其內容點擊時的觸發事件。
     */
    private Toolbar.OnMenuItemClickListener onToolBarItemClick = (item) -> {
        int itemId = item.getItemId();

        //判斷不同的Item
        switch (itemId) {
            case R.id.menu_item_messenger:
                ContentFragment.getInstance().setSendPlatform(itemId);
                showSnack(getString(R.string.snack_message_change_app_messenger));
                break;
            case R.id.menu_item_line:
                ContentFragment.getInstance().setSendPlatform(itemId);
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
