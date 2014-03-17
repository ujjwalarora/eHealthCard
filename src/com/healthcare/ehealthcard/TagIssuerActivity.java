package com.healthcare.ehealthcard;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.healthcare.taghelper.Constants;
import com.healthcare.taghelper.NetworkTools;
import com.healthcare.taghelper.TagHelper;
import com.healthcare.taghelper.Tools;

public class TagIssuerActivity extends Activity {
	private final String TAG = "com.healthcare.ehealthcard.TagIssuerActivity";
	
	private NfcAdapter mNfcAdapter;  
    private IntentFilter[] mNdefExchangeFilters;  
    private PendingIntent mNfcPendingIntent;
    private Context m;
    
    private NewTagIssuerTask mNewTagIssuerTask = null;
    private Tag tag = null;
    
    private View formView, statusView;
	private EditText name, email, password, dob, tagID;
	private String mName, mEmail, mPassword, mDob, mTagID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_issuer);
		findViews();
		init();
	}
	
	private void init() {
		m = this;
		mNfcAdapter = NfcAdapter.getDefaultAdapter(m);  
        mNfcPendingIntent = PendingIntent.getActivity(m, 0, new Intent(m, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
        IntentFilter tagDiscovered = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);  
        mNdefExchangeFilters = new IntentFilter[] { tagDiscovered };
	}
	
	private void findViews() {
		formView = findViewById(R.id.tag_issuer_form);
		statusView = findViewById(R.id.tag_issuer_status);
		name = (EditText) findViewById(R.id.tag_issuer_name);
		email = (EditText) findViewById(R.id.tag_issuer_email);
		password = (EditText) findViewById(R.id.tag_issuer_pass);
		dob = (EditText) findViewById(R.id.tag_issuer_dob);
		tagID = (EditText) findViewById(R.id.tag_issuer_tagID);
	}
	
	private void retrieveData() {
		mName = name.getText().toString();
		mEmail = email.getText().toString();
		mPassword = password.getText().toString();
		mDob = dob.getText().toString();
		mTagID = Tools.byteArrayToHexString(tag.getId());
		tagID.setText(mTagID);
	}
	
	private boolean validateData() {
		boolean result = true;
		if(mName.length()==0 || mEmail.length()==0 || mPassword.length()==0 || mDob.length()==0) {
			result = false;
		}
		return result;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		// Hide keyboard
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(email.getWindowToken(), 0);
			
		tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		
		if(tag != null)
			startTask();
		else
			Toast.makeText(m, "Tag wasn't recognized", Toast.LENGTH_LONG).show();
	}
	
	private void resetFields() {
		resetErrors();
		name.setText("");
		email.setText("");
		password.setText("");
		dob.setText("");
		tagID.setText("");
	}
	
	private void resetErrors() {
		name.setError(null);
		email.setError(null);
		password.setError(null);
		dob.setError(null);
		tagID.setError(null);
	}
	
	public void startTask() {
		if (mNewTagIssuerTask != null) {
			return;
		}

		resetErrors();
		retrieveData();

		if(!validateData()) {
			Toast.makeText(m, "Please enter correct data", Toast.LENGTH_LONG).show();
		} else {
			// Show a progress spinner, and kick off a background task
			Tools.showProgress(formView, statusView, true, m);
			mNewTagIssuerTask = new NewTagIssuerTask();
			mNewTagIssuerTask.execute((Void) null);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mNfcAdapter != null) 
			mNfcAdapter.disableForegroundDispatch(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
            if (!mNfcAdapter.isEnabled()){  
            	Tools.showNFCSettingsDialogBox(m);
            }  
		}
		else {  
           	Toast.makeText(m, "Sorry, No NFC Adapter found.", Toast.LENGTH_LONG).show();  
       }  
	}
	
	public class NewTagIssuerTask extends AsyncTask<Void, String, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;
			BasicNameValuePair creds[] = { new BasicNameValuePair(Constants.KEY_NAME, mName),
										new BasicNameValuePair(Constants.KEY_EMAIL, mEmail),
										new BasicNameValuePair(Constants.KEY_PASSWORD, mPassword),
										new BasicNameValuePair(Constants.KEY_DOB, mDob),
										new BasicNameValuePair(Constants.KEY_TAG_ID, mTagID)};
			HttpResponse response = NetworkTools.postData(Constants.EHEALTHCARE_URL_NEW_TAG_ISSUER, creds);
			JSONObject jObj = NetworkTools.getJSONObjectFromHttpResponse(response);
			
			if(NetworkTools.isResponseValid(response, jObj)) {
				result = true;
				String error = NetworkTools.getValueOfKey(jObj, Constants.KEY_ERROR);
				
				if(!error.equals("0"))
					result = false;
				
				if(result) {
					TagHelper helper = new TagHelper(tag);
					
					// Get new keys
					String s1r = NetworkTools.getValueOfKey(jObj, Constants.KEY_S1R);
					String s2r = NetworkTools.getValueOfKey(jObj, Constants.KEY_S2R);
					String s3r = NetworkTools.getValueOfKey(jObj, Constants.KEY_S3R);
					String s1rw = NetworkTools.getValueOfKey(jObj, Constants.KEY_S1RW);
					String s2rw = NetworkTools.getValueOfKey(jObj, Constants.KEY_S2RW);
					String s3rw = NetworkTools.getValueOfKey(jObj, Constants.KEY_S3RW);
					
					byte[] oldKey = MifareClassic.KEY_DEFAULT;
					
					StringBuilder msg = new StringBuilder();
					
					// Change keys on tag
					byte[] RKey = Tools.stringToByteArray(s1r);
					byte[] RWKey = Tools.stringToByteArray(s1rw);
					helper.changeKeysOfSection(0, oldKey, RKey, RWKey);
					msg.append(helper.getStatus()+"\n");
					
					RKey = Tools.stringToByteArray(s2r);
					RWKey = Tools.stringToByteArray(s2rw);
					helper.changeKeysOfSection(1, oldKey, RKey, RWKey);
					msg.append(helper.getStatus()+"\n");
					
					RKey = Tools.stringToByteArray(s3r);
					RWKey = Tools.stringToByteArray(s3rw);
					helper.changeKeysOfSection(2, oldKey, RKey, RWKey);
					msg.append(helper.getStatus()+"\n");
					
					// Write patient info
					String data = "Name: " + mName + "\nDoB: " + mDob;
					byte[] key = Tools.stringToByteArray(s1rw);
					helper.writeSection(0, key, Tools.stringToByteArray(data));
					msg.append(helper.getStatus()+"\n");
					
					helper.close();
					
					// Show status message
					publishProgress(msg.toString());
				}
				else {
					publishProgress(error);
				}
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mNewTagIssuerTask = null;
			Tools.showProgress(formView, statusView, false, m);
			if (success) {
				resetFields();
				publishProgress("Tag issue complete");
			}
		}

		@Override
		protected void onCancelled() {
			mNewTagIssuerTask = null;
			Tools.showProgress(formView, statusView, false, m);
			resetFields();
			publishProgress("Some error occurred");
		}

		@Override
		protected void onProgressUpdate(final String... values) {
			super.onProgressUpdate(values);
			Tools.debugLog(TAG, values[0]);
			Toast.makeText(m, values[0], Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tag_issuer, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		startActivity(new Intent(TagIssuerActivity.this, FormatTagActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
		return super.onOptionsItemSelected(item);
	}

}
