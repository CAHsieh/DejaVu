package ca.pet.dejavu.Utils;


import ca.pet.dejavu.Utils.Table.DaoMaster;
import ca.pet.dejavu.Utils.Table.DaoSession;
import ca.pet.dejavu.Utils.Table.DataEntityDao;

/**
 * Created by CAHSIEH on 2017/10/30.
 * singleton for DBService
 */

public class DBService {

    private volatile static DBService instance;
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
                instance = new DBService();
            }
        }
        return instance;
    }

    /**
     * 獲取LinkEntity的DAO
     *
     * @return LinkEntityDao
     */
    public DataEntityDao getDataEntityDao() {
        if (null == daoSession) throw new RuntimeException("DaoSession not initial yet.");
        return daoSession.getDataEntityDao();
    }

}
