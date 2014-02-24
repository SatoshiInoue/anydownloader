package com.satoshiinoue.anydownloader.models;

/**
 * Provides a mechanism in which objects can be notified about changes for a particular
 * model by registering themselves as listeners on a given DataModel object.
 */
public interface DataModelListener {
	/**
	 * Invoked from DataModel.alertListenersDataComplete()
	 * @param model
	 */
	public void onFetchSuccess(final DataModel model);
	
	public void onFetchProgress(DataModel model, int nBytesDownloaded, int totalBytes);
	/**
	 * Invoked from DataModel.alertListenersDataRetrievalFailed(DataModel model, int errorCode, String message) 
	 * @param model
	 * @param errorCode
	 * @param message
	 */
	public void onDataRetrievalFailed(DataModel model, int errorCode, String message);
}
