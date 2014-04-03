package com.satoshiinoue.anydownloader.utilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Used to provide the application with reusable static methods
 * primarily used as utility functions.
 */
public class Utils {
	final private static String TAG = "Utils";
	/**
	 * encode the Url string into x-www-form-urlencoded format
	 * @param url the url need to be coded
	 */
	public static String encodeDestString(String url) {		
		if (url!=null){
			String newUrl = null;
			String scheme = "http";
			URI uri = null;
			//WTF - This makes no sense - CIS 9/7/10
				//it would be smarter to just call URI(url)
				//but even the existence of this function is dubious
				//will just adjust the existing code as to not introduce too much change
			
			if(url.startsWith("https"))
			{
				scheme = "https";
				newUrl = url.replaceFirst("https://", "");
			}
			else
			{
				newUrl = url.replaceFirst("http://", "");
			}
			
			try {
				uri = new URI(scheme,"//"+newUrl, null);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
			return uri.toString();
		} else {
			return null;
		}
	}
	
	/**
	 * Decode the Url string
	 * @param url the url need to be decoded
	 */
	public static String decodeDestString(String url){		
		String newUrl = null;
		if (url!=null){
			try {
				newUrl = URLDecoder.decode(url, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("Decoder","original url = "+url);
			Log.i("Decoder","new url = "+newUrl);
			return newUrl.toString();
		} else {
			return null;
		}
	}
	
	/**
	 * 	Convert data from String object to Document object
	 * 	@param data string object that need to be converted
	 */
	public static Document convertStringToDocument(String data)
	{
		Document document = null;		
		if (data!=null)
		{
			try {
				//construct the document builder object
				ByteArrayInputStream byteStream = new ByteArrayInputStream(data.getBytes());
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder(); 
				
				//parse the xml into a document object
				document = db.parse(byteStream);
				
				//normalize the document so we don't have unusable nodes within it
				document.getDocumentElement().normalize();
			}
			catch (Exception ex) {
				Log.e("DataManager", "Error parsing the XML. Details: " + ex.getLocalizedMessage());
				ex.printStackTrace();	
				return null;
			}
		}
		return document;
	}
	
	/**
	 * 	Convert xml layout to a view
	 */
	static public View getView(Context context, int layoutId){
		View view = null;
        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = vi.inflate(layoutId, null);   
        return view;
	}
	
	
	/**
	 * return the phone number on the current handset if there is one
	 * otherwise, return an empty string
	 */	
	static public String getMyPhoneNumber(Context context){   
	    TelephonyManager mTelephonyMgr;   
	    mTelephonyMgr = (TelephonyManager)   
	        context.getSystemService(Context.TELEPHONY_SERVICE);    
	    String number = mTelephonyMgr.getLine1Number();   
  
	    if (number==null){
	    	number = ""; // set it as an empty string
	    }
	    return number;   
	}   
	static public String getAppVersion(Context context){   
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
			
		} catch (final NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}   
	static public Bitmap getBitmapFromPath(final String path,
			final int scaleReduce) {

		// final Drawable drawable = Drawable.createFromPath(m.getAbsolutePathToImage());
		// final BitmapDrawable tempBigImage = new BitmapDrawable(getResources(), m.getAbsolutePathToImage());
		// final Bitmap tempBitmap = tempBigImage.getBitmap();
		// final int halfWidth = tempBitmap.getWidth() / scaleReduce;
		// final int halfHeight = tempBitmap.getHeight() / scaleReduce;
		// final Bitmap tempThumbnailBitmap = Bitmap.createScaledBitmap(tempBitmap, halfWidth, halfHeight, true);
		// // view.setImageDrawable(Drawable.createFromPath(model.getAbsolutePathToImage()));
		// final BitmapDrawable tempThumbnailDrawable = new BitmapDrawable(tempThumbnailBitmap);
		// return tempThumbnailDrawable;
		final BitmapFactory.Options bitmapCreationOptions = new BitmapFactory.Options();
		// get the actual size of the stored image
		bitmapCreationOptions.inJustDecodeBounds = true;
		// DEL: final Bitmap bitmapInfo = BitmapFactory.decodeFile(m.getAbsolutePathToImage(), bitmapCreationOptions);
		// DEL: final int imageWidth = bitmapCreationOptions.outWidth;
		// DEL: final int imageHeight = bitmapCreationOptions.outHeight;
		// now create the actual image
		bitmapCreationOptions.inJustDecodeBounds = false;
		bitmapCreationOptions.inDither = true;
		bitmapCreationOptions.inSampleSize = scaleReduce;
		final Bitmap imageBitmap = BitmapFactory.decodeFile(path, bitmapCreationOptions);
		//final BitmapDrawable tempThumbnailDrawable = new BitmapDrawable(imageBitmap);
		return imageBitmap;

	}
	/**
	 * 
	 */
	public static String escape$(String original) {
		return original.replace("\\$", "$");
	}
	public static String html2Str(String original) {
		return android.text.Html.fromHtml(original).toString();
	}
	
	
	/**
	 * 
	 * @param context current application context
	 * @return true: iff either Wifi or 3G is available, otherwise, return false
	 */
	static public boolean isNetworkAvailable(Context context){
		ConnectivityManager connect = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if ( (connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState()== NetworkInfo.State.CONNECTED) 
				||  (connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState()== NetworkInfo.State.CONNECTED))
			return true;
		else if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 8 
				&& connect.getActiveNetworkInfo() != null
				&& connect.getActiveNetworkInfo().getType() == 6) //6 == TYPE_WIMAX 
			return true;
		 
		return false;
	}
	
	/**
	 * Legacy method from MB version2*
	 */
	public static void changeOldSndfiles() {
		String musicBoxPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MusicBox";
		boolean exist = (new File(musicBoxPath)).exists();
		if (exist) {
			String RingtonePath = musicBoxPath + "/Ringtones";
			File ringtone = (new File(RingtonePath));
			boolean FulltrackExist = ringtone.exists();
			File[] listOfFiles;
			File oldFile, newFile;
			int cnt;
			String str[];
			if (FulltrackExist) {
				listOfFiles = ringtone.listFiles();
				cnt = listOfFiles.length;
				str = new String[cnt];
				for (int i = 0; i < listOfFiles.length; i++) {
					str[i] = listOfFiles[i].getAbsolutePath();
					if (str[i].contains(".snd")) {
						str[i] = str[i].replace("snd", "mp4");
						newFile = new File(str[i]);
						oldFile = listOfFiles[i].getAbsoluteFile();
						oldFile.renameTo(newFile);
					}
				}
			}
		}
	} //End of changeOldSndfiles

	/** SINOUE 6/20/2011 -- modified getUserAgent
	 * This method does not instantiate WebView 
	 * unless it gets exceptions (e.g. due to API changes in future Android versions).
	 * Creating WebView was the problem because it runs on the UI thread 
	 * and the child methods such as MusicBoxParser.getApi() might be called in the bg thread,
	 * and which will cause a runtime exception. 
	 * Using Java Reflect, it creates WebSettings instance directly from package-visible constructor.
	 */
	public static String getUserAgent(final Context context) {
		try {
			Constructor<WebSettings> constructor = WebSettings.class.getDeclaredConstructor(Context.class, WebView.class);
			constructor.setAccessible(true);
			try {
				WebSettings settings = constructor.newInstance(context, null);
				return settings.getUserAgentString();
			} finally {
				constructor.setAccessible(false);
			}
		} catch (Exception e) {
			if(Thread.currentThread().getName().equalsIgnoreCase("main")){
				WebView webview = new WebView(context);
				return webview.getSettings().getUserAgentString();
			} else{
				final StringBuffer ret = new StringBuffer();
				((Activity) context).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						WebView webview = new WebView(context);
						ret.append(webview.getSettings().getUserAgentString());
					}
				
				});
				return ret.toString();
			}
		}
	}
}
