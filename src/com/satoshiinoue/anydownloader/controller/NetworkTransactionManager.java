package com.satoshiinoue.anydownloader.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.w3c.dom.Document;

import com.satoshiinoue.anydownloader.models.DataModel;
import com.satoshiinoue.anydownloader.utilities.Configuration;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.Log;

/**
 */
public class NetworkTransactionManager {
	private final String TAG = "NetworkTransactionManager";

	// Singleton variable
	private static NetworkTransactionManager instance = null;
	
	private Context context;

	// Whether we're currently processing a download
	private static AtomicBoolean processingDownload = new AtomicBoolean(false);

	// Is the application currently running in the foreground?
	private static AtomicBoolean applicationRunning = new AtomicBoolean(false);

	// The queue of transactions that we need to process
	private static volatile LinkedBlockingQueue<DataModel> requestQueue = new LinkedBlockingQueue<DataModel>();

	private static HttpTransactionDownloader httpTransactionDownloader = null;

	// The fixed pool of threads used to process network transactions
	final ExecutorService mThreadPool = Executors.newFixedThreadPool(NetworkConstants.NETWORK_THREAD_POOL_SIZE);

	private ArrayList<DataModel> runningRequests = new ArrayList<DataModel>();

	private HashMap<Integer, String> errorHashMap;

	@SuppressWarnings("serial")
	private class QueueNotServicableException extends Exception {
		private int errorCode = 0;

		public QueueNotServicableException(final String message, final int errorCode) {
			super(message);
			this.errorCode = errorCode;
		}

		public int getErrorCode() {
			return errorCode;
		}
	};

	class TransactionThread implements Runnable {
		public void run() {
			/*
			 * while(true) { // Nothing in the queue?
			 * while(requestQueue.size() == 0) { // Is the application not
			 * running? if(applicationRunning.get() == false) { // Stop the
			 * service Log.i(TAG,
			 * "Nothing in queue and app isn't running anymore.  Stopping service..."
			 * ); stopSelf(); return; } }
			 */

			if (requestQueue.size() > 0) {
				try {
					if (Configuration.ALLOW_LOGGING) {
						Log.i(TAG, "Attempting to service the queue");
					}
					serviceQueue();
				}

				// If the queue isn't servicable, notify all transactions of
				// failure
				catch (final QueueNotServicableException e) {
					if (Configuration.ALLOW_LOGGING) {
						Log.i(TAG, "Queue not servicable");
						e.printStackTrace();
					}

					LinkedBlockingQueue<DataModel> tempQueue = requestQueue;
					requestQueue = new LinkedBlockingQueue<DataModel>();

					for (final DataModel d : tempQueue) {
						d.onFetchError(e.getErrorCode(), e.getMessage());
					}

					tempQueue.clear();
					tempQueue = null;
				}
			}
			// }
		}
	};

	/**
	 * Constructor
	 * @param context
	 */
	public NetworkTransactionManager(final Context context) {
		// Set Context
		this.context = context;
		
		// create httpdownloader object
		errorHashMap = getErrorHashMap();
		httpTransactionDownloader = new HttpTransactionDownloader(errorHashMap, context);
		applicationRunning.set(true);
	}
	
	public static NetworkTransactionManager getInstance(final Context context) {
		
		if(instance == null) {
			instance = new NetworkTransactionManager(context);
		}
		
		return instance;
	}

	public void setApplicationRunning(final boolean isRunning) {
		applicationRunning.set(isRunning);
	}

	private HashMap<Integer, String> getErrorHashMap() {
		final HashMap<Integer, String> errorMap = new HashMap<Integer, String>();
		errorMap.put(NetworkConstants.NET_CONNECTION_ERROR, Constants.NETWORK_CONNECTION_ERROR);
		errorMap.put(NetworkConstants.NET_DEFAULT_ERROR, Constants.NETWORK_CONNECTION_ERROR);
		return errorMap;
	}

