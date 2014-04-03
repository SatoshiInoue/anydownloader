package com.satoshiinoue.anydownloader.models;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

import com.satoshiinoue.anydownloader.controller.HttpTransactionDownloader;
import com.satoshiinoue.anydownloader.controller.NetworkTransactionManager;
import com.satoshiinoue.anydownloader.utilities.Configuration;

import android.content.Context;
import android.util.Log;

/**
 * The base class for model objects which will contain the data or a path to the data.  
 * Has hooks for a listener interface which allows for notification when the data has been filled,
 * as well as a state 
 */
public abstract class DataModel {
	private static final String TAG = "DataModel";
	
	private int id;//unique ID for this object
	private LinkedList<DataModelListener> listeners = new LinkedList<DataModelListener>();
	protected boolean isDataComplete = false;
	private boolean isRunning = false;
	private boolean showProgress = false;
	protected String url = null;
	private HttpUriRequest httpUriRequest = null;
	private UrlEncodedFormEntity formEntity = null;
	private HashMap<String, String> header = null;
	protected Header[] responseHeaders;
	//private ModelType modelType = null;
	//private NetworkTransaction networkTransaction = null;
	
	protected OutputStream outputStream = null;
	protected Context appContext;//Should be Application context (not activity context)
	
	public final static int FLAGS_NO_DOWNLOAD_RETRIES = 0x01; 
	
	public DataModel(final String url, final Context appContext) {
		this.url = url;
		this.appContext = appContext;
		id = (int) (1000*Math.random()) + url.length();
	}
	public int getId() {
		return this.id;
	}
	
	/**
	 * Retrieve the URL for the requested data
	 * 
	 * @return the URL object for the location of the data
	 */
	public String getDataURL() {
		return url;
	}
	
	public boolean getShowProgress() {
		return showProgress;
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	public HttpUriRequest getHttpUriRequest() {
		return httpUriRequest;
	}
	public HashMap<String, String> getHeader() {
		return header;
	}
	public void setResponseHeaders(final Header[] responseHeaders) {
		this.responseHeaders = responseHeaders;
	}
	/**
	 * Check if data has been set on this model
	 *
	 * @return true if the data is filled and complete, false otherwise
	 */
	public boolean isDataComplete() {
		return isDataComplete;
	}
	
	/**
	 * Add an item to the set of listeners on this model
	 *
	 * @param modelListener the listener to register
	 * @return true if successful, false otherwise
	 */
	public boolean addListener(DataModelListener modelListener) {
		if (modelListener != null) {
			return listeners.add(modelListener);
		}
		else {
			return false;
		}
	}
	
	/**
	 * Remove an item from the set of listeners on this model
	 * 
	 * @param modelListener the listener to unregister
	 * @return true if successful, false otherwise
	 */
	public boolean removeListener(DataModelListener modelListener) {
		if (modelListener != null) {
			return listeners.remove(modelListener);
		}
		else {
			return false;
		}
	}
	
	/**
	 * Remove all listeners associated with this model
	 */
	public void removeAllListeners() {
		listeners.removeAll(listeners);
	}
	/**
	 * This should be defined in the child class b/c the data varies (i.e. XML, JSON, file (path), etc..)
	 * 
	 * 
	 */
	abstract protected void fillData();
	
	abstract protected void prepareOutputStream();
	
	/**
	 * Get data from the URL
	 */
	public void fetch(final DataModelListener listener, final boolean showProgress) {
		//Set the method type
		httpUriRequest = HttpTransactionDownloader.createHttpUriRequest("GET", url, null);
		//Prepare outputStream
		prepareOutputStream();
		//outputStream = new ByteArrayOutputStream();
		isRunning = true;
		this.showProgress = showProgress;
		
		listeners.add(listener);
		//Invoke download
		NetworkTransactionManager.getInstance(appContext).enqueueFetch(this);
	}
	/**
	 * Get data from the URL with header
	 */
	public void fetch(final DataModelListener listener, final boolean showProgress, final HashMap<String, String> header) {
		//Set the method type
		httpUriRequest = HttpTransactionDownloader.createHttpUriRequest("GET", url, null);
		//Set header
		this.header = header;
		
		//Prepare outputStream
		prepareOutputStream();
		//outputStream = new ByteArrayOutputStream();
		isRunning = true;
		this.showProgress = showProgress;
		
		listeners.add(listener);
		//Invoke download
		NetworkTransactionManager.getInstance(appContext).enqueueFetch(this);
	}
	
	/**
	 * Alert all listeners that the data has been filled
	 */
	public void onFetchSuccess() {
		if (Configuration.ALLOW_LOGGING)
			Log.i(TAG, "onFetchSuccess");
		isRunning = false;
		if (isDataComplete) {
			
			synchronized (listeners) {
				//iterate through all the listeners and alert each one there is an update
				for (DataModelListener listener : listeners) {
					listener.onFetchSuccess(this);
				}	
			}
		}
	}
	
	/**
	 * Alert all listeners as the data is being filled.
	 */
	public void onFetchProgress(final int nBytesDownloaded, final int totalBytes) {
		if (Configuration.ALLOW_LOGGING)
			Log.d(TAG, "onFetchProgress" + nBytesDownloaded + "-" + totalBytes);
		//Check the threshold to update the listeners
		//int threshold = totalBytes / 100;
		//if (nBytesDownloaded % threshold == 0) {
			if (!isDataComplete) {	
				synchronized (listeners) {
					//iterate through all the listeners and alert each one there is an update
					for (DataModelListener listener : listeners) {
						listener.onFetchProgress(this, nBytesDownloaded, totalBytes);
					}	
				}
			}
		//}
	}
	
	/**
	 * Alert all listeners that the data retrieval is failed
	 */
	public void onFetchError(int errorCode, String message) {
		isRunning = false;
		if (!isDataComplete) {
			synchronized (listeners) {
				//iterate through all the listeners and alert each one there is an update
				for (DataModelListener listener : listeners) {
					listener.onDataRetrievalFailed(this, errorCode, message);
				}	
			}
		}
	}

	public void refetch() {
		outputStream = new ByteArrayOutputStream();
		isRunning = true;
	}
	public void resume(Context context, String transactionUrl) {
		outputStream = new ByteArrayOutputStream();
		isRunning = true;
	}
	public boolean cancel() {

		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, ">>>>>cancel setting isCanceled on:" + url);
		}
		isRunning = false;
		isDataComplete = false;//Should it be?
		
		// This is probably an erroneous state
		assert (false);
		return false;
	}
	
	public boolean getIsRunning() {
		return isRunning; 
	}
	public void setIsRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
}
