package ca.pet.dejavu.Presenter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.webkit.URLUtil;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.share.model.ShareLinkContent;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import ca.pet.dejavu.Data.LinkEntity;
import ca.pet.dejavu.Model.ILinkModel;
import ca.pet.dejavu.Model.LinkEntityModel;
import ca.pet.dejavu.R;
import ca.pet.dejavu.Utils.MyApplication;
import ca.pet.dejavu.View.IMainView;

/**
 * Created by CAMac on 2017/11/20.
 * Presenter of MainActivity
 * only for MainActivity
 */

public class MainPresenter implements IMainPresenter, SearchView.OnQueryTextListener,
        Response.Listener<JSONObject>, Response.ErrorListener,
        LinkPreviewCallback {

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
        new ActionTask(this, LinkEntityModel.ACTION_QUERYALL, null, "").execute(linkModel);
    }

    @Override
    public void addNewUrl(String title, String url) {
        newData = new LinkEntity();
        newData.setLink(url);
        newData.setTitle(title);
        new ActionTask(this, LinkEntityModel.ACTION_INSERT, newData, null).execute(linkModel);
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
    public void editTitle(String title) {
        editTitleLink.setTitle(title);
        new ActionTask(this, LinkEntityModel.ACTION_UPDATE, editTitleLink, null).execute(linkModel);
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

        new ActionTask(this, LinkEntityModel.ACTION_DELETE, entity, null).execute(linkModel);
    }

    @Override
    public void OnTitleModifyClick(LinkEntity entity) {
        editTitleLink = entity;
        mainView.showTitleDialog(entity.getTitle());
    }

    @Override
    public void onSendClick(String tag) {
        String url = currentSelectLink == null ? DEJAVU_URL : currentSelectLink.getLink();

        Context context = MyApplication.getContext();

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

    @Override
    public boolean onQueryTextSubmit(String query) {
        //need not to use
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        new ActionTask(this, LinkEntityModel.ACTION_QUERYBYTITLE, null, newText).execute(linkModel);
        return false;
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

    private void afterDoAction(int actionId, int tag) {
        switch (actionId) {
            case LinkEntityModel.ACTION_INSERT:
                if (linkModel.table_size() > 0)
                    mainView.displayNoContentTextView(false);

                if (!URLUtil.isValidUrl(newData.getLink())) {
                    mainView.notifyDataSetChanged();
                    editTitleLink = newData;
                    mainView.showTitleDialog(newData.getTitle());
                }
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
    public void onResponse(JSONObject response) {
        mainView.dismissProgress();
        try {
            Log.i(LOG_TAG, "title: " + response.getString("title") + "  thumbnail_url: " + response.getString("thumbnail_url"));
            newData.setTitle(response.getString("title"));
            newData.setThumbnailUrl(response.getString("thumbnail_url"));

            new ActionTask(this, LinkEntityModel.ACTION_UPDATE, newData, null).execute(linkModel);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mainView.dismissProgress();
        Log.e(LOG_TAG, "JSONObject Update Error.");
        editTitleLink = newData;
        mainView.showTitleDialog(null);
    }

    @Override
    public void onPre() {
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

        new ActionTask(MainPresenter.this, LinkEntityModel.ACTION_UPDATE, newData, null).execute(linkModel);
    }


    private static class ActionTask extends AsyncTask<ILinkModel, Void, Integer> {

        private WeakReference<MainPresenter> weakPresenter;

        private int actionId;
        private LinkEntity entity;
        private String title;

        private ActionTask(MainPresenter presenter, int actionId, LinkEntity entity, String title) {
            weakPresenter = new WeakReference<>(presenter);
            this.actionId = actionId;
            this.entity = entity;
            this.title = title;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainPresenter presenter = weakPresenter.get();
            if (presenter != null) {
                presenter.mainView.showProgress();
            }
        }

        @Override
        protected Integer doInBackground(ILinkModel... iModel) {
            return iModel[0].doAction(actionId, entity, title);
        }

        @Override
        protected void onPostExecute(Integer position) {
            super.onPostExecute(position);
            MainPresenter presenter = weakPresenter.get();
            if (presenter != null) {
                presenter.afterDoAction(actionId, position);
                presenter.mainView.dismissProgress();
            }
        }
    }
}
