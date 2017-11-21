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
import com.leocardz.link.preview.library.TextCrawler;

import ca.pet.dejavu.Data.DBService;
import ca.pet.dejavu.Presenter.MainPresenter;
import ca.pet.dejavu.R;

public class MainActivity extends AppCompatActivity implements IMainView {

    private MainPresenter mainPresenter = null;

    private ProgressDialog progressDialog = null;
    private TextCrawler textCrawler = null;

    private ContentAdapter adapter = null;

    private FloatingActionButton sendButton = null;
    private TextView noContentText = null;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBService service = DBService.getInstance();
        service.init(getApplicationContext());
        mainPresenter = MainPresenter.getInstance(this);


        Toolbar toolbar = findViewById(R.id.toolbar_main);

        toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        toolbar.inflateMenu(R.menu.menu_toolbar_main);
        toolbar.setNavigationOnClickListener(onNavigationIconClick);
        toolbar.setOnMenuItemClickListener(onToolBarItemClick);
        SearchView searchView = (SearchView) toolbar.getMenu().findItem(R.id.menu_item_search).getActionView();
        searchView.setOnQueryTextListener(mainPresenter);

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

    @Override
    protected void onStart() {
        super.onStart();
        //        mainPresenter.addNewUrl("asdfasf","https://youtu.be/dv13gl0a-FA");
        mainPresenter.queryAll();
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                String title = intent.getStringExtra(Intent.EXTRA_TITLE);
                if (title == null) {
                    title = "";
                }
                mainPresenter.addNewUrl(title, text);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (textCrawler != null) {
            textCrawler.cancel();
        }
        System.exit(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    private View.OnClickListener onSendClick = (View v)
            -> mainPresenter.onSendClick((String) v.getTag());

    private View.OnClickListener onNavigationIconClick = (v) -> openDrawer();

    private Toolbar.OnMenuItemClickListener onToolBarItemClick = (item) -> {
        int itemId = item.getItemId();

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
            case R.id.menu_item_search:

                Transition changeBounds = new ChangeBounds();
                changeBounds.setDuration(100);
                Transition fade_in = new Fade(Fade.IN);
                fade_in.setDuration(100);
                TransitionSet transitionSet = new TransitionSet();
                transitionSet.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);
                transitionSet.addTransition(changeBounds).addTransition(fade_in);

                TransitionManager.beginDelayedTransition(findViewById(R.id.toolbar_main), transitionSet);
                item.expandActionView();
                break;
        }
        return true;
    };

    private void openDrawer() {
        ((DrawerLayout) findViewById(R.id.main_drawer)).openDrawer(Gravity.START);
    }

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

    @Override
    public void showMessageDialog(ShareLinkContent content) {
        MessageDialog.show(this, content);
    }

    @Override
    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void notifyItemRemoved(int position) {
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void notifyItemChanged(int position) {
        adapter.notifyItemChanged(position);
    }

    @Override
    public void showProgress() {
        dismissProgress();
        progressDialog = ProgressDialog.show(this, getString(R.string.title_progress_loading), getString(R.string.msg_progress_parsing_url));
    }

    @Override
    public void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

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

    @Override
    public void displayNoContentTextView(boolean display) {
        if (display) {
            noContentText.setVisibility(View.VISIBLE);
        } else {
            noContentText.setVisibility(View.GONE);
        }
    }
}
