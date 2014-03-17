package com.healthcare.ehealthcard;

import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.healthcare.taghelper.Constants;
import com.healthcare.taghelper.NetworkTools;
import com.healthcare.taghelper.TagHelper;
import com.healthcare.taghelper.Tools;

public class FormatTagActivity extends FragmentActivity implements ActionBar.TabListener {
	private final String TAG = "com.healthcare.ehealthcard.FormatTagActivity";
	
	private static String s1Key, s2Key, s3Key;
	private String mtagID = "";
	private static EditText s1KeyView, s2KeyView, s3KeyView; 
	
	private FormatTagAuto mFormatTagAuto = null;
	private FormatTagManual mFormatTagManual = null;
	
	private View formView, statusView;
	private NfcAdapter mNfcAdapter;  
    private IntentFilter[] mNdefExchangeFilters;  
    private PendingIntent mNfcPendingIntent;
    private Context m;
    
    private Tag tag = null;

	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_format_tag);
		findViews();
		init();

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}
	
	private void findViews() {
		formView = findViewById(R.id.format_tag_pager);
		statusView = findViewById(R.id.format_tag_status);
		mViewPager = (ViewPager) formView;
	}
	
	private void init() {
//		resetData();
		m = this;
		mNfcAdapter = NfcAdapter.getDefaultAdapter(m);  
        mNfcPendingIntent = PendingIntent.getActivity(m, 0, new Intent(m, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
        IntentFilter tagDiscovered = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);  
        mNdefExchangeFilters = new IntentFilter[] { tagDiscovered };
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.format_tag, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		mViewPager.setCurrentItem(tab.getPosition(), true);
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			
			Fragment fragment = null;
			
			switch(position) {
			case 0:
				fragment = new AutoFormatFragment();
				break;
			case 1:
				fragment = new ManualFormatFragment();
				break;
			}
			
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.format_tag_title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.format_tag_title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	public static class AutoFormatFragment extends Fragment {

		public AutoFormatFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_format_tag_automatic, container, false);
			return rootView;
		}
	}
	
	public static class ManualFormatFragment extends Fragment {

		public ManualFormatFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_format_tag_manual, container, false);
			s1KeyView = (EditText) rootView.findViewById(R.id.format_tag_section1key);
			s2KeyView = (EditText) rootView.findViewById(R.id.format_tag_section2key);
			s3KeyView = (EditText) rootView.findViewById(R.id.format_tag_section3key);
			return rootView;
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
			
		tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		
		if(tag != null)
			startTask();
		else
			Toast.makeText(m, "Tag wasn't recognized", Toast.LENGTH_LONG).show();
	}
	
	public void startTask() {
		switch(mViewPager.getCurrentItem()) {
		case 0:
			// Auto
			mtagID = Tools.byteArrayToHexString(tag.getId());
			Tools.debugLog(TAG, mtagID);
			if (mFormatTagAuto != null) {
				return;
			}
//			resetData();
			Tools.showProgress(formView, statusView, true, m);
			mFormatTagAuto = new FormatTagAuto();
			mFormatTagAuto.execute((Void) null);
			break;
		case 1:
			// Manual
			s1Key = s1KeyView.getText().toString();
			s2Key = s2KeyView.getText().toString();
			s3Key = s3KeyView.getText().toString();
			
			if (mFormatTagManual != null) {
				return;
			}
//			resetData();
			Tools.showProgress(formView, statusView, true, m);
			mFormatTagManual = new FormatTagManual();
			mFormatTagManual.execute((Void) null);
			break;
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
	
	public class FormatTagAuto extends AsyncTask<Void, String, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;
			BasicNameValuePair creds[] = { new BasicNameValuePair(Constants.KEY_TAG_ID, mtagID) };
			HttpResponse response = NetworkTools.postData(Constants.EHEALTHCARE_URL_GET_WRITE_KEYS, creds);
			JSONObject jObj = NetworkTools.getJSONObjectFromHttpResponse(response);
			
			if(NetworkTools.isResponseValid(response, jObj)) {
				result = true;
				String error = NetworkTools.getValueOfKey(jObj, Constants.KEY_ERROR);
				
				if(!error.equals("0"))
					result = false;
				
				if(result) {
					String s1RW = NetworkTools.getValueOfKey(jObj, Constants.KEY_S1RW);
					String s2RW = NetworkTools.getValueOfKey(jObj, Constants.KEY_S2RW);
					String s3RW = NetworkTools.getValueOfKey(jObj, Constants.KEY_S3RW);
					
					byte s1rw[] = Tools.stringToByteArray(s1RW);
					byte s2rw[] = Tools.stringToByteArray(s2RW);
					byte s3rw[] = Tools.stringToByteArray(s3RW);
					
					byte to[] = MifareClassic.KEY_DEFAULT;
					
					TagHelper helper = new TagHelper(tag);
					StringBuilder msg = new StringBuilder();
					helper.changeKeysOfSection(0, s1rw, to, to);
					msg.append(helper.getStatus()+"\n");
					helper.changeKeysOfSection(1, s2rw, to, to);
					msg.append(helper.getStatus()+"\n");
					helper.changeKeysOfSection(2, s3rw, to, to);
					msg.append(helper.getStatus()+"\n");
					helper.writeTag(to, Tools.stringToByteArray(""));
					msg.append(helper.getStatus());
					
					helper.close();
					
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
			mFormatTagAuto = null;
			Tools.showProgress(formView, statusView, false, m);
			if (success) {
				mSectionsPagerAdapter = null;
				mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
				mViewPager.setAdapter(mSectionsPagerAdapter);
				publishProgress("Formatting complete");
			}
		}

		@Override
		protected void onCancelled() {
			mFormatTagAuto = null;
			Tools.showProgress(formView, statusView, false, m);
			publishProgress("Some error occurred");
		}

		@Override
		protected void onProgressUpdate(final String... values) {
			super.onProgressUpdate(values);
			Tools.debugLog(TAG, values[0]);
			Toast.makeText(m, values[0], Toast.LENGTH_LONG).show();
		}
	}
	
	private boolean validateData() {
		if(s1Key.length() == 6 && s2Key.length() == 6 && s3Key.length() == 6)
			return true;
		return false;
	}
	
	public class FormatTagManual extends AsyncTask<Void, String, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = true;
			
			if(!validateData()) {
				publishProgress("Please enter the keys correctly");
				return false;
			}
			
			byte s1rw[] = Tools.stringToByteArray(s1Key);
			byte s2rw[] = Tools.stringToByteArray(s2Key);
			byte s3rw[] = Tools.stringToByteArray(s3Key);
			
			byte to[] = MifareClassic.KEY_DEFAULT;
			
			TagHelper helper = new TagHelper(tag);
			
			StringBuilder msg = new StringBuilder();
			helper.changeKeysOfSection(0, s1rw, to, to);
			msg.append(helper.getStatus()+"\n");
			helper.changeKeysOfSection(1, s2rw, to, to);
			msg.append(helper.getStatus()+"\n");
			helper.changeKeysOfSection(2, s3rw, to, to);
			msg.append(helper.getStatus()+"\n");
			helper.writeTag(to, Tools.stringToByteArray(""));
			msg.append(helper.getStatus());
			
			helper.close();
			
			publishProgress(msg.toString());
			
			return result;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mFormatTagManual = null;
			Tools.showProgress(formView, statusView, false, m);
			if (success) {
				mSectionsPagerAdapter = null;
				mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
				mViewPager.setAdapter(mSectionsPagerAdapter);
				mViewPager.setCurrentItem(1);
				publishProgress("Formatting complete");
			}
		}

		@Override
		protected void onCancelled() {
			mFormatTagManual = null;
			Tools.showProgress(formView, statusView, false, m);
			publishProgress("Some error occurred");
		}

		@Override
		protected void onProgressUpdate(final String... values) {
			super.onProgressUpdate(values);
			Tools.debugLog(TAG, values[0]);
			Toast.makeText(m, values[0], Toast.LENGTH_LONG).show();
		}
	}

}
