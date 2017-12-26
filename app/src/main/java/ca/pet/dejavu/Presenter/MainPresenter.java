package ca.pet.dejavu.Presenter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import ca.pet.dejavu.Model.DataEntityModel;
import ca.pet.dejavu.Model.IDataModel;
import ca.pet.dejavu.R;
import ca.pet.dejavu.Utils.AsyncTask.ImageInsertTask;
import ca.pet.dejavu.Utils.AsyncTask.NormalActionTask;
import ca.pet.dejavu.Utils.MyApplication;
import ca.pet.dejavu.Utils.SPConst;
import ca.pet.dejavu.Utils.Table.DataEntity;
import ca.pet.dejavu.View.IMainView;

/**
 * Created by CAMac on 2017/11/20.
 * Presenter of MainActivity
 * 內容包含View與Model，
 * 做為業務面的操作及橋樑。
 */
public class MainPresenter implements IMainPresenter, SearchView.OnQueryTextListener,
        Response.Listener<JSONObject>, Response.ErrorListener,
        LinkPreviewCallback {

    private static final String LOG_TAG = "DejaVu";
    private static final String DEJAVU_URL = "https://youtu.be/dv13gl0a-FA";

    private IMainView mainView;
    private IDataModel dataModel;

    private DataEntity newData = null;
    private DataEntity currentSelectLink = null;
    private DataEntity editTitleLink = null;

    public MainPresenter(IMainView mainView) {
        this.mainView = mainView;
        dataModel = new DataEntityModel(this); //註冊Model
    }

    @Override
    public void setQueryType(int type) {
        dataModel.setQueryType(type);
        MyApplication.currentVisibleType = type;
    }

    /**
     * 列出Table內所有資料
     */
    @Override
    public void queryAll() {
        new NormalActionTask(this, DataEntityModel.ACTION_QUERYALL, null, "").execute(dataModel);
    }

    /**
     * 新增網頁資料
     *
     * @param title 標題
     * @param url   網址
     */
    @Override
    public void addNewUrl(String title, String url) {
        newData = new DataEntity();
        newData.setUri(url);
        newData.setTitle(title);
        newData.setParent_Id(null);
        newData.setType(SPConst.VISIBLE_TYPE_LINK);
        new NormalActionTask(this, DataEntityModel.ACTION_INSERT, newData, null).execute(dataModel);
    }

    @Override
    public void addNewImage(Uri... uris) {
        new ImageInsertTask(this, DataEntityModel.ACTION_INSERT, uris).execute(dataModel);
    }

    /**
     * 取得特定資料
     *
     * @param position index值
     * @return 該index值的資料
     */
    @Override
    public DataEntity getEntity(int position) {
        return dataModel.getEntity(position);
    }

    /**
     * 取得目前顯示的list的資料數量
     *
     * @return size of list
     */
    @Override
    public int getPresentingSize() {
        return dataModel.presenting_size();
    }

    /**
     * 修改資料標題。
     *
     * @param title 新的標題。
     */
    @Override
    public void editTitle(String title) {
        editTitleLink.setTitle(title);
        new NormalActionTask(this, DataEntityModel.ACTION_UPDATE, editTitleLink, null).execute(dataModel);
        editTitleLink = null;
    }

    /**
     * 當項目被選取時的反應
     *
     * @param position 被選取的資料
     */
    @Override
    public void onLinkSelected(int position) {
        DataEntity entity = getEntity(position);
        if (entity.equals(currentSelectLink)) {
            currentSelectLink = null;
            return;
        }
        currentSelectLink = entity;
        mainView.notifyDataSetChanged();
    }

    /**
     * 資料的刪除事件內容
     *
     * @param position 要被刪除的資料。
     */
    @Override
    public void onLinkDelete(int position) {

        DataEntity entity = getEntity(position);

        if (null != currentSelectLink && currentSelectLink.equals(entity))
            currentSelectLink = null;

        if (dataModel.presenting_size() == 1) {
            mainView.displayNoContentTextView(true);
        }

        new NormalActionTask(this, DataEntityModel.ACTION_DELETE, entity, null).execute(dataModel);
    }

    /**
     * 編輯標題的事件內容
     *
     * @param position 要編輯標題的資料
     */
    @Override
    public void onTitleModifyClick(int position) {
        editTitleLink = getEntity(position);
        mainView.showTitleDialog(editTitleLink.getTitle());
    }

    /**
     * 發送的事件內容
     *
     * @param tag 發送平台
     */
    @Override
    public void onSendClick(String tag) {

        if (SPConst.VISIBLE_TYPE_LINK == MyApplication.currentVisibleType) {
            sendLink(tag);
        } else {
            //todo
        }

    }

    @Override
    public void cancelTextCrawler() {
        dataModel.cancelTextCrawler();
    }

    /**
     * SearchView事件，submit
     * need not to use
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * SearchView上的文字更改監聽
     *
     * @param newText 新的文字內容
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        new NormalActionTask(this, DataEntityModel.ACTION_QUERYBYTITLE, null, newText).execute(dataModel);
        return false;
    }

    private void sendLink(String tag) {
        String url = currentSelectLink == null ? DEJAVU_URL : currentSelectLink.getUri();
        Context context = MyApplication.getContext();
        if (tag == null || tag.equals(context.getString(R.string.tag_messenger))) {
            //share to messenger
            if (isAppInstalled(context, context.getString(R.string.package_name_messenger))) {
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(url))
                        .build();
                mainView.showMessengerDialog(content);
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

    /**
     * 用於判斷App是否已安裝
     *
     * @param context     application context
     * @param packageName 要判斷的App的package name
     * @return true for is installed.
     */
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

    /**
     * 執行Table操作後的後續業務
     *
     * @param actionId 操作ID
     * @param tag      insert時的偏移量及其他動作的index值
     */
    public void afterDoAction(int actionId, int tag) {
        switch (actionId) {
            case DataEntityModel.ACTION_INSERT:
                //若tag為0代表新增圖片失敗。
                if (tag == 0) {
                    mainView.showSnack("Insert new data error.");
                    break;
                }

                //新增的後續：判斷NoContentTextView是否需要開啟
                //以及若傳入的內容非正確網址格式，則需要自行添加標題。
                if (dataModel.presenting_size() > 0)
                    mainView.displayNoContentTextView(false);

                if (newData != null && !URLUtil.isValidUrl(newData.getUri())) {
                    mainView.notifyInsertCompleted();
                    editTitleLink = newData;
                    mainView.showTitleDialog(newData.getTitle());
                } else {
                    mainView.notifyInsertCompleted();
                }
                break;
            case DataEntityModel.ACTION_QUERYALL:
                //Query的後續：判斷NoContentTextView是否需要開啟
                //及通知畫面更新
                if (dataModel.presenting_size() > 0)
                    mainView.displayNoContentTextView(false);
                //fall through
            case DataEntityModel.ACTION_QUERYBYTITLE:
                mainView.notifyDataSetChanged();
                break;
            case DataEntityModel.ACTION_DELETE:
                //刪除的後續：通知畫面更新。
                //若index有值則使用有動畫的方法。
                if (-1 != tag) {
                    mainView.notifyItemRemoved(tag);
                } else {
                    mainView.notifyDataSetChanged();
                }
                break;
            case DataEntityModel.ACTION_UPDATE:
                //更新的後續：通知畫面更新。
                //若index有值則使用有動畫的方法。
                if (-1 != tag) {
                    mainView.notifyItemChanged(tag);
                } else {
                    mainView.notifyDataSetChanged();
                }
                break;
        }
    }

    public void showProgress() {
        mainView.showProgress();
    }

    public void dismissProgress() {
        mainView.dismissProgress();
    }

    /**
     * Volley的Listener
     * 用於youtube的詳細資訊
     * 查詢成功的callback
     *
     * @param response 回傳結果
     */
    @Override
    public void onResponse(JSONObject response) {
        try {
            Log.i(LOG_TAG, "title: " + response.getString("title") + "  thumbnail_url: " + response.getString("thumbnail_url"));
            newData.setTitle(response.getString("title"));
            newData.setThumbnailUrl(response.getString("thumbnail_url"));

            new NormalActionTask(this, DataEntityModel.ACTION_UPDATE, newData, null).execute(dataModel);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Volley的Listener
     * 用於youtube的詳細資訊
     * 查詢失敗的callback
     *
     * @param error 錯誤資訊
     */
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(LOG_TAG, error.getMessage());
        error.printStackTrace();
        editTitleLink = newData;
        mainView.showTitleDialog(null);
    }

    /**
     * TextCrawler's listener
     * 用於youtube以外的網址。
     * <p>
     * 執行前的callback
     * need not to used
     */
    @Override
    public void onPre() {
    }

    /**
     * TextCrawler's listener
     * 用於youtube以外的網址。
     * <p>
     * 執行後的callback
     *
     * @param sourceContent 回傳結果
     */
    @Override
    public void onPos(SourceContent sourceContent, boolean b) {
        mainView.dismissProgress();

        if (sourceContent.isSuccess()) {
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

            new NormalActionTask(MainPresenter.this, DataEntityModel.ACTION_UPDATE, newData, null).execute(dataModel);
        } else {
            Log.e(LOG_TAG, MyApplication.getContext().getString(R.string.log_textcrawler_failed));
            editTitleLink = newData;
            mainView.showTitleDialog(null);
        }
    }

}
