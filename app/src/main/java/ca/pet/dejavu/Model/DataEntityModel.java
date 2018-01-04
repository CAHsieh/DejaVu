package ca.pet.dejavu.Model;

import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.webkit.URLUtil;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.leocardz.link.preview.library.TextCrawler;

import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ca.pet.dejavu.Presenter.MainPresenter;
import ca.pet.dejavu.Utils.DBService;
import ca.pet.dejavu.Utils.MyApplication;
import ca.pet.dejavu.Utils.SPConst;
import ca.pet.dejavu.Utils.Table.DataEntity;
import ca.pet.dejavu.Utils.Table.DataEntityDao;

/**
 * Created by CAHSIEH on 2017/11/15.
 * DataEntityModel
 * 內容不可包含View的方法的調用
 * 僅可透過presenter做交流。
 */

public class DataEntityModel implements IDataModel {

    public static final int ACTION_QUERYALL = 0x01;
    public static final int ACTION_QUERYBYTITLE = 0x02;
    public static final int ACTION_INSERT = 0x03;
    public static final int ACTION_UPDATE = 0x04;
    public static final int ACTION_DELETE = 0x05;

    private Property typeProperty = DataEntityDao.Properties.Type;
    private Property isDeleteProperty = DataEntityDao.Properties.IsDelete;

    private DataEntityDao entityDao;
    private List<DataEntity> presenting_list = null;
    private int queryType;
    private String currentTitleCondition = "";

    private MainPresenter mainPresenter = null;

    private TextCrawler textCrawler = null;

    public DataEntityModel(MainPresenter mainPresenter) {
        DBService service = DBService.getInstance();
        entityDao = service.getDataEntityDao();
        this.mainPresenter = mainPresenter;
        cleanTable();
    }

    @Override
    public void setQueryType(int type) {
        queryType = type;
    }

    /**
     * 用於執行LinkTable操作
     *
     * @param actionId 操作ID，定義在上方final
     * @param entity   用於操作的實例，可為null
     * @param title    用於修改標題時使用，可為null
     * @return 操作實例的index值，可用於執行畫面更新。
     */
    @Override
    public synchronized int doAction(int actionId, @Nullable DataEntity entity, @Nullable String title) {
        int position = 0;
        switch (actionId) {
            case ACTION_QUERYALL:
                queryAll();
                break;
            case ACTION_QUERYBYTITLE:
                queryByTitle(title);
                break;
            case ACTION_INSERT:
                insert(entity);
                break;
            case ACTION_UPDATE:
                update(entity);
                position = presenting_list.indexOf(entity);
                break;
            case ACTION_DELETE:
                position = delete(entity);
                break;
        }

        return position;
    }

    /**
     * 取得目前顯示內容的資料數
     *
     * @return 目前顯示內容的資料數
     */
    @Override
    synchronized public int presenting_size() {
        if (presenting_list != null) {
            return presenting_list.size();
        }
        return 0;
    }

    /**
     * 取得目前顯示內容的第i筆資料，
     * 若目前顯示的list為null則回傳null。
     *
     * @param i index
     * @return 第i筆資料
     */
    @Override
    synchronized public DataEntity getEntity(int i) {
        if (presenting_list != null) {
            return presenting_list.get(i);
        }
        return null;
    }

    /**
     * 若textCrawler還在運作的話將其終止
     */
    @Override
    public void cancelTextCrawler() {
        if (textCrawler != null) {
            textCrawler.cancel();
        }
    }

    /**
     * 以ID按降冪排序（新到舊）來列出全部資料
     */
    private void queryAll() {
        Property idProperty = DataEntityDao.Properties.Id;
        presenting_list = entityDao.queryBuilder().where(typeProperty.eq(queryType),
                isDeleteProperty.eq(false))
                .orderDesc(idProperty).list();
    }

    /**
     * 傳入標題，列出含此標題的資料，
     * 並以ID降冪排序。
     *
     * @param title 用於查詢的標題
     */
    private void queryByTitle(String title) {
        currentTitleCondition = title;
        String like = "%" + title + "%";
        QueryBuilder<DataEntity> queryBuilder = entityDao.queryBuilder();
        Property titleProperty = DataEntityDao.Properties.Title;
        Property idProperty = DataEntityDao.Properties.Id;
        presenting_list = queryBuilder.where(typeProperty.eq(queryType),
                isDeleteProperty.eq(false),
                titleProperty.like(like))
                .orderDesc(idProperty).list();
    }

    /**
     * 刪除指定資料
     *
     * @param entity 要刪除的資料
     * @return 刪除資料在list中的位置
     */
    private int delete(DataEntity entity) {
        int position = presenting_list.indexOf(entity);
        presenting_list.remove(entity);

        //delete image need to delete file.
        if (entity.getType() == SPConst.VISIBLE_TYPE_IMAGE) {
            File file = new File(entity.getThumbnailUrl());
            if (file.exists()) {
                if (file.delete()) {
                    entityDao.delete(entity);
                } else {
                    entity.setIsDelete(true);
                    entityDao.update(entity);
                }
            }
        } else {
            entityDao.delete(entity);
        }
        return position;
    }

    /**
     * 新增資料
     *
     * @param entity 要新增的資料
     */
    private void insert(DataEntity entity) {
        entityDao.insert(entity);
        if (presenting_list != null)
            presenting_list.add(0, entity);

        String url = entity.getUri();

        //新增完後，若存入的內容確認為網址，則開始查詢其縮圖及標題。
        if (0 == url.indexOf("https://youtu.be") || 0 == url.indexOf("https://www.youtube.com/")) {
            //youtube網址直接使用它提供的API方法查詢。
            String ytDesUrl = "https://www.youtube.com/oembed?url=" + url + "&format=json";
            JsonObjectRequest ytDesReq = new JsonObjectRequest(ytDesUrl, mainPresenter, mainPresenter);
            Volley.newRequestQueue(MyApplication.getContext()).add(ytDesReq);
        } else if (URLUtil.isValidUrl(url)) {
            //其餘網址使用TextCrawler library查詢
            url = url.substring(url.indexOf("http"));
            entity.setUri(url);
            textCrawler = new TextCrawler();
            textCrawler.makePreview(mainPresenter, url);
        }

    }

    /**
     * 更新傳入的資料至表單。
     *
     * @param entity 更新的資料
     */
    private void update(DataEntity entity) {
        entityDao.update(entity);
        if (!currentTitleCondition.equals("")) {
            queryByTitle(currentTitleCondition);
        }
    }

    private void cleanTable() {
        new Thread(() -> {
            List<DataEntity> list = entityDao.queryBuilder().
                    where(isDeleteProperty.eq(true)).list();
            List<DataEntity> deleteList = new ArrayList<>();
            for (DataEntity entity : list) {
                File file = new File(entity.getThumbnailUrl());
                if (!file.exists() || (file.exists() && file.delete())){
                    deleteList.add(entity);
                }
            }
            entityDao.deleteInTx(deleteList);
        }).start();
    }
}
