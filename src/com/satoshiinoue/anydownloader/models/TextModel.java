package com.satoshiinoue.anydownloader.models;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

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
public class TextModel extends DataModel {
	private static final String TAG = "TextModel";
	protected String textData = "";
	
	public TextModel(String resourceURL, Context context) {
		super(resourceURL, context);
	}
	
	public String getTextData() {
		return textData;
	}
		
	@Override
	protected void prepareOutputStream() {
		outputStream = new ByteArrayOutputStream();
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
		convertStreamToString();
		isDataComplete = true;
	}
	public void convertStreamToString() {
		if (outputStream != null) {
			textData = outputStream.toString();
			if (Configuration.ALLOW_LOGGING) {
				Log.i(TAG, textData);
			}
			try {
				outputStream.close();
			} catch (IOException e) {
				Log.e(TAG, "OutputStream close failed");
				e.printStackTrace();
			}
		}
	}

	
	
}
