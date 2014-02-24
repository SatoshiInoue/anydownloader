package com.satoshiinoue.anydownloader;

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
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements DataModelListener {

	private static final String TAG = "MainActivity";
	
	private TextView testTextView;
	private ImageView testImageView;
	private DataModel testModel;
	private DataModel testImageModel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		testTextView = (TextView) findViewById(R.id.testTextView);
		testImageView = (ImageView) findViewById(R.id.testImageView);
		
		testModel = DataModelManager.getInstance().retrieveModel("http://www.w3schools.com/xml/note.xml", this.getApplicationContext(), ModelType.XMLModel);
		//testModel = DataModelManager.getInstance().retrieveModel("http://google.com", this.getApplicationContext(), ModelType.TextModel);
		//testModel = DataModelManager.getInstance().retrieveModel("http://serene-sierra-8637.herokuapp.com/wines", this.getApplicationContext(), ModelType.JSONModel);
		if (!testModel.isDataComplete()) {
			testModel.fetch(this, false);
		} else {
			//testTextView.setText(((TextModel) testModel).getTextData());
			//testTextView.setText(((XMLModel) testModel).getTextData());
			testTextView.setText(((JSONModel) testModel).getTextData());
		}
		
		testImageModel = DataModelManager.getInstance().retrieveModel("https://www.google.com/images/srpr/logo5w.png", this.getApplicationContext(), ModelType.FileModel);
		if (!testImageModel.isDataComplete()) {
			testImageModel.fetch(this, false);
		} else {
			//create a drawable
			final Drawable drawable = Drawable.createFromPath(((FileModel) testImageModel).getPathOnDisk());
			testImageView.setImageDrawable(drawable);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
	public void onFetchProgress(final DataModel model, final int nBytesDownloaded,
			final int totalBytes) {
		
	}

	@Override
	public void onDataRetrievalFailed(DataModel model, int errorCode,
			String message) {
		// TODO Auto-generated method stub
		
	}

}
