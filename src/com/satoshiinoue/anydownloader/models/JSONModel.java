package com.satoshiinoue.anydownloader.models;
import org.json.JSONException;
import org.json.JSONObject;
import com.satoshiinoue.anydownloader.utilities.Configuration;

import android.content.Context;
import android.util.Log;

/**
 * Provides a method to retrieve the XML data in DOM form.
 */
public class JSONModel extends TextModel {
	private static final String TAG = "JSONModel";
	private JSONObject json = null;
	
	public JSONModel(String resourceURL, Context context) {
		super(resourceURL, context);
	}
	
	/**
	 * Get the XML DOM object from the model.
	 * 
	 * @return the XML document object
	 */
	public JSONObject getJSON() {
		return json;
	}

	@Override
	protected void fillData() {
		super.fillData();
		parseStringToJSON();
		
	}
	
	private void parseStringToJSON(){
		try {
			json= new JSONObject(textData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
