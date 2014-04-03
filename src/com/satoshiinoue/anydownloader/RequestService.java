package com.satoshiinoue.anydownloader;

import java.util.HashMap;

import com.satoshiinoue.anydownloader.models.DataModel;
import com.satoshiinoue.anydownloader.models.DataModelListener;
import com.satoshiinoue.anydownloader.utilities.Configuration;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
/**
 * A service to notify the request completion to the activity/device
 * @author satoshi.inoue
 *
 */
public class RequestService extends Service implements DataModelListener {
	protected static final String TAG = "RequestService";
	protected NotificationManager mNotificationManager;
	protected HashMap<Integer, NotificationCompat.Builder> currentRequests = new HashMap<Integer, NotificationCompat.Builder>();
	protected Handler mHandler = new Handler();
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	if(Configuration.ALLOW_LOGGING)
    		Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return new RequestServiceBinder();
	}
	
	public final class RequestServiceBinder extends Binder {
    	public RequestService getService() {
    		return RequestService.this;
    	}
    }

	public void fetch(DataModel model, boolean showProgress, final HashMap<String, String> header) {
		Log.d(TAG, "fetch");
		if(header == null)
			model.fetch(this, showProgress);
		else
			model.fetch(this, showProgress, header);
		
		//startForeground(model.getId(), notification);
		
		// Instantiate a Builder object.
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
				.setContentTitle("Start downloading")
			    .setSmallIcon(R.drawable.ic_launcher);
		Intent notifyIntent = new Intent(this, MainActivity.class);
		// Sets the Activity to start in a new, empty task
		//notifyIntent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(notifyPendingIntent);
		currentRequests.put(model.getId(), builder);
		
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(model.getId(), builder.build());
	} 
	
	@Override
	public void onFetchSuccess(DataModel model) {
		Log.d(TAG, "onFetchSuccess");
		// Instantiate a Builder object.
		NotificationCompat.Builder builder = currentRequests.get(model.getId());
		builder.setContentTitle("Download Complete");
		builder.setProgress(0, 0, false);
		Intent notifyIntent = new Intent(android.content.Intent.ACTION_VIEW,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(notifyPendingIntent);
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(model.getId(), builder.build());
		
	}

	@Override
	public void onFetchProgress(final DataModel model, final int nBytesDownloaded,
			final int totalBytes) {
		
		//Restrict too frequent updates:
		final int percentage = (int) (((double)nBytesDownloaded/totalBytes)*100);
		final int buffer = totalBytes/100;
		
		
		if ((nBytesDownloaded%buffer < buffer/100)/* && ((percentage%10) < 2 || (percentage%10) > 8)*/) {
			Log.d(TAG, "onFetchProgress Update percentage -" + percentage + "; " + percentage%10);
			if (mNotificationManager == null)
				mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			if (currentRequests.get(model.getId()) != null)
				/*mHandler.post(new Runnable(){
					@Override
					public void run() {
					*/	mNotificationManager.notify(model.getId(), currentRequests.get(model.getId()).setContentTitle("Downloading " + model.getDataURL()).setProgress(100, percentage, false).build());
					/*}
				});*/
		}
		
		
	}

	@Override
	public void onDataRetrievalFailed(DataModel model, int errorCode,
			String message) {
		Log.d(TAG, "onDataRetrievalFailed");
	}

}
