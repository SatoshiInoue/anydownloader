package com.satoshiinoue.anydownloader;

import com.satoshiinoue.anydownloader.RequestService.RequestServiceBinder;
import com.satoshiinoue.anydownloader.models.DataModel;
import com.satoshiinoue.anydownloader.models.DataModelListener;
import com.satoshiinoue.anydownloader.models.DataModelManager;
import com.satoshiinoue.anydownloader.models.FileModel;
import com.satoshiinoue.anydownloader.models.JSONModel;
import com.satoshiinoue.anydownloader.models.ModelType;
import com.satoshiinoue.anydownloader.models.TextModel;
import com.satoshiinoue.anydownloader.models.XMLModel;
import com.satoshiinoue.anydownloader.utilities.Configuration;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity implements DataModelListener {

	private static final String TAG = "MainActivity";
	
	private TextView testTextView;
	private ImageView testImageView;
	private ProgressBar downloadProgressBar;
	private DataModel testModel;
	private DataModel testImageModel;
	
	private RequestService mService;
	private boolean isBound;
	private ServiceConnection mServiceConnection = new ServiceConnection() {

	    public void onServiceConnected(ComponentName name, IBinder service) {
	    	Log.d(TAG, "onServiceConnected");
	    	RequestServiceBinder binder = (RequestServiceBinder) service;
	    	mService = binder.getService();
	        //isBound = true;
	    	DataModel serviceModel = DataModelManager.getInstance().retrieveModel("http://upload.wikimedia.org/wikipedia/commons/3/3d/LARGE_elevation.jpg", getApplicationContext(), ModelType.FileModel);
	    	mService.fetch(serviceModel, true, null);
	    }
	    
	    public void onServiceDisconnected(ComponentName name) {
	    	Log.d(TAG, "onServiceDisconnected");
	        //isBound = false;
	    }
	    
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		testTextView = (TextView) findViewById(R.id.testTextView);
		testImageView = (ImageView) findViewById(R.id.testImageView);
		downloadProgressBar = (ProgressBar) findViewById(R.id.downloadProgressBar);
		
		Intent intent = new Intent(this, RequestService.class);
		isBound = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "isBound - onServiceConnected");
		//testModel = DataModelManager.getInstance().retrieveModel("http://www.w3schools.com/xml/note.xml", this.getApplicationContext(), ModelType.XMLModel);
		//testModel = DataModelManager.getInstance().retrieveModel("http://google.com", this.getApplicationContext(), ModelType.TextModel);
		testModel = DataModelManager.getInstance().retrieveModel("http://gomashup.com/json.php?fds=geo/usa/zipcode/state/IL", this.getApplicationContext(), ModelType.JSONModel);
		if (!testModel.isDataComplete()) {
		//	testModel.fetch(this, false);
		} else {
			//testTextView.setText(((TextModel) testModel).getTextData());
			//testTextView.setText(((XMLModel) testModel).getTextData());
			testTextView.setText(((TextModel) testModel).getTextData());
		}
		
		testImageModel = DataModelManager.getInstance().retrieveModel("https://www.google.com/images/srpr/logo11w.png", this.getApplicationContext(), ModelType.FileModel);
		if (!testImageModel.isDataComplete()) {
		//	testImageModel.fetch(this, true);
		} else {
			//create a drawable
			final Drawable drawable = Drawable.createFromPath(((FileModel) testImageModel).getPathOnDisk());
			testImageView.setImageDrawable(drawable);
		}
		
		//DataModel serviceModel = DataModelManager.getInstance().retrieveModel("http://upload.wikimedia.org/wikipedia/commons/3/3d/LARGE_elevation.jpg", this.getApplicationContext(), ModelType.FileModel);
	//	if (isBound)
		//	mService.fetch(serviceModel, true);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//stopService(new Intent(getActivity(), RequestService.class));
		unbindService(mServiceConnection);
	}

	@Override
	public void onFetchSuccess(final DataModel model) {
		if (Configuration.ALLOW_LOGGING)
			Log.d(TAG, "onFetchSuccess");
		//Enum test = (Enum) model.getClass();
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				switch (ModelType.valueOf( model.getClass().getSimpleName() )) {
				case TextModel:
					testTextView.setText(((TextModel) model).getTextData());
					break;
				case JSONModel:
					testTextView.setText(((JSONModel) model).getTextData());
					break;
				case XMLModel:
					testTextView.setText(((XMLModel) model).getTextData());
					break;
				case FileModel:
					final Drawable drawable = Drawable.createFromPath(((FileModel) testImageModel).getPathOnDisk());
					testImageView.setImageDrawable(drawable);
					break;
				default:
					break;
				}
				
			}
			
		});
	}

	@Override
	public void onFetchProgress(final DataModel model, final int nBytesDownloaded, final int totalBytes) {
		
		if (Configuration.ALLOW_LOGGING)
			Log.d(TAG, "onFetchProgress" + (int) (((double)nBytesDownloaded/totalBytes)*100) );
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				downloadProgressBar.setProgress((int) (((double)nBytesDownloaded/totalBytes)*100));
			}
		});	
	}

	@Override
	public void onDataRetrievalFailed(DataModel model, int errorCode,
			String message) {
		// TODO Auto-generated method stub
		
	}

}
