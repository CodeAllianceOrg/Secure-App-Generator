package org.martus.android.vitalVoices.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.martus.android.library.utilities.BulletinSender;
import org.martus.clientside.MobileClientBulletinStore;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.bulletin.DebugClass;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.DatabaseKey;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author roms Date: 10/3/12
 *         animal@martus.org 09/21/2014
 */

public class ZipBulletinTask extends AsyncTask<Object, Integer, File> {

    private static final SimpleDateFormat FILE_NAME_DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SS_");
    private Bulletin mBulletin;
    private BulletinSender mSender;

    public ZipBulletinTask(Bulletin bulletin, BulletinSender sender) {
        mBulletin = bulletin;
        mSender = sender;
    }

    @Override
    protected File doInBackground(Object... params) {

        final File currentBulletinDir = (File)params[0];
        final MobileClientBulletinStore store = (MobileClientBulletinStore)params[1];

        try {
            SingleBulletinDataBase database = new SingleBulletinDataBase();
            store.setDatabase(database);
            store.saveBulletin(mBulletin);
            File file = File.createTempFile("tmp_send_" + getCurrentTimeStamp(), ".zip", currentBulletinDir);
            DebugClass.setDebugToTrue();

            DatabaseKey databaseKey = mBulletin.getDatabaseKey();
            MartusCrypto signatureGenerator = mBulletin.getSignatureGenerator();
            BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(database, databaseKey, file, signatureGenerator);
            if (!file.exists())
                throw new FileNotFoundException("Could not find record zip file after it was created");

            return file;
        } catch (Exception e) {
            Log.e("martus", "problem serializing record to zip", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(File result) {
        if (null != mSender) {
            mSender.onZipped(mBulletin, result);
        }

        super.onPostExecute(result);
    }

    private String getCurrentTimeStamp() {
        Date now = new Date();
        return FILE_NAME_DATE_FORMAT.format(now);
    }
}
