package ca.pet.dejavu.Model;

import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.leocardz.link.preview.library.TextCrawler;

import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import ca.pet.dejavu.Utils.DBService;
import ca.pet.dejavu.Utils.Table.LinkEntity;
import ca.pet.dejavu.Utils.Table.LinkEntityDao;
import ca.pet.dejavu.Presenter.MainPresenter;
import ca.pet.dejavu.Utils.MyApplication;

/**
 * Created by CAHSIEH on 2017/11/15.
 * LinkEntityModel
 * 內容不可包含View的方法的調用
 * 僅可透過presenter做交流。
 */

public class LinkEntityModel implements ILinkModel {

    public static final int ACTION_QUERYALL = 0x01;
    public static final int ACTION_QUERYBYTITLE = 0x02;
    public static final int ACTION_INSERT = 0x03;
    public static final int ACTION_UPDATE = 0x04;
    public static final int ACTION_DELETE = 0x05;

    private LinkEntityDao entityDao;
    private List<LinkEntity> presenting_list = null;
    private String currentTitleCondition = "";

    private MainPresenter mainPresenter = null;

    private TextCrawler textCrawler = null;

    public LinkEntityModel(MainPresenter mainPresenter) {
        DBService service = DBService.getInstance();
        entityDao = service.getLinkEntityDao();
        this.mainPresenter = mainPresenter;
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
    public synchronized int doAction(int actionId, @Nullable LinkEntity entity, @Nullable String title) {
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
     * 取得Table中的資料數。
     *
     * @return Table中的資料數
     */
    @Override
    public int table_size() {
        return (int) entityDao.count();
    }

    /**
     * 取得目前顯示內容的資料數
     *
     * @return 目前顯示內容的資料數
     */
    @Override
    public int presenting_size() {
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
    public LinkEntity getEntity(int i) {
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
        Property idProperty = LinkEntityDao.Properties.Id;
        presenting_list = entityDao.queryBuilder().orderDesc(idProperty).list();
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
        QueryBuilder<LinkEntity> queryBuilder = entityDao.queryBuilder();
        Property titleProperty = LinkEntityDao.Properties.Title;
        Property idProperty = LinkEntityDao.Properties.Id;
        presenting_list = queryBuilder.where(titleProperty.like(like)).orderDesc(idProperty).list();
    }

    /**
     * 刪除指定資料
     *
     * @param entity 要刪除的資料
     * @return 刪除資料在list中的位置
     */
    private int delete(LinkEntity entity) {
        int position = presenting_list.indexOf(entity);
        presenting_list.remove(entity);
        entityDao.delete(entity);
        return position;
    }

    /**
     * 新增資料
     *
     * @param entity 要新增的資料
     */
    private void insert(LinkEntity entity) {
        entityDao.insert(entity);
        if (presenting_list != null)
            presenting_list.add(0, entity);

        String url = entity.getLink();

        //新增完後，若存入的內容確認為網址，則開始查詢其縮圖及標題。
        if (0 == url.indexOf("https://youtu.be") || 0 == url.indexOf("https://www.youtube.com/")) {
            //youtube網址直接使用它提供的API方法查詢。
            String ytDesUrl = "https://www.youtube.com/oembed?url=" + url + "&format=json";
            JsonObjectRequest ytDesReq = new JsonObjectRequest(ytDesUrl, mainPresenter, mainPresenter);
            Volley.newRequestQueue(MyApplication.getContext()).add(ytDesReq);
        } else if (URLUtil.isValidUrl(url)) {
            //其餘網址使用TextCrawler library查詢
            url = url.substring(url.indexOf("http"));
            entity.setLink(url);
            textCrawler = new TextCrawler();
            textCrawler.makePreview(mainPresenter, url);
        }

    }

    /**
     * 更新傳入的資料至表單。
     *
     * @param entity 更新的資料
     */
    private void update(LinkEntity entity) {
        entityDao.update(entity);
        if (!currentTitleCondition.equals("")) {
            queryByTitle(currentTitleCondition);
        }
    }

}
