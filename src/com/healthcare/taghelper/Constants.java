package com.healthcare.taghelper;

public interface Constants {
	
	// eHealthCard URLs
	public static final String EHEALTHCARE_DOMAIN = "http://192.168.1.10/ehealthcard/";
	public static final String EHEALTHCARE_URL_LOGIN = EHEALTHCARE_DOMAIN + "login.php";
	public static final String EHEALTHCARE_URL_NEW_TAG_ISSUER = EHEALTHCARE_DOMAIN + "newTagIssuer.php";
	public static final String EHEALTHCARE_URL_GET_WRITE_KEYS = EHEALTHCARE_DOMAIN + "getWriteKeys.php";
	public static final String EHEALTHCARE_URL_GET_READ_KEYS = EHEALTHCARE_DOMAIN + "getReadKeys.php";
	public static final String EHEALTHCARE_URL_DOCTOR = EHEALTHCARE_DOMAIN + "doctor.php";
	public static final String EHEALTHCARE_URL_UPDATE_PATIENT_DATA = EHEALTHCARE_DOMAIN + "updatePatientData.php";
	
	// P2P URLs
	public static final String P2P_DOMAIN = "http://192.168.1.10/p2p/";
	public static final String P2P_URL_LOGIN = P2P_DOMAIN + "login.php";
	
	// eHealthCard Keys
	public static final String KEY_NAME = "name";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_DOB = "dob";
	public static final String KEY_TAG_ID = "tag_id";
	public static final String KEY_ERROR = "error";
	public static final String KEY_S1R = "s1r";
	public static final String KEY_S2R = "s2r";
	public static final String KEY_S3R = "s3r";
	public static final String KEY_S1RW = "s1w";
	public static final String KEY_S2RW = "s2w";
	public static final String KEY_S3RW = "s3w";
	public static final String KEY_DATA = "data";
	public static final String KEY_READ_KEY = "read_key";
	public static final String KEY_PATIENT_NAME = "name";
	public static final String KEY_PATIENT_ID = "id";
	public static final String KEY_PROBLEM = "problem";
	public static final String KEY_DOCTOR_NAME = "doctor";
	public static final String KEY_TEST = "test";
	public static final String KEY_MEDICINE = "medicine";
	
	// P2P Keys
	public static final String patientNameKey = "patientName";
	public static final String patientDOBKey = "patientDOB";
	public static final String section1Key = "section1";
	public static final String section2Key = "section2";
	public static final String doctorNameKey = "doctorName";
	public static final String problemKey = "problem";
	public static final String testKey = "test";
	public static final String medicineKey = "medicine";
	
	// Miscellaneous
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
	public static final String STREAM_CIPHER_KEYWORD = "safestate";
}
