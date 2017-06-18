package app.locationrecorder;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import app.locationrecorder.models.DaoSession;
import app.locationrecorder.models.LocationStamp;
import app.locationrecorder.models.LocationStampDao;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class DataPersistanceService extends IntentService {

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_WRITE_DB = "app.locationrecorder.action.FOO";
    private static final String ACTION_WRITE_FILE = "app.locationrecorder.action.BAZ";

    private static final String EXTRA_LOCATION_STAMP = "app.locationrecorder.extra.PARAM1";
    private static final String TAG = DataPersistanceService.class.getSimpleName();

    private static final String FILE_NAME = "LocationRecorder.log";

    public DataPersistanceService() {
        super("DataPersistanceService");
    }

    /**
     * Starts this service to perform action ACTIION_ACTION_WRITE_DB with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public static void startActionWriteToDb(Context context, LocationStamp locationStamp) {
        Intent intent = new Intent(context, DataPersistanceService.class);
        intent.setAction(ACTION_WRITE_DB);
        intent.putExtra(EXTRA_LOCATION_STAMP, locationStamp);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ACTION_WRITE_FILE with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionWriteToFile(Context context, LocationStamp locationStamp) {
        Intent intent = new Intent(context, DataPersistanceService.class);
        intent.setAction(ACTION_WRITE_FILE);
        intent.putExtra(EXTRA_LOCATION_STAMP, locationStamp);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_WRITE_DB.equals(action)) {
                final LocationStamp locationStamp = intent.getParcelableExtra(EXTRA_LOCATION_STAMP);
                handleActionWriteDb(locationStamp);
            } else if (ACTION_WRITE_FILE.equals(action)) {
                final LocationStamp locationStamp = intent.getParcelableExtra(EXTRA_LOCATION_STAMP);
                handleActionWriteFile(locationStamp);
            }
        }
    }

    /**
     * Handle action ACTION_WRITE_DB in the provided background thread with the provided
     * parameters.
     */
    private void handleActionWriteDb(LocationStamp locationStamp) {
        DaoSession daoSession = ((TrackerApplication) getApplication()).getDaoSession();
        LocationStampDao locationStampDao = daoSession.getLocationStampDao();
        locationStampDao.insert(locationStamp);

        Log.d(TAG, "Locations are: " + locationStampDao.loadAll().toString());
    }

    /**
     * Handle action ACTION_WRITE_FILE in the provided background thread with the provided
     * parameters.
     */
    private void handleActionWriteFile(LocationStamp locationStamp) {
        File file = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
        Log.d(TAG, "Writing file to: " + file.getAbsolutePath());
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            Log.d(TAG, locationStamp.toString());
            String locationString = LocationUtils.getFormattedDate(locationStamp.getTimestamp()) +
                    " " + LocationUtils.getFormattedDecimal(locationStamp.getLatitude()) +
                    " " + LocationUtils.getFormattedDecimal(locationStamp.getLongitude()) +
                    " " + (locationStamp.getCurrentInterval() == null ? "N/A" : locationStamp.getCurrentInterval().getInterval()) +
                    " " + locationStamp.getNextInterval().getInterval();
            bufferedWriter.write(locationString);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
