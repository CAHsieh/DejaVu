package ca.pet.dejavu.Model;

import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.leocardz.link.preview.library.TextCrawler;

import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import ca.pet.dejavu.Data.DBService;
import ca.pet.dejavu.Data.LinkEntity;
import ca.pet.dejavu.Data.LinkEntityDao;
import ca.pet.dejavu.Presenter.MainPresenter;
import ca.pet.dejavu.Utils.MyApplication;

/**
 * Created by CAHSIEH on 2017/11/15.
 * presenter of LinkEntity
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

    public LinkEntityModel(MainPresenter mainPresenter) {
        DBService service = DBService.getInstance();
        entityDao = service.getLinkEntityDao();
        this.mainPresenter = mainPresenter;
    }

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

    private void queryAll() {
        Property idProperty = LinkEntityDao.Properties.Id;
        presenting_list = entityDao.queryBuilder().orderDesc(idProperty).list();
    }

    private void queryByTitle(String title) {
        currentTitleCondition = title;
        String like = "%" + title + "%";
        QueryBuilder<LinkEntity> queryBuilder = entityDao.queryBuilder();
        Property titleProperty = LinkEntityDao.Properties.Title;
        Property idProperty = LinkEntityDao.Properties.Id;
        presenting_list = queryBuilder.where(titleProperty.like(like)).orderDesc(idProperty).list();
    }

    private int delete(LinkEntity entity) {
        int position = presenting_list.indexOf(entity);
        presenting_list.remove(entity);
        entityDao.delete(entity);
        return position;
    }

    private void insert(LinkEntity entity) {
        entityDao.insert(entity);
        if (presenting_list != null)
            presenting_list.add(0, entity);

        String url = entity.getLink();

        if (0 == url.indexOf("https://youtu.be") || 0 == url.indexOf("https://www.youtube.com/")) {
            String ytDesUrl = "https://www.youtube.com/oembed?url=" + url + "&format=json";
            JsonObjectRequest ytDesReq = new JsonObjectRequest(ytDesUrl, mainPresenter, mainPresenter);
            Volley.newRequestQueue(MyApplication.getContext()).add(ytDesReq);
        } else if (URLUtil.isValidUrl(url)) {
            url = url.substring(url.indexOf("http"));
            entity.setLink(url);
            TextCrawler textCrawler = new TextCrawler();
            textCrawler.makePreview(mainPresenter, url);
        }

    }

    /**
     * used to update table by entity
     *
     * @param entity updated LinkEntity
     */
    private void update(LinkEntity entity) {
        entityDao.update(entity);
        if (!currentTitleCondition.equals("")) {
            queryByTitle(currentTitleCondition);
        }
    }

    @Override
    public int table_size() {
        return (int) entityDao.count();
    }

    @Override
    public int presenting_size() {
        if (presenting_list != null) {
            return presenting_list.size();
        }
        return 0;
    }

    @Override
    public LinkEntity getEntity(int i) {
        if (presenting_list != null) {
            return presenting_list.get(i);
        }
        return null;
    }
}
