package ca.pet.dejavu.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.MessageDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ca.pet.dejavu.Model.DBService;
import ca.pet.dejavu.Model.LinkEntity;
import ca.pet.dejavu.Model.LinkEntityDao;
import ca.pet.dejavu.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ContentAdapter.OnLinkActionListener {

    private static final String dejavu_url = "https://youtu.be/dv13gl0a-FA";
    private static final String LOG = "DejaVu";
    private static final String TAG_MESSENGER = "Messenger";
    private static final String TAG_LINE = "Line";

    private LinkEntity newData = null;
    private LinkEntity currentSelectLink = null;
    private ContentAdapter adapter = null;

    private FloatingActionButton sendButton = null;
    private TextView noContentText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);

        toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        toolbar.inflateMenu(R.menu.menu_toolbar_main);
        toolbar.setNavigationOnClickListener(onNavigationIconClick);
        toolbar.setOnMenuItemClickListener(onToolBarItemClick);

        sendButton = (FloatingActionButton) findViewById(R.id.fab_d);
        sendButton.setOnClickListener(this);
        noContentText = (TextView) findViewById(R.id.txt_no_content);

        DBService service = DBService.getInstance();
        service.init(getApplicationContext());
        LinkEntityDao linkEntityDao = service.getLinkEntityDao();
        List<LinkEntity> currentList = linkEntityDao.loadAll();

        adapter = new ContentAdapter(this, currentList);
        adapter.setOnLinkActionListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_content);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

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
                linkEntityDao.insert(newData);
                currentList.add(newData);

                if (0 == text.indexOf("https://youtu.be") || 0 == text.indexOf("https://www.youtube.com/")) {
                    String ytDesUrl = "https://www.youtube.com/oembed?url=" + text + "&format=json";
                    JsonObjectRequest ytDesReq = new JsonObjectRequest(ytDesUrl, successListener, errorListener);
                    Volley.newRequestQueue(this).add(ytDesReq);
                } else {
                    adapter.notifyDataSetChanged();
                    TitleDialog titleDialog = new TitleDialog(this, newData);
                    titleDialog.setOnTitleActionCallback(adapter);
                    titleDialog.show();
                }
            }
        }
        if (currentList.size() > 0)
            noContentText.setVisibility(View.GONE);
    }

    private View.OnClickListener onNavigationIconClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openDrawer();
        }
    };

    private void openDrawer() {
        ((DrawerLayout) findViewById(R.id.main_drawer)).openDrawer(Gravity.START);
    }

    private Toolbar.OnMenuItemClickListener onToolBarItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int itemId = item.getItemId();

            switch (itemId) {
                case R.id.menu_item_messenger:
                    sendButton.setImageResource(R.drawable.ic_action_send_messenger);
                    sendButton.setTag(TAG_MESSENGER);
                    break;
                case R.id.menu_item_line:
                    sendButton.setImageResource(R.drawable.ic_action_send_line);
                    sendButton.setTag(TAG_LINE);
                    break;
            }
            return true;
        }
    };

    @Override
    public void onClick(View v) {

        if (v.getTag() == null || v.getTag().equals(TAG_MESSENGER)) {

            String url = currentSelectLink == null ? dejavu_url : currentSelectLink.getLink();

            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse(url))
                    .build();
            MessageDialog.show(this, content);
        } else {

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

        if (adapter.getItemCount() == 1)
            noContentText.setVisibility(View.VISIBLE);
    }

    private Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                Log.i(LOG, "title: " + response.getString("title") + "  thumbnail_url: " + response.getString("thumbnail_url"));
                newData.setTitle(response.getString("title"));
                newData.setThumbnailUrl(response.getString("thumbnail_url"));
                DBService.getInstance().getLinkEntityDao().update(newData);
                adapter.notifyItemChanged(adapter.getItemCount() - 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(LOG, "JSONObject Update Error.");
            TitleDialog titleDialog = new TitleDialog(MainActivity.this, newData);
            titleDialog.setOnTitleActionCallback(adapter);
            titleDialog.show();
        }
    };

//    private class YTasync extends AsyncTask<String, Void, String> {
//
//        ProgressDialog progressDialog = null;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            progressDialog = ProgressDialog.show(MainActivity.this, "Loading...", "requesting for title...", false, false);
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            if (null != progressDialog && progressDialog.isShowing())
//                progressDialog.dismiss();
//        }
//    }
}
