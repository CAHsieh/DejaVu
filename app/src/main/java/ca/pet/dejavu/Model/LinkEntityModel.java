package ca.pet.dejavu.Model;

import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import ca.pet.dejavu.Data.DBService;
import ca.pet.dejavu.Data.LinkEntity;
import ca.pet.dejavu.Data.LinkEntityDao;
import ca.pet.dejavu.Utils.LinkEntityEvent;

/**
 * Created by CAHSIEH on 2017/11/15.
 * presenter of LinkEntity
 */

public class LinkEntityModel {

    public static final int ACTION_QUERYALL = 0x01;
    public static final int ACTION_QUERYBYTITLE = 0x02;
    public static final int ACTION_INSERT = 0x03;
    public static final int ACTION_UPDATE = 0x04;
    public static final int ACTION_DELETE = 0x05;

    private static LinkEntityModel instance = null;

    private LinkEntityDao entityDao;
    private List<LinkEntity> presenting_list = null;

    private String currentTitleCondition = "";

    private LinkEntityModel() {
        DBService service = DBService.getInstance();
        entityDao = service.getLinkEntityDao();
        presenting_list = new ArrayList<>();
    }

    synchronized public static LinkEntityModel getInstance() {
        if (null == instance) {
            synchronized (LinkEntityModel.class) {
                if (null == instance) {
                    instance = new LinkEntityModel();
                }
            }
        }
        return instance;
    }

    public synchronized void doAction(int action_Id, @Nullable LinkEntity entity, @Nullable String title) {
        int position = 0;
        switch (action_Id) {
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

        LinkEntityEvent event = new LinkEntityEvent(action_Id, position);
        EventBus.getDefault().post(event);
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

    public int table_size() {
        return (int) entityDao.count();
    }

    public int presenting_size() {
        if (presenting_list != null) {
            return presenting_list.size();
        }
        return 0;
    }

    public List<LinkEntity> getPresenting_list() {
        return presenting_list;
    }

    public LinkEntity getEntity(int i) {
        if (presenting_list != null) {
            return presenting_list.get(i);
        }
        return null;
    }
}
