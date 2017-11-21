package ca.pet.dejavu.Presenter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.webkit.URLUtil;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.share.model.ShareLinkContent;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;

import org.json.JSONException;
import org.json.JSONObject;

import ca.pet.dejavu.Data.LinkEntity;
import ca.pet.dejavu.Model.ILinkModel;
import ca.pet.dejavu.Model.LinkEntityModel;
import ca.pet.dejavu.R;
import ca.pet.dejavu.View.IMainView;
import ca.pet.dejavu.View.MainActivity;

/**
 * Created by CAMac on 2017/11/20.
 */

public class MainPresenter implements IMainPresenter {

    private static final String LOG_TAG = "DejaVu";
    private static final String DEJAVU_URL = "https://youtu.be/dv13gl0a-FA";

    private static MainPresenter instance = null;

    private IMainView mainView;
    private ILinkModel linkModel;

    private LinkEntity newData = null;
    private LinkEntity currentSelectLink = null;
    private LinkEntity editTitleLink = null;

    private MainPresenter(IMainView mainView) {
        this.mainView = mainView;
        linkModel = new LinkEntityModel(this);
    }

    public synchronized static MainPresenter getInstance(IMainView mainView) {
        if (instance == null) {
            synchronized (MainPresenter.class) {
                if (instance == null) {
                    instance = new MainPresenter(mainView);
                }
            }
        }
        return instance;
    }

    @Override
    public void queryAll() {
        linkModel.doAction(LinkEntityModel.ACTION_QUERYALL, null, "");
    }

    @Override
    public void addNewUrl(String title, String url) {
        newData = new LinkEntity();
        newData.setLink(url);
        newData.setTitle(title);
        linkModel.doAction(LinkEntityModel.ACTION_INSERT, newData, null);

        if (0 == url.indexOf("https://youtu.be") || 0 == url.indexOf("https://www.youtube.com/")) {
            mainView.showProgress();
            String ytDesUrl = "https://www.youtube.com/oembed?url=" + url + "&format=json";
            JsonObjectRequest ytDesReq = new JsonObjectRequest(ytDesUrl, successListener, errorListener);
            Volley.newRequestQueue((MainActivity) mainView).add(ytDesReq);
        } else if (URLUtil.isValidUrl(url)) {
            url = url.substring(url.indexOf("http"));
            newData.setLink(url);
            TextCrawler textCrawler = new TextCrawler();
            textCrawler.makePreview(linkPreviewCallback, url);
        } else {
            mainView.notifyDataSetChanged();
            mainView.showTitleDialog(title);
        }
    }

    @Override
    public void editTitle(String title) {
        editTitleLink.setTitle(title);
        linkModel.doAction(LinkEntityModel.ACTION_UPDATE, editTitleLink, null);
        editTitleLink = null;
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

        if (linkModel.table_size() == 1) {
            mainView.displayNoContentTextView(true);
        }

        linkModel.doAction(LinkEntityModel.ACTION_DELETE, entity, null);
    }

    @Override
    public void OnTitleModifyClick(LinkEntity entity) {
        editTitleLink = entity;
        mainView.showTitleDialog(entity.getTitle());
    }

    @Override
    public LinkEntity getEntity(int position) {
        //todo 改為非回傳entity.
        return linkModel.getEntity(position);
    }

    @Override
    public int getPresentingSize() {
        return linkModel.presenting_size();
    }

    @Override
    public void afterDoAction(int actionId, int tag) {
        switch (actionId) {
            case LinkEntityModel.ACTION_INSERT:
                if (linkModel.table_size() > 0)
                    mainView.displayNoContentTextView(false);
                break;
            case LinkEntityModel.ACTION_QUERYALL:
                if (linkModel.table_size() > 0)
                    mainView.displayNoContentTextView(false);
                //fall through
            case LinkEntityModel.ACTION_QUERYBYTITLE:
                mainView.notifyDataSetChanged();
                break;
            case LinkEntityModel.ACTION_DELETE:
                if (-1 != tag) {
                    mainView.notifyItemRemoved(tag);
                } else {
                    mainView.notifyDataSetChanged();
                }
                break;
            case LinkEntityModel.ACTION_UPDATE:
                if (-1 != tag) {
                    mainView.notifyItemChanged(tag);
                } else {
                    mainView.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public SearchView.OnQueryTextListener getOnQueryTextListener() {
        return searchViewQueryListener;
    }

    @Override
    public void onSendClick(Context context, String tag) {
        assert context != null;
        context = context.getApplicationContext();
        String url = currentSelectLink == null ? DEJAVU_URL : currentSelectLink.getLink();

        if (tag == null || tag.equals(context.getString(R.string.tag_messenger))) {
            //share to messenger
            if (isAppInstalled(context, context.getString(R.string.package_name_messenger))) {
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(url))
                        .build();
                mainView.showMessageDialog(content);
            } else {
                mainView.showSnack(context.getString(R.string.snack_message_not_installed_messenger));
            }
        } else {
            //share to line
            if (isAppInstalled(context, context.getString(R.string.package_name_line))) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setPackage("jp.naver.line.android");
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, url);
                context.startActivity(intent);
            } else {
                mainView.showSnack(context.getString(R.string.snack_message_not_installed_line));
            }
        }
    }

    private boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, packageName + " is not installed.");
        }
        return false;
    }

    private Response.Listener<JSONObject> successListener = (response) -> {
        mainView.dismissProgress();
        try {
            Log.i(LOG_TAG, "title: " + response.getString("title") + "  thumbnail_url: " + response.getString("thumbnail_url"));
            newData.setTitle(response.getString("title"));
            newData.setThumbnailUrl(response.getString("thumbnail_url"));

            linkModel.doAction(LinkEntityModel.ACTION_UPDATE, newData, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    private Response.ErrorListener errorListener = (error) -> {
        mainView.dismissProgress();
        Log.e(LOG_TAG, "JSONObject Update Error.");
        mainView.showTitleDialog(null);
    };

    private LinkPreviewCallback linkPreviewCallback = new LinkPreviewCallback() {
        @Override
        public void onPre() {
            mainView.showProgress();
        }

        @Override
        public void onPos(SourceContent sourceContent, boolean b) {
            mainView.dismissProgress();
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

            Log.i(LOG_TAG, "title: " + sourceContent.getTitle() + "  thumbnail_url: " + imgUrl);
            newData.setTitle(sourceContent.getTitle());
            newData.setThumbnailUrl(imgUrl);

            linkModel.doAction(LinkEntityModel.ACTION_UPDATE, newData, null);
        }
    };


    private SearchView.OnQueryTextListener searchViewQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            //need not to use
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            linkModel.doAction(LinkEntityModel.ACTION_QUERYBYTITLE, null, newText);
            return false;
        }
    };
}
