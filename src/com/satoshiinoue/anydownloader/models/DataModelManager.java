package com.satoshiinoue.anydownloader.models;

import java.util.Hashtable;

import com.satoshiinoue.anydownloader.utilities.Configuration;

import android.content.Context;
import android.util.Log;


/**
 * Fully manages the flow of data between all components of the application. Provides
 * an interface which allows for client code to retrieve data in the form of a model,
 * and fills the model by integrating with other system components such as the 
 * ModelManager, the CacheManager, and the NetworkService.
 * 
 */
public class DataModelManager {
	private final String TAG = "DataManager";
	private static DataModelManager instance = null;
	
	private Hashtable<String, DataModel> modelCache = null;

	/**
	 * Constructor - create a new instance of the cache hashtable
	 */
	private DataModelManager() {
		modelCache = new Hashtable<String, DataModel>();
	}
	
	public static DataModelManager getInstance() 
	{
		if (instance == null) {
			instance = new DataModelManager();
		}
		
		return instance;
	}
	/**
	 * Retrieves the model if it already exists in memory, if it doesn't
	 * a new one will be created and returned having not been filled with data.
	 * 
	 * @param url the url in which to associate the data model
	 * @return a DataModel object, whether having been populated with data or empty
	 */
	public DataModel retrieveModel(String url, Context context, ModelType modelType) {
		//check if we already have the model in memory
		if (modelCache.containsKey(url)) {
			//if the model already exists, return it
			return modelCache.get(url);
		}
		else {
			//if the model does not exist, create a new model, add it to the model cache, and return it
			DataModel dataModel = DataModelFactory.createDataModelInstance(url, context, modelType);
			modelCache.put(url, dataModel);
			return dataModel;
		}
	}
	
	/**
	 * @param url a key for the model which need to be clear from the cache
	 */
	public void removeCachedDataAt(String url){
		if (modelCache.containsKey(url)) {
			//if the model already exists, return it
			modelCache.remove(url);
		}
		else {
			if (Configuration.ALLOW_LOGGING) {
				Log.i(TAG, "clearModel() not exist in the cache: url="+url);
			}
		}
	}
	
	/**
	 * clear cached data
	 */
	public void removeCachedDataAll() {
		modelCache.clear();
	}
}
