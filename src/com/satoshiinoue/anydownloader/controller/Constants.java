package com.satoshiinoue.anydownloader.controller;


public class Constants {
	/**
	 * General 
	 */
	
	
	/**
	 * Constants relating to the network service
	 */

	// true - OAuth is used to guard network protocol
	// false - no authentication
	public static final boolean AUTH_ENABLED = true;

	// true - Perform logins via a desktop browser
	// false - Perform logins via the embedded webview in the client
	public static final boolean AUTH_MANUALLY = false;

	// non-null - Forces the app to use this access token/secret

	public static final String   AUTH_FORCE_ACCESS_TOKEN = null;
	public static final String   AUTH_FORCE_ACCESS_TOKEN_SECRET = null;
		
	public static final String tmpApps = "";
	

	
	// The maximum number of request token download attempts
	public static final int MAX_REQUEST_TOKEN_DL_ATTEMPTS = 1;

	// The maximum number of access token download attempts
	public static final int MAX_ACCESS_TOKEN_DL_ATTEMPTS = 1;

	// The maximum number of transaction download attempts
	public static final int MAX_TRANSACTION_DL_ATTEMPTS = 3;

	// The amount of time (in milliseconds) to yield the service thread while waiting for an action
	public static final long NETWORK_SERVICE_WAIT_TIME = 50;

	// The maximum number of retries for each transaction type, using APPMGR_DOWNLOAD_APK_RETRY_INTERVAL
	public static final int RETRY_DEFAULT_NETWORK_TRANSACTION = 0;
	public static final int RETRY_WEB_SERVICE_TRANSACTION = 3;
	public static final int RETRY_POST_RATING_TRANSACTION = 0;
	
	// The key used for the access token preference
	public static final String PREF_ACCESS_TOKEN = "access_token";

	// The key used for the access token secret preference
	public static final String PREF_ACCESS_TOKEN_SECRET = "access_token_secret";


	// Network error codes
	public static final int NET_DEFAULT_ERROR 		 = -1;
	public static final int NET_BAD_REQUEST_ERROR 	 = 400;
	public static final int NET_AUTHENTICATION_ERROR = 401;
	public static final int NET_IMEI_ERROR 			 = 403;
	public static final int NET_CONNECTION_ERROR 	 = 404;
	public static final int NET_SERVER_ERROR 		 = 500;
	public static final int NET_INSUFFICIENT_STORAGE = 507;
	public static final int HTTP_RESPONSE_OK    	 = 200;
	
	
	/**
	 * Constants relating to result codes
	 */
	public static final int RESULT_OK = 0x0001;
	public static final int RESULT_CANCEL = 0x0000;
	public static final int RESULT_NO = 0x0000;
	
	/**
	 * Preference
	 */	
	public static final String PREF_UPDATE_NOTIFICATION = "update notification";
	
	public static final String PREF_ROAMING = "roaming";
	public static final String PREF_NOT_CHECK_WHILE_ROAMING = "not check while roaming";
	public static final String PREF_CHECK_WHILE_ROAMING = "check while roaming";
	
	public static final String PREF_TIMEINTERVAL = "Time Interval";
//	public static final String PREF_NOT_ROAMING = "Not Roaming";
	public static final String PREF_NEVER = "Never";
	
	public static final String PREF_EULA_VERSION = "null";
	
	public static final String PREF_LOCALE = "lang";
	
	
	/**
	 * EXTRAs
	 */
	public static final String EXTRA_URL = "url";
	public static final String EXTRA_IS_LOCKER = "is_locker";
	public static final String EXTRA_TAB_ID = "id";
	public static final String EXTRA_TXT="txt";
	public static final String EXTRA_STATUS_INVALID_APK = "status_invalid_apk";
	public static final String EXTRA_STATUS_DOWNLOADING = "status_downloading";
	public static final String EXTRA_STATUS_INSTALLING = "status_installing";
	public static final String EXTRA_STATUS_INSTALL_COMPLETE = "status_install_complete";
	public static final String EXTRA_STATUS_INSTALL_FAIL = "status_install_fail";
	public static final String EXTRA_APP_ID = "appId";
	public static final String EXTRA_APP_NAME = "appName";
	public static final String EXTRA_APP_STATUS = "app_status";
	public static final String EXTRA_APPLICATION="application";

	public static final String EXTRA_SEARCH = "search";
	public static final String EXTRA_FORWARD_INTENT = "forward_intent";
	public static final String EXTRA_PATH = "path";
	public static final String EXTRA_AUTHORIZED_URL = "authorized_url";
	public static final String EXTRA_AUTHORIZATION_URL = "authorization_url";
	public static final String EXTRA_MESSAGE_STRING = "message_text";
	
	public static final String SEARCH_URL = "url";
	public static final String SEARCH_PARAMS = "params";

	public static final String EXTRA_PERMISSIONS_TEXT = "permissions_text";
	public static final String EXTRA_PERMISSIONS_APPID = "permissions_appid";
	public static final String EXTRA_PERMISSIONS_APPNAME = "permissions_appname";
	public static final String EXTRA_PERMISSIONS_CALLBACK = "permissions_callback";
	
	public static final String EXTRA_WEBVIEW_ERROR_FAILURL = "webview_error_failurl";
	
	public static boolean bAccessTokenHack = false;
	
	/**
	 * Notification
	 */
	public static final int UPDATE_NOTIFY_ID = 0;
	public static final int LOGIN_FAIL_NOTIFY_ID = 1;
	
	/**
	 * Flags for building
	 */
	public static final boolean PRODUCTION_BUILD = true;
	public static String g_resourceCacheUrl = "";
	
	
	/**
	 * Miscellaneous
	 */
	public static final String STATFS_FOLDER = "/data";
	public static final int LOADING_TIMEOUT = 200;
	public static final boolean DELETE_ALL_TEMP_FILES = false;
	
	
	// for minimum free space in order to run
	public static final int MINIMUM_EXTRA_DOWNLOAD_SPACE = 20000;
	public static final int MINIMUM_FREE_SPACE = 1024 * 1000 * 2;	// 2MB
	public static final int INSTALL_BISHOP_REQUIRED_SPACE = 2500000;  // 2.5MB
	public static final int UPGRADE_BISHOP_REQUIRED_SPACE = (575 * 1024 * 2);
	public static final int UPDATE_MRS_CACHE_MINIMUM_SIZE = 1024 * 1500;	// 1.5MB
	
	
	/*
	 * Exceptions
	 */
	//public static final String EXCEPTION_NO_SPACE_LEFT = "No space left";
	
	
	/**
	 * Messages
	 */
	public static final String ERROR_TEXT_XML_PARSE_FAILURE = "Error parsing Data.";
	public static final String ERROR_TEXT_IMAGE_PARSE_FAILURE = "Error parsing Image.";
	public static final String NETWORK_CONNECTION_ERROR = "Network connection error.";
	public static final String EXCEPTION_NO_SPACE_LEFT = "Not enough space on filesystem for download.";
}
