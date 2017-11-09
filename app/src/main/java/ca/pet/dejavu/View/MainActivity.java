package ca.pet.dejavu.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

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

    private LinkEntity newData = null;
    private LinkEntity currentSelectLink = null;
    private ContentAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.fab_d).setOnClickListener(this);

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
    }

    @Override
    public void onClick(View v) {

        String url = currentSelectLink == null ? dejavu_url : currentSelectLink.getLink();

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(url))
                .build();
        MessageDialog.show(this, content);
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
