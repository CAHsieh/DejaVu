package ca.pet.dejavu.Model;

import android.content.Context;

/**
 * Created by CAHSIEH on 2017/10/30.
 * singleton for DBService
 */

public class DBService {

    private static DBService instance;
    private static final String DB_NAME = "DEJAVU.db";

    private DaoSession daoSession;

    private DBService() {

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

    public void init(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME);
        DaoMaster daoMaster = new DaoMaster(helper.getWritableDb());
        daoSession = daoMaster.newSession();
    }

    public LinkEntityDao getLinkEntityDao() {
        if (null == daoSession) throw new RuntimeException("DaoSession not initial yet.");
        return daoSession.getLinkEntityDao();
    }

}
