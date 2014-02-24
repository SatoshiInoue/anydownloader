package com.satoshiinoue.anydownloader.controller;

public class NetworkConstants {

	// The maximum number of transaction download attempts
	public static final int MAX_TRANSACTION_DL_ATTEMPTS = 3;
	/**
	 * The maximum amount, in bytes, that the file system cache for images should stay below. Note because it is a lazy
	 * purge, the cache may grow by at least 1*maximum_image_data_per_screen before being brought back down. Currently
	 * that over-grow size is probably about 512K or so
	 */
	public final static long MB = 1024 * 1024; // 1 MB
	public final static long MAX_IMAGE_SIZE_ON_DISK = 5 * MB; // currently 5MB

	// The maximum number of retries for each transaction type
	public static final int RETRY_DEFAULT_NETWORK_TRANSACTION = 0;

	public static final int NETWORK_THREAD_POOL_SIZE = 3;
	public static final boolean MD5_FILE_NAME_ENABLED = true;
	public static final int DOWNLOAD_PACKET_SIZE = 128;

	// Network error codes
	public static final int NET_DEFAULT_ERROR = -1;
	public static final int NET_BAD_REQUEST_ERROR = 400;
	public static final int NET_CONNECTION_ERROR = 404;
	public static final int NET_INSUFFICIENT_STORAGE = 507;
	public static final int HTTP_RESPONSE_OK = 200;

	/**
	 * NetworkConstants relating to result codes
	 */
	public static final int RESULT_OK = 0x0001;
	public static final int RESULT_CANCEL = 0x0000;
	public static final int RESULT_NO = 0x0000;

	/**
	 * Miscellaneous
	 */
	public static final String STATFS_FOLDER = "/data";

	/**
	 * size constants currently in use
	 */
	public static final int MINIMUM_EXTRA_DOWNLOAD_SPACE = 20000;
	public static final int MAXIMUM_IMAGES_IN_MY_GALLERY = (10*2)+1; //20 at the moment

	/**
	 * Http Header
	 */
	public static final String HTTP_IMEI = "X-IMEI";
	public static final String HTTP_RESOLUTION = "X-Resolution";
	public static final String HTTP_DUMMY_IMEI = "123456781234658";
	public static final String HTTP_LOCALE = "X-Locale";
	public static final String HTTP_TOKEN = "X-Token";
	public static final String HTTP_TIMESTAMP = "X-Timestamp";
	public static final String HTTP_SIGNATURE = "X-Signature";
	public static final String HTTP_COOKIE = "cookie";
	public static final String HTTP_USER_AGENT = "User-Agent";
	public static String g_cookieHeader = "";

	/**
	 * Testing features
	 */
	public static final long SLEEP_DOWNLOAD_DELAY_MS = 0;
	public static final boolean ENFORCE_IMAGE_CACHE_LIMIT = true;
	public static final boolean USE_INDIVIDUAL_TRANSACTION_CANCEL = true;
}
