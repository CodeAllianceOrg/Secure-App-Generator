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

package org.benetech.secureapp;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import org.benetech.secureapp.application.MainApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Created by animal@martus.org on 9/9/14.
 */
public class UnencryptedFormFromAssetFolderExtractor {

    private static final String XML_FILE_EXTENSION = ".xml";
    private static final String TAG = "FormCopier";
    public static final String XFORMS_DIR_NAME = "xforms";
    private MainApplication mApplication;

    public UnencryptedFormFromAssetFolderExtractor(MainApplication application) throws Exception {
        mApplication = application;
    }

    public File writeSingleFormToStorage() throws Exception {
        Vector<File> copiedForms = writeFormsToDiskFromAssestsFolder();
        if (copiedForms.size() != 1)
            throw new RuntimeException("Incorrect number of forms copied");

        File formFile = copiedForms.firstElement();
        if (!formFile.exists())
            throw new RuntimeException("Could not find newly copied form file:" + formFile.getAbsolutePath());
        else
            Log.i(TAG, "Form file found: " + formFile.getAbsolutePath());

        return formFile;
    }

    private Vector<File> writeFormsToDiskFromAssestsFolder() throws Exception {
        String externalStorageDirectory = Environment.getExternalStorageDirectory().toString();
        String basepath = externalStorageDirectory + "/secureApp";

        File xformsDir = new File(basepath + "/" + XFORMS_DIR_NAME + "/");
        xformsDir.mkdirs();
        return copyXFormsToExternalStorage(basepath);
    }

    private Vector<File> copyXFormsToExternalStorage(String basepath) throws Exception {
        AssetManager assetManager = getResources().getAssets();
        String[] files = assetManager.list(XFORMS_DIR_NAME);
        Vector<File> copiedToPaths = new Vector<File>();
        for(int index = 0; index < files.length; ++index) {
            String fileNameToCopy = files[index];
            String copyToPath = copyFile(assetManager, basepath, fileNameToCopy);
            copiedToPaths.add(new File(copyToPath));
        }

        return copiedToPaths;
    }

    private String copyFile(AssetManager assetManager, String basepath, String fileNameToCopy) throws IOException {
        InputStream in = assetManager.open(XFORMS_DIR_NAME + "/" + fileNameToCopy);
        String copyToPath = basepath + "/" + XFORMS_DIR_NAME + "/" + fileNameToCopy + ".xml";
        OutputStream out = new FileOutputStream(copyToPath);
        try {
            copyStream(in, out);
        }
        finally {
            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;
        }

        return copyToPath;
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private MainApplication getApplication() {
        return mApplication;
    }

    private Resources getResources() {
        return getApplication().getResources();
    }
}
