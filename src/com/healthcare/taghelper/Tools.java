package com.healthcare.taghelper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.healthcare.beans.EncryptedMessageBean;
import com.healthcare.beans.HealthRecordBean;
import com.healthcare.beans.MessageBean;
import com.healthcare.beans.SectionDataBean;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

public class Tools {
	
	public static void debugLog(String TAG, String msg) {
		if(TAG == null) TAG = "untagged";
		Log.i(TAG, msg);
	}
	
	public static void showNFCSettingsDialogBox(final Context context) {
		Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Alert");
        builder.setMessage("This app requires NFC for working. Please turn on the NFC adapter from settings.");
        builder.setPositiveButton("Open settings", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent setnfc = new Intent(Settings.ACTION_WIRELESS_SETTINGS);  
				context.startActivity(setnfc);
			}
		});
        AlertDialog dialog = builder.create();
        dialog.show();
	}
	
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public static void showProgress(final View mainView, final View progressView, final boolean showProgress, Context context) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

			progressView.setVisibility(View.VISIBLE);
			progressView.animate().setDuration(shortAnimTime).alpha(showProgress ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					progressView.setVisibility(showProgress ? View.VISIBLE : View.GONE);
				}
			});

			mainView.setVisibility(View.VISIBLE);
			mainView.animate().setDuration(shortAnimTime).alpha(showProgress ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mainView.setVisibility(showProgress ? View.GONE : View.VISIBLE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			progressView.setVisibility(showProgress ? View.VISIBLE : View.GONE);
			mainView.setVisibility(showProgress ? View.GONE : View.VISIBLE);
		}
	}
	
	public static byte[] readFileToByteArray(String keyPath, Context c) {
		if(keyPath == null)
			return new byte[]{};
		Uri key = Uri.fromFile(new File(keyPath));
		InputStream is = null;
		ByteArrayOutputStream bos = null;
		try {
			is = c.getContentResolver().openInputStream(key);
			bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int bytesRead;
			while ((bytesRead = is.read(b)) != -1) {
			   bos.write(b, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		byte[] bytes = bos.toByteArray();
		
		return bytes;
	}
	
	public static String byteArrayToHexString(byte[] bytes) {
		if(bytes == null)
			return "";
		
	    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static void generateRandomNewKeys() {
		byte[] keyA = new byte[6];
		byte[] keyB = new byte[6];
		
		new Random().nextBytes(keyA);
		new Random().nextBytes(keyB);
		
		File extStore = Environment.getExternalStorageDirectory().getAbsoluteFile();
		File folder = new File(extStore, "keys");
		folder.mkdir();
		
		String oldKeyAFileName = "OldKeyR.key";
		String oldKeyBFileName = "OldKeyRW.key";
		String newKeyAFileName = "NewKeyR.key";
		String newKeyBFileNAme = "NewKeyRW.key";
		
		File oldKeyA = new File(folder, oldKeyAFileName);
		File oldKeyB = new File(folder, oldKeyBFileName);
		File newKeyA = new File(folder, newKeyAFileName);
		File newKeyB = new File(folder, newKeyBFileNAme);
		
		if(newKeyA.exists()) {
			newKeyA.renameTo(oldKeyA);
			newKeyA = new File(folder, newKeyAFileName);
		}
		if(newKeyB.exists()) {
			newKeyB.renameTo(oldKeyB);
			newKeyB = new File(folder, newKeyBFileNAme);
		}
		
		try {
			newKeyA.createNewFile();
			newKeyB.createNewFile();
			
			OutputStream newKeyAOS = new FileOutputStream(newKeyA);
			OutputStream newKeyBOS = new FileOutputStream(newKeyB);
			newKeyAOS.write(keyA);
			newKeyBOS.write(keyB);
			newKeyAOS.close();
			newKeyBOS.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void addDataToSectioniOfHealthRecordFile(String pathToHRFile, SectionDataBean sdb, int i) throws Exception {
		String fileContents = getStringFromFile(pathToHRFile);
		
		JSONObject data = new JSONObject();
		data.put(Constants.doctorNameKey, sdb.getDoctor());
		data.put(Constants.problemKey, sdb.getProblem());
		data.put(Constants.testKey, sdb.getTest());
		data.put(Constants.medicineKey, sdb.getMedicine());
		
		JSONObject main = (JSONObject) new JSONTokener(fileContents).nextValue();
		JSONArray section = null;
		if(i == 1)
			section = main.getJSONArray(Constants.section1Key);
		else if(i == 2)
			section = main.getJSONArray(Constants.section2Key);
		
		section.put(data);
		
		writeJSONObjectToFile(main, pathToHRFile);
	}
	
	public static void writeJSONObjectToFile(JSONObject object, String path) throws Exception {
		FileWriter file = new FileWriter(path);
		file.write(object.toString());
		file.flush();
		file.close();
	}
	
	public static String getStringFromFile (String filePath) throws Exception {
	    File fl = new File(filePath);
	    FileInputStream fin = new FileInputStream(fl);
	    String ret = convertStreamToString(fin);
	    fin.close();        
	    return ret;
	}
	
	public static String convertStreamToString(InputStream is) throws Exception {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append("\n");
	    }
	    return sb.toString();
	}

	public static byte[] objectToByteArray(Object obj) throws Exception{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(obj);
		byte res[] = bos.toByteArray();		
		out.close();
		bos.close();
		return res;
	}
	
	public static Object byteArrayToObject(byte[] data) throws Exception{
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = new ObjectInputStream(bis);
		Object res = in.readObject();
		bis.close();
		in.close();
		return res;
	}
	
	public static HealthRecordBean readHealthRecordFromFile(String path) throws Exception {
		String result = getStringFromFile(path);
		JSONObject main= (JSONObject) new JSONTokener(result).nextValue();
		String patientName = main.getString(Constants.patientNameKey);
		String patientDOB = main.getString(Constants.patientDOBKey);
		JSONArray section1 = main.getJSONArray(Constants.section1Key);
		JSONArray section2 = main.getJSONArray(Constants.section2Key);
		
		HealthRecordBean hrb = new HealthRecordBean(patientName, patientDOB);
		
		for(int i=0; i<section1.length(); i++) {
			JSONObject data = section1.getJSONObject(i);
			String doctor = data.getString(Constants.doctorNameKey);
			String problem = data.getString(Constants.problemKey);
			String test = data.getString(Constants.testKey);
			String medicine = data.getString(Constants.medicineKey);
			hrb.addDataInSection1(new SectionDataBean(doctor, problem, test, medicine));
		}
		for(int i=0; i<section2.length(); i++) {
			JSONObject data = section2.getJSONObject(i);
			String doctor = data.getString(Constants.doctorNameKey);
			String problem = data.getString(Constants.problemKey);
			String test = data.getString(Constants.testKey);
			String medicine = data.getString(Constants.medicineKey);
			hrb.addDataInSection2(new SectionDataBean(doctor, problem, test, medicine));
		}
		
		return hrb;
	}
	
	public static MessageBean getMessageBean(String path) throws Exception {
		String senderID = "PATIENT 001"; // TODO: MAC address?
		HealthRecordBean hrb = readHealthRecordFromFile(path);
		String key = "PATIENT001_SIGNATURE"; // TODO: Key from server
		MessageBean msg = new MessageBean(senderID, hrb, key);
		return msg;
	}
	
	public static EncryptedMessageBean getEncryptedMessageBean(String path, String publicKey) throws Exception {
		String senderID = "PATIENT 001"; // TODO: MAC address?
		HealthRecordBean hrb = readHealthRecordFromFile(path);
		RSA crypt = new RSA(publicKey);
		byte[] encryptedKey = crypt.encrypt(stringToByteArray(Constants.STREAM_CIPHER_KEYWORD));
		
		// Debug
		System.out.println("Encrypted key: " + byteArrayToString(encryptedKey));
		
		RC4 enc = new RC4(stringToByteArray(Constants.STREAM_CIPHER_KEYWORD));
		byte[] ehrb = enc.encrypt(objectToByteArray(hrb));
		
		EncryptedMessageBean msg = new EncryptedMessageBean(senderID, ehrb, encryptedKey);				
		return msg;
	}
	
	public static MessageBean getDencryptedMessageBean(EncryptedMessageBean emb, String privateKey) throws Exception {
		String senderID = emb.getSenderID();
		byte[] ehrb = emb.getEncryptedHealthRecord();
		byte[] encryptedKey = emb.getEncryptedKey();
		RSA crypt = new RSA(privateKey);
		String key = Tools.byteArrayToString(crypt.decrypt(encryptedKey));
		
		// Debug:
		System.out.println("Decrypted key: " + key);
		
		if(!key.equals(Constants.STREAM_CIPHER_KEYWORD))
			return null;
		
		RC4 enc = new RC4(stringToByteArray(Constants.STREAM_CIPHER_KEYWORD));
		byte[] hrb = enc.decrypt(ehrb);

		MessageBean msg = new MessageBean(senderID, (HealthRecordBean) byteArrayToObject(hrb), key);				
		return msg;
	}
	
	public static String byteArrayToString(byte[] from) {
		String result = "";
		try {
			if(from != null)
				result = new String(from, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static byte[] stringToByteArray(String arg) {
		if(arg == null || arg.length() == 0)
			return new byte[0];
	    String hex = String.format("%040x", new BigInteger(arg.getBytes()));
	    BigInteger bi = new BigInteger(hex, 16);
     	byte[] bytes = bi.toByteArray();
     	return bytes;
	}
}
