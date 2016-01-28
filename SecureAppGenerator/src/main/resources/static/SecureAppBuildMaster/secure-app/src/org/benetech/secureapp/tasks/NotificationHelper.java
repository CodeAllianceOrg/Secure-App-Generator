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

package org.benetech.secureapp.tasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import org.benetech.secureapp.R;
import org.benetech.secureapp.activities.BulletinActivity;
import org.martus.common.network.NetworkInterfaceConstants;

/**
 * @author roms
 *         Date: 10/23/12
 */
public class NotificationHelper {
    private Context mContext;
    private NotificationManager mNotificationManager;
    private int mNotificationId;
    private String mTitle;

    public NotificationHelper(Context context, int notificationId)
    {
        mContext = context;
        mNotificationId = notificationId;
    }

    /**
     * Put the notification into the status bar
     */
    public void createNotification(String title, String subject) {
        mTitle = title;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        //create the content which is shown in the notification pulldown
        final Notification notification = new NotificationCompat.Builder(mContext)
                 .setContentTitle(mTitle)
                 .setContentText(subject)
                 .setSmallIcon(android.R.drawable.stat_sys_upload)
                 .setOngoing(true)
                 .setProgress(100, 0, false)
                 .setAutoCancel(true)
                 .build();

        notification.contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(), 0);

        //show the notification
        mNotificationManager.notify(mNotificationId, notification);
    }

    public void updateProgress(String subject, int progress) {
        //create the content which is shown in the notification pulldown
        final Notification notification = new NotificationCompat.Builder(mContext)
                 .setContentTitle(mTitle)
                 .setContentText(subject)
                 .setSmallIcon(android.R.drawable.stat_sys_upload)
                 .setOngoing(true)
                 .setProgress(100, progress, false)
                 .build();

        notification.contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(), 0);

        //show the notification
        mNotificationManager.notify(mNotificationId, notification);
    }

    /**
     * called when the background task is complete, this removes the notification from the status bar.
     * We could also use this to add a new ‘task complete’ notification
     */
    public void  completed(String resultMsg)    {
        //update notification to indicate completion
        int icon;
        if (null != resultMsg) {
            icon = android.R.drawable.stat_sys_upload_done;
            if (!resultMsg.equals(NetworkInterfaceConstants.OK)) {
                icon = android.R.drawable.stat_notify_error;
            }
        } else {
            resultMsg = mContext.getString(R.string.send_failed_cant_reach_server);
            icon = android.R.drawable.stat_notify_error;
        }
        String message = BulletinActivity.getResultMessage(resultMsg, mContext);
        final Notification notification = new NotificationCompat.Builder(mContext)
                .setContentTitle(mTitle)
                .setContentText(message)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .build();

        notification.contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(), 0);

        mNotificationManager.notify(mNotificationId, notification);
    }
}
