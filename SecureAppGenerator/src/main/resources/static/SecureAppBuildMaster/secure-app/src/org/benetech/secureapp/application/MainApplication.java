/*
 * The Martus(tm) free, social justice documentation and
 * monitoring software. Copyright (C) 2016, Beneficent
 * Technology, Inc. (Benetech).
 *
 * Martus is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later
 * version with the additions and exceptions described in the
 * accompanying Martus license file entitled "license.txt".
 *
 * It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, including warranties of fitness of purpose or
 * merchantability.  See the accompanying Martus License and
 * GPL license for more details on the required license terms
 * for this software.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */

package org.benetech.secureapp.application;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import org.benetech.secureapp.R;
import org.benetech.secureapp.activities.AppTimeoutManager;
import org.benetech.secureapp.activities.LogoutActivityHandler;
import org.benetech.secureapp.activities.MainActivity;
import org.benetech.secureapp.collect.io.SecureFileStorageManager;
import org.martus.android.library.io.SecureFile;
import org.odk.collect.android.application.Collect;

import java.io.File;

import info.guardianproject.cacheword.CacheWordHandler;

/**
 * Created by nimaa on 7/18/14.
 */
public class MainApplication extends Collect {
	private static final String TAG = "MainApplication";
    private static String sSecureStoragePath;
    private static SecureFileStorageManager sSecureStorage;
    private static int sNumSecureStorageHolds;
    private static final String SECURE_STORAGE_FOLDER_NAME = "secureStorage";
    private AppTimeoutManager appTimeoutManager;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Assign a path on this application's internal storage for the secure filesystem

        sSecureStoragePath = new File(this.getFilesDir(), SECURE_STORAGE_FOLDER_NAME).getAbsolutePath();
        appTimeoutManager = new AppTimeoutManager(this);
    }

    public void mountSecureStorage(CacheWordHandler cacheWordActivityHandler) {
		if (sSecureStorage == null) {
			if (sSecureStoragePath == null) {
				throw new IllegalStateException(getString(R.string.error_message_cannot_mount_secure_storage));
			}
			sSecureStorage = new SecureFileStorageManager(cacheWordActivityHandler, sSecureStoragePath);
		}
		if (!sSecureStorage.isFilesystemMounted()) {
			sSecureStorage.mountFilesystem(this, cacheWordActivityHandler.getEncryptionKey());
		}
		sNumSecureStorageHolds++;
	}

    public SecureFileStorageManager getMountedSecureStorage() {
        if (sSecureStorage == null) {
            throw new IllegalStateException(getString(R.string.error_message_secure_storage_has_not_been_initialized));
        }

        return sSecureStorage;
    }

    public SecureFile getSecureStorageDir() {
        return getMountedSecureStorage().getSecureFileSysteDir();
    }

    public SecureFile getGallaryDir() {
        return new SecureFile(getSecureStorageDir(), MainActivity.GALLARY_FOLDER_NAME);
    }

    public void unmountSecureStorage() {
    	if (sSecureStorage != null && sSecureStorage.isFilesystemMounted()) {
    		if (--sNumSecureStorageHolds == 0) {
    			sSecureStorage.unmountFilesystem();
    		}
    	} else {
    		Log.w(TAG, getString(R.string.error_message_unmountSecureStorage_called_with_no_storage_mounted));
    	}
    }

    public void resetInactivityTimer() {
        appTimeoutManager.resetInactivityTimer();
    }

    public void registerLogoutHandler(LogoutActivityHandler logoutActivityHandler) {
        appTimeoutManager.registerLogoutHandler(logoutActivityHandler);
    }
}
