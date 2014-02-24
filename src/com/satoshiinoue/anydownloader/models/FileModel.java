package com.satoshiinoue.anydownloader.models;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLDecoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.satoshiinoue.anydownloader.utilities.Configuration;

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
			String tmpFilename = decodedUrl.substring(decodedUrl.lastIndexOf('/') + 1);
			outputStream = appContext.openFileOutput(tmpFilename, Context.MODE_PRIVATE);
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
		
		//Bug fix for FileNotFoundException (Too many open files) 
		try {
			outputStream.close();
		} catch (IOException e) {
			Log.e("FileNetworkTransaction", "IOException - Error while closing outputStream");
			e.printStackTrace();
		}
	}

	
	
}
