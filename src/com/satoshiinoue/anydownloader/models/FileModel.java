package com.satoshiinoue.anydownloader.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;

import org.apache.http.Header;

import com.satoshiinoue.anydownloader.utilities.Configuration;
import com.satoshiinoue.anydownloader.utilities.ContentDispositionParser;

import android.content.Context;
import android.util.Log;

/**
 * Provides a method to retrieve the XML data in DOM form.
 */
public class FileModel extends DataModel {
	private static final String TAG = "FileModel";
	
	private File tempFile = null;	
	private String pathOnDisk = null;
	
	public FileModel(String resourceURL, Context context) {
		super(resourceURL, context);
	}
	
	public String getPathOnDisk() {
		return pathOnDisk;
	}
	
	@Override
	protected void prepareOutputStream() {
		try {
			// TODO: should we be storing images in a subdirectory?
			String decodedUrl = URLDecoder.decode(url);
			String tmpFilename = decodedUrl.substring(decodedUrl.lastIndexOf('/') + 1) + ".apk";
			outputStream = appContext.openFileOutput(tmpFilename, Context.MODE_WORLD_READABLE);
			tempFile = new File(appContext.getFilesDir().getAbsolutePath() + "/" + tmpFilename);
			tempFile.deleteOnExit();
		} 
		catch (FileNotFoundException e) {
			Log.e("FileNetworkTransaction", "FileNetworkTransaction() - Error creating file");
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void onFetchSuccess() {
		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, "onFetchSuccess");
		}
		fillData();
		super.onFetchSuccess();
	}	

	@Override
	protected void fillData() {
		isDataComplete = true;
		pathOnDisk = tempFile.getAbsolutePath();
		if (responseHeaders != null) {
			for (Header header: responseHeaders) {
				if (header.getName().equalsIgnoreCase("Content-Disposition")) {
					Log.d(TAG, header.getValue());
					//Log.d(TAG, ContentDispositionParser.getValue(header.getValue(), "filename"));
					renameFile(ContentDispositionParser.getValue(header.getValue(), "filename"));
				}
			}
		}
		//pathOnDisk = tempFile.getAbsolutePath();
		Log.d(TAG, pathOnDisk);
		
		//Bug fix for FileNotFoundException (Too many open files) 
		try {
			outputStream.close();
		} catch (IOException e) {
			Log.e("FileNetworkTransaction", "IOException - Error while closing outputStream");
			e.printStackTrace();
		}
	}
	public void renameFile(final String fileName) {
		File newFile = new File(appContext.getFilesDir().getAbsolutePath() + "/" + fileName);
		newFile.deleteOnExit();
		if(tempFile.renameTo(newFile)){
			pathOnDisk = newFile.getAbsolutePath();
		} else {
			Log.e(TAG, "File Rename fail");
		}
		
	}
	
	
}
