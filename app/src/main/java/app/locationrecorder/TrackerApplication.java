package app.locationrecorder;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import app.locationrecorder.models.DaoMaster;
import app.locationrecorder.models.DaoSession;

/**
 * Created by Madeyedexter on 18-06-2017.
 * The Application class for this app. Initializes GreenDao session.
 */

public class TrackerApplication extends Application {

    private DaoMaster daoMaster;

    public DaoSession getDaoSession() {
        return daoSession == null ? daoSession = daoMaster.newSession() : daoSession;
    }

    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(this, "notes-db", null);
        SQLiteDatabase sqLiteDatabase = devOpenHelper.getWritableDatabase();
        daoMaster = new DaoMaster(sqLiteDatabase);
    }
}
