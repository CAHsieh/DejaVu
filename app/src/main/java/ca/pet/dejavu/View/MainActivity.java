package ca.pet.dejavu.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.MessageDialog;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import ca.pet.dejavu.Data.DBService;
import ca.pet.dejavu.Data.LinkEntity;
import ca.pet.dejavu.Model.LinkEntityModel;
import ca.pet.dejavu.R;
import ca.pet.dejavu.Utils.LinkEntityEvent;

public class MainActivity extends AppCompatActivity implements ContentAdapter.OnItemActionListener,
        SearchView.OnQueryTextListener {

    private static final String DEJAVU_URL = "https://youtu.be/dv13gl0a-FA";
    private static final String LOG = "DejaVu";
    private static final String TAG_MESSENGER = "Messenger";
    private static final String TAG_LINE = "Line";

    private LinkEntity newData = null;
    private LinkEntity currentSelectLink = null;

    private ProgressDialog progressDialog = null;
    private TextCrawler textCrawler = null;

    private LinkEntityModel entityModel = null;
    private ContentAdapter adapter = null;

    private FloatingActionButton sendButton = null;
    private TextView noContentText = null;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);

        toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        toolbar.inflateMenu(R.menu.menu_toolbar_main);
        toolbar.setNavigationOnClickListener(onNavigationIconClick);
        toolbar.setOnMenuItemClickListener(onToolBarItemClick);
        SearchView searchView = (SearchView) toolbar.getMenu().findItem(R.id.menu_item_search).getActionView();
        searchView.setOnQueryTextListener(this);

        sendButton = findViewById(R.id.fab_d);
        sendButton.setOnClickListener(onSendClick);
        noContentText = findViewById(R.id.txt_no_content);

        DBService service = DBService.getInstance();
        service.init(getApplicationContext());
        entityModel = LinkEntityModel.getInstance();

        adapter = new ContentAdapter();
        adapter.setOnItemActionListener(this);
        RecyclerView recyclerView = findViewById(R.id.list_content);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                String title = intent.getStringExtra(Intent.EXTRA_TITLE);

                newData = new LinkEntity();
                newData.setLink(text);
                newData.setTitle(title);
                entityModel.doAction(LinkEntityModel.ACTION_INSERT, newData, null);

                if (0 == text.indexOf("https://youtu.be") || 0 == text.indexOf("https://www.youtube.com/")) {
                    progressDialog = ProgressDialog.show(this, getString(R.string.title_progress_loading), getString(R.string.msg_progress_parsing_url));
                    String ytDesUrl = "https://www.youtube.com/oembed?url=" + text + "&format=json";
                    JsonObjectRequest ytDesReq = new JsonObjectRequest(ytDesUrl, successListener, errorListener);
                    Volley.newRequestQueue(this).add(ytDesReq);
                } else if (text.contains("http://") || text.contains("https://")) {
                    String url = text.substring(text.indexOf("http"));
                    newData.setLink(url);
                    textCrawler = new TextCrawler();
                    textCrawler.makePreview(linkPreviewCallback, url);
                } else {
                    adapter.notifyDataSetChanged();
                    TitleDialog titleDialog = new TitleDialog(newData);
                    titleDialog.setOnTitleActionCallback(adapter);
                    titleDialog.show(this);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null)
            progressDialog.dismiss();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textCrawler != null) {
            textCrawler.cancel();
        }
    }

    @Override
    public void OnLinkSelected(LinkEntity entity) {
        if (entity.equals(currentSelectLink)) {
            currentSelectLink = null;
            return;
        }
        currentSelectLink = entity;
    }

    @Override
    public void OnLinkDelete(LinkEntity entity) {
        if (null != currentSelectLink && currentSelectLink.equals(entity))
            currentSelectLink = null;

        if (entityModel.table_size() == 1)
            noContentText.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnTitleModifyClick(LinkEntity entity) {
        TitleDialog titleDialog = new TitleDialog(entity);
        titleDialog.setOnTitleActionCallback(adapter);
        titleDialog.show(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLinkEntityEvent(LinkEntityEvent event) {
        int action_Id = event.getAction_Id();
        int tag = event.getTag();

        switch (action_Id) {
            case LinkEntityModel.ACTION_QUERYALL:
                if (entityModel.getPresenting_list().size() > 0)
                    noContentText.setVisibility(View.GONE);
                //fall through
            case LinkEntityModel.ACTION_QUERYBYTITLE:
                adapter.notifyDataSetChanged();
                break;
            case LinkEntityModel.ACTION_DELETE:
                if (-1 != tag) {
                    adapter.notifyItemRemoved(tag);
                } else {
                    adapter.notifyDataSetChanged();
                }
                break;
            case LinkEntityModel.ACTION_UPDATE:
                if (-1 != tag) {
                    adapter.notifyItemChanged(tag);
                } else {
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //need not to use
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        new Thread() {
            @Override
            public void run() {
                entityModel.doAction(LinkEntityModel.ACTION_QUERYBYTITLE, null, newText);
            }
        }.start();
        return false;
    }

    private LinkPreviewCallback linkPreviewCallback = new LinkPreviewCallback() {
        @Override
        public void onPre() {
            progressDialog = ProgressDialog.show(MainActivity.this, getString(R.string.title_progress_loading), getString(R.string.msg_progress_parsing_url));
        }

        @Override
        public void onPos(SourceContent sourceContent, boolean b) {
            if (progressDialog != null)
                progressDialog.dismiss();
            String imgUrl = null;
            for (String img : sourceContent.getImages()) {
                String[] splitStr = img.split("\\.");
                if (splitStr.length > 0
                        && (splitStr[splitStr.length - 1].equals("png")
                        || splitStr[splitStr.length - 1].equals("jpg")
                        || splitStr[splitStr.length - 1].equals("jpeg"))) {
                    imgUrl = img;
                    break;
                }
            }

            Log.i(LOG, "title: " + sourceContent.getTitle() + "  thumbnail_url: " + imgUrl);
            newData.setTitle(sourceContent.getTitle());
            newData.setThumbnailUrl(imgUrl);

            new Thread() {
                @Override
                public void run() {
                    entityModel.doAction(LinkEntityModel.ACTION_UPDATE, newData, null);
                }
            }.start();
        }
    };

    private View.OnClickListener onSendClick = (v) -> {
        String url = currentSelectLink == null ? DEJAVU_URL : currentSelectLink.getLink();

        if (v.getTag() == null || v.getTag().equals(TAG_MESSENGER)) {
            //share to messenger
            if (isAppInstalled(getString(R.string.package_name_messenger))) {
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(url))
                        .build();
                MessageDialog.show(this, content);
            } else {
                showSnack(getString(R.string.snack_message_not_installed_messenger));
            }
        } else {
            //share to line
            if (isAppInstalled(getString(R.string.package_name_line))) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setPackage("jp.naver.line.android");
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, url);
                startActivity(intent);
            } else {
                showSnack(getString(R.string.snack_message_not_installed_line));
            }
        }
    };

    private Response.Listener<JSONObject> successListener = (response) -> {
        if (progressDialog != null)
            progressDialog.dismiss();
        try {
            Log.i(LOG, "title: " + response.getString("title") + "  thumbnail_url: " + response.getString("thumbnail_url"));
            newData.setTitle(response.getString("title"));
            newData.setThumbnailUrl(response.getString("thumbnail_url"));

            new Thread() {
                @Override
                public void run() {
                    entityModel.doAction(LinkEntityModel.ACTION_UPDATE, newData, null);
                }
            }.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    private Response.ErrorListener errorListener = (error) -> {
        if (progressDialog != null)
            progressDialog.dismiss();
        Log.e(LOG, "JSONObject Update Error.");
        TitleDialog titleDialog = new TitleDialog(newData);
        titleDialog.setOnTitleActionCallback(adapter);
        titleDialog.show(MainActivity.this);
    };

    private View.OnClickListener onNavigationIconClick = (v) -> openDrawer();

    private Toolbar.OnMenuItemClickListener onToolBarItemClick = (item) -> {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.menu_item_messenger:
                sendButton.setImageResource(R.drawable.ic_action_send_messenger);
                sendButton.setTag(TAG_MESSENGER);
                showSnack(getString(R.string.snack_message_change_app_messenger));
                break;
            case R.id.menu_item_line:
                sendButton.setImageResource(R.drawable.ic_action_send_line);
                sendButton.setTag(TAG_LINE);
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

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(LOG, packageName + " is not installed.");
        }
        return false;
    }

    private void showSnack(String message) {
        if (snackbar == null) {
            snackbar = Snackbar
                    .make(findViewById(R.id.list_content), message, Snackbar.LENGTH_SHORT);
        }
        if (!snackbar.isShown()) {
            snackbar.setText(message);
            snackbar.show();
        }
    }
}
