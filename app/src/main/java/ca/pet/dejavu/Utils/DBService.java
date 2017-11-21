package ca.pet.dejavu.Utils;


import ca.pet.dejavu.Utils.Table.DaoMaster;
import ca.pet.dejavu.Utils.Table.DaoSession;
import ca.pet.dejavu.Utils.Table.LinkEntityDao;

/**
 * Created by CAHSIEH on 2017/10/30.
 * singleton for DBService
 */

public class DBService {

    private static DBService instance;
    private static final String DB_NAME = "DEJAVU.db";

    private DaoSession daoSession;

    private DBService() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(MyApplication.getContext(), DB_NAME);
        DaoMaster daoMaster = new DaoMaster(helper.getWritableDb());
        daoSession = daoMaster.newSession();
    }

    synchronized public static DBService getInstance() {
        if (instance == null) {
            synchronized (DBService.class) {
                if (instance == null) {
                    instance = new DBService();
                }
            }
        }
        return instance;
    }

    /**
     * 獲取LinkEntity的DAO
     * @return LinkEntityDao
     */
    public LinkEntityDao getLinkEntityDao() {
        if (null == daoSession) throw new RuntimeException("DaoSession not initial yet.");
        return daoSession.getLinkEntityDao();
    }

}
