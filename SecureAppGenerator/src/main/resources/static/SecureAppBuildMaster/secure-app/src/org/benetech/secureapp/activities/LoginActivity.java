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

package org.benetech.secureapp.activities;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.iangclifton.android.floatlabel.FloatLabel;

import org.benetech.secureapp.utilities.Utility;
import org.benetech.secureapp.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import info.guardianproject.cacheword.ICacheWordSubscriber;
import info.guardianproject.cacheword.Wiper;

/**
 * Created by animal@martus.org on 8/27/14.
 */
public class LoginActivity extends AbstractLoginActivity implements ICacheWordSubscriber {

    private static final String TAG = "LoginActivity";
    private TextView passPhraseEditField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_secure_app);

        FloatLabel floatingLabelEditText = ((FloatLabel) findViewById(R.id.passphrase_edit_field));
        passPhraseEditField = floatingLabelEditText.getEditText();
        Util.setupFloatingLabelEditTextForPassword(floatingLabelEditText.getEditText());

    }

    public void login(View view) {
        if (passPhraseEditField.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.label_empty_pass_phrase), Toast.LENGTH_SHORT).show();
            return;
        }

        char[] passphrase = passPhraseEditField.getText().toString().toCharArray();
        try {
            getCacheWordActivityHandler().setPassphrase(passphrase);
        } catch (GeneralSecurityException e) {
            Log.e(TAG, getString(R.string.error_message_cacheword_pass_verification_failed), e);
            Toast.makeText(getApplicationContext(), getString(R.string.error_incorrect_passphrase), Toast.LENGTH_SHORT).show();
            passPhraseEditField.getEditableText().clear();
            login(view);
        }
        finally {
            Wiper.wipe(passphrase);
        }
    }

    @Override
    protected void postMountStorageExecute() {
        super.postMountStorageExecute();

        confirmAccount();
    }

    private void confirmAccount()  {
        try {
            byte[] keyPair = Base64.decode(getSettings().getString(KEY_KEY_PAIR, ""), Base64.NO_WRAP);
            InputStream byteArrayInputStream = new ByteArrayInputStream(keyPair);

            char[] passphraseAsCharArray = Utility.convertToCharArray(getCacheWordActivityHandler().getEncryptionKey());
            getMartusCrypto().readKeyPair(byteArrayInputStream, passphraseAsCharArray);
        } catch (Exception e) {
            Util.alterUserOfUnexpectedError(this, e, getString(R.string.error_message_problem_confirming_password));
        }

        getMartusCrypto().setShouldWriteAuthorDecryptableData(false);
    }

    @Override
    public void onCacheWordLocked() {
    }

    @Override
    protected TextView getPassPhraseTextField() {
        return passPhraseEditField;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}
