package com.satoshiinoue.anydownloader.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.RequestAddCookies;
import org.apache.http.client.protocol.ResponseProcessCookies;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import com.satoshiinoue.anydownloader.models.DataModel;
import com.satoshiinoue.anydownloader.utilities.Configuration;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.StatFs;
import android.util.Log;

public class HttpTransactionDownloader 
{	
	final String TAG = "HttpTransactionDownloader";
	private static DefaultHttpClient configuredClient = null;
	private AtomicBoolean isCancelled = new AtomicBoolean(false);
	private HashMap<Integer, String> errorMapping = null;
	///private String phoneNumber = "";
	private String product="";
	private String versionName = "";

	SSLSocketFactory sslFactory = null;
	
	private Context context;
	
	/**
	 * Constructor
	 * Initializes everything, sets up the custom headers that will be added to each transaction, etc.
	 */
	public HttpTransactionDownloader(HashMap<Integer, String> errorMapping, Context context)
	{
		super();
		this.errorMapping = errorMapping;
		
		this.context = context;
		
	}

	/**
	 * Allows access to the HttpClient that will be used for all network transactions
	 * @return HttpClient object to be used for network transactions.
	 */
	private DefaultHttpClient getConfiguredClient()
	{
		if(configuredClient == null)
		{
			final HttpParams params = new BasicHttpParams();
			ThreadSafeClientConnManager cm = null;
					   
			ConnManagerParams.setMaxTotalConnections(params, 15);
			ConnManagerParams.setTimeout(params, 30000);
			HttpConnectionParams.setSoTimeout(params, 20000);
			HttpConnectionParams.setConnectionTimeout(params, 10000);
			HttpConnectionParams.setTcpNoDelay(params, true);
			HttpConnectionParams.setLinger(params, 0);
			HttpConnectionParams.setSocketBufferSize(params, 512);
			HttpConnectionParams.setStaleCheckingEnabled(params, true);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpClientParams.setRedirecting(params, true);
			
			final SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			/*if(sslFactory != null)
				schemeRegistry.register(new Scheme("https", sslFactory, 443));
			else*/
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

			
			
			//create a thread-safe client manager
			cm = new ThreadSafeClientConnManager(params, schemeRegistry);

			//create a default http client from given params
			configuredClient = new DefaultHttpClient(cm, params);
			
			//allow a maximum of 2 redirections
			configuredClient.getParams().setParameter("http.protocol.max-redirects", new Integer(2));
			
			
			/* set parameters for holding the connection to the server for increased performance */
			
			configuredClient.removeRequestInterceptorByClass(RequestAddCookies.class);
			configuredClient.removeResponseInterceptorByClass(ResponseProcessCookies.class);
			
			configuredClient.setReuseStrategy(new ConnectionReuseStrategy() {
				public boolean keepAlive(final HttpResponse resp, final HttpContext ctx) {
					return true;
				}
			});
			
			configuredClient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
				public long getKeepAliveDuration(HttpResponse arg0, HttpContext arg1) {
					return 30;
				}
			});
		}
		
		return configuredClient;
	}
	
	public void setIsCancelled(boolean cancelled)
	{
		isCancelled.set(cancelled);
	}
	public void setIsCancelled(boolean cancelled, DataModel dataModel)
	{
		if (dataModel!= null)
			Log.e(TAG, "SetisCancelled called for " + dataModel.getDataURL());
		isCancelled.set(cancelled);
	}
	/**
	 * Initiates a fetch (GET) transaction
	 * @param transaction The transaction that should be processed.
	 * @throws Exception Signifies that an error occured during processing the transaction.
	 */
	public void downloadTransaction(DataModel dataModel)
		throws Exception
	{
		assert(dataModel != null);
		
		// Support cancelling the dataModel
		if (isCancelled.get())
		{
			//reset the cancelled flag
			isCancelled.set(false);
			
			//chain the exception to the calling code
			throw new CancelTransactionException();
		}
		
		HttpClient client = getConfiguredClient();

		// Transaction is beginning
		HttpUriRequest request = dataModel.getHttpUriRequest();
		HttpResponse response = null;
		
		HashMap<String, String> header = dataModel.getHeader();
		if (header != null) {
			Iterator<Entry<String, String>> it = header.entrySet().iterator();
			while (it.hasNext()) { 
				Entry<String, String> pairs = (Entry<String, String>) it.next();
				request.addHeader(pairs.getKey(), pairs.getValue());
			}
		}
		
		
		// Sign the request and execute it
		try {			
			response = client.execute(request);			
		}
		catch (Exception e) {
			if (Configuration.ALLOW_LOGGING) {
				Log.e(TAG, "Exception thrown when executing request");
			}
			e.printStackTrace();
			// Pass the exception on to the caller
			throw e;
		}
		dataModel.setResponseHeaders(response.getAllHeaders());
		
		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, "Status line: " + response.getStatusLine());
		}
		
		// Inspect the response body
		HttpEntity responseEntity = response.getEntity();
		int contentLength = (int)responseEntity.getContentLength();

		if (Configuration.ALLOW_LOGGING) {
			Log.i(TAG, "Content-length: " + contentLength);
		}
		// Stop downloading current file if its size bigger than available memory space
		if(/* (dataModel.getClass() == FileModel.class) && */(contentLength>0) ){			
			if (!this.checkForAvailableResources(contentLength/*2*contentLength + Constants.MINIMUM_EXTRA_DOWNLOAD_SPACE*/)){
				if (Configuration.ALLOW_LOGGING) {
					Log.e(TAG,"No space left exception!" );
				}
				throw new IOException(Constants.EXCEPTION_NO_SPACE_LEFT);
			}
		}

		int bytesRead = 0;
		int bytesLastNotified = 0;
		int minimumBytesBetweenEachNotify = Math.max(1024, contentLength / 100); // 1k or 1%, whichever is higher
		int totalBytesRead = 0;
		int bufSize = 1024;
		byte[] buffer = new byte[bufSize];

		InputStream contentStream = responseEntity.getContent();

		try {
			// Read the response body and pump it into the transaction's OutputStream
			while((bytesRead = contentStream.read(buffer, 0, bufSize)) != -1) {
				// Support canceling the transaction
				if (isCancelled.get()) {
					//ensure we don't leave the stream open
					contentStream.close();
					
					//reset the canceled flag
					isCancelled.set(false);
					
					Log.e(TAG, "canceling the transaction while downloading");
					
					//chain the exception to the calling code
					throw new CancelTransactionException();
				}
				
				try {
					dataModel.getOutputStream().write(buffer, 0, bytesRead);
				}
				catch (IOException ioExcept) {
					if (Configuration.ALLOW_LOGGING) {
						Log.e(TAG, "IOException: " + ioExcept.getMessage());
					}
					if (ioExcept.getMessage() != null && ioExcept.getMessage().startsWith(Constants.EXCEPTION_NO_SPACE_LEFT))
					{
						//transaction.onTransactionFailed(transaction, 
						//	      Constants.NET_INSUFFICIENT_STORAGE, ioExcept.getMessage());
						contentStream.close();
						throw(ioExcept);
					}
				}
	
				totalBytesRead += bytesRead;
				/*need it??
				// Notify approximately every "minimumBytesBetweenEachNotify" bytes, or if the totalBytesRead matches (or exceeds, shouldn't happen) the contentLength
				if( (totalBytesRead > (bytesLastNotified + minimumBytesBetweenEachNotify)) || totalBytesRead >= contentLength)
				{
					bytesLastNotified = totalBytesRead;
					dataModel.onFetchSuccess();//.onDataReceived(transaction, totalBytesRead, contentLength);
				}
				*/
				if (dataModel.getShowProgress())
					dataModel.onFetchProgress(totalBytesRead, contentLength);
			}
		}
		catch (SocketTimeoutException socketErr) {
			if (Configuration.ALLOW_LOGGING) {
				Log.e(TAG, "socketTimeoutException on download");
			}
			contentStream.close();
			throw(socketErr);
		}
		catch (IOException ioErr) {
			if (Configuration.ALLOW_LOGGING) {
				Log.e(TAG, "IOException on download");
			}
			contentStream.close();
			throw(ioErr);
		}
		catch (Exception generalErr) {
			if (Configuration.ALLOW_LOGGING) {
				Log.e(TAG, "Exception on download");
			}
			contentStream.close();
			throw(generalErr);
		}
		//cleanup the open stream
		try { contentStream.close(); }
		catch (Exception e) { }
		
		if (totalBytesRead < contentLength) {
			if (Configuration.ALLOW_LOGGING) {
				Log.e(TAG, "Not enough bytes read: THROWING EXCEPTION...");
			}
			IOException ioErr = new IOException();
			throw ioErr;
		}
		
		if(response.getStatusLine().getStatusCode() == 200) {
			if (Configuration.ALLOW_LOGGING) {
				Log.i(TAG, "(HTTP 200) All OK");
			}
			// The transaction has been completed
			dataModel.onFetchSuccess();
			return;
		}
		else if(response.getStatusLine().getStatusCode() == 202) {
			if (Configuration.ALLOW_LOGGING) {
				Log.i(TAG, "(HTTP 202) - Redirect");
			}
			// TODO: We need to redirect here
			return;
		}
		else {
			// Default error
			String errorMessage = "";
			int statusCode = response.getStatusLine().getStatusCode();
			
			try {
				if(errorMapping.containsKey(statusCode)) {
					errorMessage = errorMapping.get(statusCode);
				}
				else if(errorMapping.containsKey(Constants.NET_DEFAULT_ERROR)) {
					errorMessage = errorMapping.get(Constants.NET_DEFAULT_ERROR);
				}
			}
			catch(Exception e) { }
			
			if (Configuration.ALLOW_LOGGING) {
				Log.e(TAG, "(HTTP "+ statusCode+") - ERROR");
			}
			// Notify listeners
			dataModel.onFetchError(statusCode, errorMessage);
		}
	}

	/**
	 * Returns the method type (GET/POST/PUT/DELETE)
	 * @return
	 */
	public static HttpUriRequest createHttpUriRequest(final String httpRequestMethod, final String url, final UrlEncodedFormEntity urlEncodedFormEntity) {
		HttpUriRequest request = null;
		if (httpRequestMethod.equalsIgnoreCase("GET")) {
			final HttpGet get = new HttpGet(url);
			request = get;
		} else if (httpRequestMethod.equalsIgnoreCase("POST")) {
			final HttpPost post = new HttpPost(url);
			request = post;

			// Add a request entity, if one was specified
			if (urlEncodedFormEntity != null) {
				post.setEntity(urlEncodedFormEntity);
			}
		} else if (httpRequestMethod.equalsIgnoreCase("DELETE")) {
			final HttpDelete delete = new HttpDelete(url);
			request = delete;
		}
		// Make sure we created a request method
		assert (request != null);
		return request;
	}
	
	private int getTodaysSecret() {
		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		
		//return the calculated secret for the vzwpix3 authentication
		return (int) Math.floor((((day * 669) + (month * 7) + year) / 4) + day);
	}
	
	/*
	 * This is a duplicate method which also resides in the Utils file, however
	 * a copy of it must remain in this file so if it is used by the PackageInstaller project,
	 * there will not be a crash due to inner dependencies not being accessories to Pakcagine Installer.
	 */
	private boolean checkForAvailableResources(int fileSize) {
		if (context != null) {
		// get free space
		StatFs stats = new StatFs(context.getFilesDir().getAbsolutePath());
		Log.e(TAG, context.getFilesDir().getAbsolutePath());
		int availableSpace = Math.abs(stats.getAvailableBlocks() * stats.getBlockSize());
		if (Configuration.ALLOW_LOGGING) 
			Log.i("UTILS", "avail: " + availableSpace + ", totalBytes= " + fileSize);		
		
		if (availableSpace < fileSize) {
			if (Configuration.ALLOW_LOGGING) {
				Log.i("UTILS", "avail: " + stats.getAvailableBlocks() + ", blksize=" + stats.getBlockSize() + ", totalBytes= " + fileSize);
			}
			return false;
		}
		return true;
		}
		return false;
	}
}
