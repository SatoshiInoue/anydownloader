package com.satoshiinoue.anydownloader.models;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.satoshiinoue.anydownloader.utilities.Configuration;

import android.content.Context;
import android.util.Log;

/**
 * Provides a method to retrieve the XML data in DOM form.
 */
public class XMLModel extends TextModel {
	private static final String TAG = "XMLModel";
	private Document xmlDocument = null;
	
	public XMLModel(String resourceURL, Context context) {
		super(resourceURL, context);
	}
		
	/**
	 * Set the XML DOM object for this data model.
	 * 
	 * @param document the DOM object to set on the model
	 */
	public void setXMLDocument(Document document) {
		if (document != null) {
			xmlDocument = document;
			isDataComplete = true;
			
			//now that we have set the path, we can alert the listeners
			//alertListenersDataComplete();
		}
	}
	
	/**
	 * Get the XML DOM object from the model.
	 * 
	 * @return the XML document object
	 */
	public Document getXMLDocument() {
		return xmlDocument;
	}
	/*
	@Override
	public void onFetchSuccess() {
		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, "onFetchSuccess");
		}
		fillData();
		super.onFetchSuccess();
	}
	*/
	@Override
	protected void fillData() {
		super.fillData();
		processXMLTransaction();
		
	}
	
	private void processXMLTransaction(){
		Document document = null;
		String xmlData = null;
		try {
			//using the original xml string data, construct a document builder object
		    xmlData = textData;
		    if (Configuration.ALLOW_LOGGING) {
		    	Log.e(TAG, xmlData);
		    }
			//construct the document builder object
			final ByteArrayInputStream byteStream = new ByteArrayInputStream(xmlData.getBytes());
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder(); 
			
			if (Configuration.ALLOW_LOGGING) {
				Log.d(TAG, byteStream.toString());
			}
			
			//parse the xml into a document object
			document = db.parse(byteStream);
			
			//normalize the document so we don't have unusable nodes within it
			document.getDocumentElement().normalize();
		}
		catch (Exception ex) {
			if (Configuration.ALLOW_LOGGING) {
				Log.e(TAG, "Error parsing the XML. Details: " + ex.getLocalizedMessage());
			}
			ex.printStackTrace();
			
			onFetchError(101, "Error parsing the XML");
	
		}

		if (document != null) {
			setXMLDocument(document);
		}		
	}
}
