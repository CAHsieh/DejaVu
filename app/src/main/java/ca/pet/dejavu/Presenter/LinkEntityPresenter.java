package ca.pet.dejavu.Presenter;

import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import ca.pet.dejavu.Model.DBService;
import ca.pet.dejavu.Model.LinkEntity;
import ca.pet.dejavu.Model.LinkEntityDao;

/**
 * Created by CAHSIEH on 2017/11/15.
 * presenter of LinkEntity
 */

public class LinkEntityPresenter {

    private static LinkEntityPresenter instance = null;

    private LinkEntityDao entityDao;
    private List<LinkEntity> presenting_list = null;

    private String currentTitleCondition = "";

    private LinkEntityPresenter() {
        DBService service = DBService.getInstance();
        entityDao = service.getLinkEntityDao();
    }

    synchronized public static LinkEntityPresenter getInstance() {
        if (null == instance) {
            synchronized (LinkEntityPresenter.class) {
                if (null == instance) {
                    instance = new LinkEntityPresenter();
                }
            }
        }
        return instance;
    }

    public void queryAll() {
        presenting_list = entityDao.loadAll();
    }

    public void queryByTitle(String title) {
        currentTitleCondition = title;
        String like = "%" + title + "%";
        QueryBuilder<LinkEntity> queryBuilder = entityDao.queryBuilder();
        Property titleProperty = LinkEntityDao.Properties.Title;
        presenting_list = queryBuilder.where(titleProperty.like(like)).list();
    }

    public int delete(LinkEntity entity) {
        int position = presenting_list.indexOf(entity);
        presenting_list.remove(entity);
        entityDao.delete(entity);
        return position;
    }

    public void insert(LinkEntity entity) {
        entityDao.insert(entity);
        presenting_list.add(entity);
    }

    /**
     * used to update table by entity
     *
     * @param entity updated LinkEntity
     */
    public void update(LinkEntity entity) {
        entityDao.update(entity);
        if (!currentTitleCondition.equals("")) {
            queryByTitle(currentTitleCondition);
        }
    }

    public int table_size() {
        return (int) entityDao.count();
    }

    public int presenting_size() {
        return presenting_list.size();
    }

    public List<LinkEntity> getPresenting_list() {
        return presenting_list;
    }

    public LinkEntity getEntity(int i) {
        return presenting_list.get(i);
    }
}