	/**
	 * Should be called when to exit the application
	 */
	public void destroy() {
		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, "Network Service in onDestroy, cancelling outstanding requests, clearing pool.");
		}
		cancelAllTransactions();
		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, "Network Service in onDestroy, shutting down thread pool.");
		}

		try {
			if (!mThreadPool.isTerminated()) {
				// mThreadPool.shutdownNow();
				shutdownAndAwaitTermination(mThreadPool);
			}
		} catch (final Exception e) {
			if (Configuration.ALLOW_LOGGING) {
				Log.e(TAG, "Error destroying thread pool: " + e.getLocalizedMessage());
			}
		}

		instance = null;
		httpTransactionDownloader = null;
		applicationRunning.set(false);
	}

	/*
	public XMLNetworkTransaction downloadXMLData(final String url) {
		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, "downloadXMLData: " + url);
		}
		final XMLNetworkTransaction newTransaction = new XMLNetworkTransaction(url, "GET", null);
		return newTransaction;
	}

	public FileNetworkTransaction downloadFileToDisk(final String url) {
		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, "downloadFileToDisk: " + url);
		}
		final FileNetworkTransaction newTransaction = new FileNetworkTransaction(context, url);
		return newTransaction;
	}
	 */
	public boolean cancelTransaction1(final DataModel dataModel) {
		// TODO: needs to be reworked!!
		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, ">>>>cancelTransaction - " + dataModel.getDataURL());
		}
		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, ">>>>cancelTransaction - calling cancel on: " + dataModel.getDataURL());
		}
		httpTransactionDownloader.setIsCancelled(true);
		dataModel.cancel();
		if (requestQueue.contains(dataModel)) {
			if (Configuration.ALLOW_LOGGING) {
				Log.i(TAG,
						">>>>cancelTransaction - Transaction in queue. remove it! " + dataModel.getDataURL());
			}
			requestQueue.remove(dataModel);
		} else {
			// the transaction is not in the queue and therefore not able to be
			// canceled
			if (Configuration.ALLOW_LOGGING) {
				Log.i(TAG,
						">>>>cancelTransaction - Transaction *not* in queue, no need to remove it... "
								+ dataModel.getDataURL());
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Remove the transaction from the queue. If it's not there, then set cancel to Http
	 * @param transaction
	 */
	public void cancelTransaction(final DataModel dataModel) {
		
		if (requestQueue.contains(dataModel)) {
			if (Configuration.ALLOW_LOGGING) {
				Log.i(TAG,
						">>>>cancelTransaction - Transaction in queue. remove it! " + dataModel.getDataURL());
			}
			requestQueue.remove(dataModel);
		} else {
			// the transaction is not in the queue and therefore not able to be
			// canceled
			if (Configuration.ALLOW_LOGGING) {
				Log.i(TAG,
						">>>>cancelTransaction - Transaction *not* in queue, no need to remove it... "
								+ dataModel.getDataURL());
			}
			
			httpTransactionDownloader.setIsCancelled(true);
			//Log.e(TAG, "iscompleted?: " + dataModel.isTransactionCompleted() + " for " + dataModel.getDataURL());
			//transaction.cancel();
			
		}
	}

	synchronized public boolean cancelAllTransactions() {
		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, ">>>>cancelAllTransactions - requestQueue.size=" + requestQueue.size()
					+ " - mThreadPool.isShutdown" + mThreadPool.isShutdown());
		}
		int i = 0;
		for (final DataModel dataModel : requestQueue) {
			/*if (Configuration.ALLOW_LOGGING) {
				Log.i(TAG,
						">>>>cancelAllTransactions - i=" + (i++) + " " + "isTransactionCompleted="
								+ dataModel.isTransactionCompleted() + " " + dataModel.getDataURL());
			}*/
			cancelTransaction(dataModel);
		}
		i = 0;
		synchronized (runningRequests) {
			for (final DataModel d : runningRequests) {
				if (Configuration.ALLOW_LOGGING) {
					Log.i(TAG,
							">>>>cancelAllTransactions - runningRequests - i=" + (i++) + " "
									+ d.getDataURL());
				}
				cancelTransaction(d);
			}
			runningRequests.clear();
		}
		requestQueue.clear();
		return true;
	}
	/*
	public DataModel findFirstTransactionWithType(final Object transactionType) {
		// iterate through list of transactions to see if the type exists
		for (final NetworkTransaction transaction : requestQueue) {
			if (transaction.getClass() == transactionType && !transaction.isTransactionCompleted()) {
				if (Configuration.ALLOW_LOGGING) {
					Log.i(TAG, "findFirstTransactionWithType - found a transaction match int he queue! Type: "
							+ transaction.getClass().toString());
				}
				return transaction;
			}
		}
		return null;
	}*/

	public boolean enqueueFetch(final DataModel dataModel) {
		if (Configuration.ALLOW_LOGGING)
			Log.e(TAG, "enqueueNetworkTransaction - " + dataModel.getDataURL());
		
		// check if the transaction is already running
		synchronized (runningRequests) {
			if (!runningRequests.contains(dataModel)) {
				// check if the transaction is already in the queue
				if (!requestQueue.contains(dataModel)) {
					try {
						//transaction.resume();
						// if not in queue, add it to the queue
						requestQueue.add(dataModel);
					} catch (final IllegalStateException ise) {
						if (Configuration.ALLOW_LOGGING) {
							Log.e(TAG, "enqueueNetworkTransaction - error adding transaction to queue.");
							ise.printStackTrace();
						}
						return false;
					}
		
					// kickQueue();
					mThreadPool.execute(new TransactionThread());
					return true;
				}
			}
		}
		return false;
	}

	private void serviceQueue() throws QueueNotServicableException {
		DataModel currentRunningRequest = null;

		try {
			// grab a reference to the item at the head of the queue and
			// remove it so other threads don't begin processing it
			if (!requestQueue.isEmpty()) {
				currentRunningRequest = requestQueue.remove();

				if (Configuration.ALLOW_LOGGING) {
					Log.i("PAT", "runningRequests.add " + currentRunningRequest.getDataURL());
				}
				synchronized (runningRequests) {
					runningRequests.add(currentRunningRequest);
				}
			}
		} catch (final Exception e) {
			if (Configuration.ALLOW_LOGGING) {
				e.printStackTrace();
				Log.i(TAG, "Failed to peek at transaction queue...  It appears the queue is empty");
			}
			currentRunningRequest = null;
			return;
		}

		processingDownload.set(true);

		// retry the download 'MAX_TRANSACTION_DL_ATTEMPTS' times if an
		// IOException occurs
		int retryCount = 0;
		while (retryCount < NetworkConstants.MAX_TRANSACTION_DL_ATTEMPTS) {
			try {
				// begin the downloading of the transaction
				Log.d(TAG, "dlTransaction count:" + retryCount + " " + currentRunningRequest.getDataURL());
				httpTransactionDownloader.downloadTransaction(currentRunningRequest);
			} catch (final CancelTransactionException cte) {
				// the transaction has been cancelled
				if (Configuration.ALLOW_LOGGING) {
					Log.e(TAG, "Transaction has been cancelled. Exception: " + cte.getMessage() + " " + currentRunningRequest.getDataURL());
				}
				
				currentRunningRequest.cancel();
			} catch (final IOException ioe) {
				// if we get an io exception, there was an error during the
				// download - try N times and then fail
				// except if there is "no space on device" - no need to retry -
				// just fail (TT25917)
				if (Configuration.ALLOW_LOGGING) {
					Log.e(TAG, "Failed downloading transaction. IOException: " + ioe.getMessage());
				}
				if ((ioe.getMessage() != null)
						&& ioe.getMessage().startsWith(Constants.EXCEPTION_NO_SPACE_LEFT)) {
					// if there's no space, no need to retry..
					currentRunningRequest.onFetchError(NetworkConstants.NET_INSUFFICIENT_STORAGE, ioe.getMessage());
					retryCount = 0; // this is so we don't call onTransactionFailed twice
					break;
				} else {
					if (Configuration.ALLOW_LOGGING) {
						ioe.printStackTrace();
					}
					retryCount++;
					/*if (currentRunningRequest.isFlagSet(DataModel.FLAGS_NO_DOWNLOAD_RETRIES)) {
						// if this flag is set, force the retry count to max to prevent download reattempt
						retryCount =  NetworkConstants.MAX_TRANSACTION_DL_ATTEMPTS;
					}*/
					// go back up to the top of while loop
					continue;
				}
			} catch (final Exception e) {
				currentRunningRequest.onFetchError(NetworkConstants.NET_CONNECTION_ERROR, 
						errorHashMap.get(NetworkConstants.NET_CONNECTION_ERROR));
				if (Configuration.ALLOW_LOGGING) {
					e.printStackTrace();
					Log.e(TAG, "Failed to download transaction. Exception: " + e.getMessage());
				}
				retryCount = 0; // this is so we don't call onTransactionFailed twice
			}
			// if we get here, break out of while loop
			break;
		}

		if (retryCount >= NetworkConstants.MAX_TRANSACTION_DL_ATTEMPTS) {
			// we could not process the transaction because an exception has occured
			currentRunningRequest.onFetchError(NetworkConstants.NET_CONNECTION_ERROR, 
					errorHashMap.get(NetworkConstants.NET_CONNECTION_ERROR));
		}

		processingDownload.set(false);
		if (Configuration.ALLOW_LOGGING) {
			Log.i("PAT", "runningRequests.remove " + currentRunningRequest.getDataURL());
		}
		runningRequests.remove(currentRunningRequest);
	}

	void shutdownAndAwaitTermination(final ExecutorService pool) {
		List<Runnable> remaingTaskList = pool.shutdownNow(); // Disable new tasks from being submitted
		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, "Waiting on " + remaingTaskList.size() + " tasks to shutdown.");
		}
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
				if (Configuration.ALLOW_LOGGING) {
					Log.i(TAG, "Pool did not terminate");
				}
			} else {
				if (Configuration.ALLOW_LOGGING) {
					Log.i(TAG, "Pool terminated");
				}
			}
		} catch (final InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}
}
