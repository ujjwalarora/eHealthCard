package com.healthcare.ehealthcard;

import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
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
import android.widget.TextView;
import android.widget.Toast;

import com.healthcare.taghelper.Constants;
import com.healthcare.taghelper.NetworkTools;
import com.healthcare.taghelper.TagHelper;
import com.healthcare.taghelper.Tools;

public class PatientActivity extends FragmentActivity implements ActionBar.TabListener {
	private final String TAG = "com.healthcare.ehealthcard.PatientActivity";
	
	private NfcAdapter mNfcAdapter;  
    private IntentFilter[] mNdefExchangeFilters;  
    private PendingIntent mNfcPendingIntent;
    private Context m;
    
    private PatientTask mPatientTask = null;
    private Tag tag = null;
    
    private static TextView pageView1, pageView2, pageView3;
    private View formView, statusView;
    private String mEmail = "";
    private String section1 = "", section2 = "", section3 = "";

	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_patient);
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
			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
		}
	}
	
	private void findViews() {
		formView = findViewById(R.id.patient_pager);
		statusView = findViewById(R.id.patient_status);
		mViewPager = (ViewPager) formView;
	}
	
	private void init() {
		resetData();
		m = this;
		mNfcAdapter = NfcAdapter.getDefaultAdapter(m);  
        mNfcPendingIntent = PendingIntent.getActivity(m, 0, new Intent(m, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
        IntentFilter tagDiscovered = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);  
        mNdefExchangeFilters = new IntentFilter[] { tagDiscovered };
        
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        // Get data via the key
        String value1 = extras.getString("email");
        if (value1 != null) {
          // Do something with the data
        	mEmail = value1;
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
		if (mPatientTask != null) {
			return;
		}

		resetData();

		Tools.showProgress(formView, statusView, true, m);
		mPatientTask = new PatientTask();
		mPatientTask.execute((Void) null);
	}
	
	private void setViews() {
		if(pageView1 != null) pageView1.setText(section1);
		if(pageView2 != null) pageView2.setText(section2);
		if(pageView3 != null) pageView3.setText(section3);
	}
	
	private void resetData() {
		section1 = section2 = section3 = getResources().getString(R.string.label_tag_instructions);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.patient, menu);
		return true;
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int position) {
			Fragment frag = null;
			switch(position) {
			case 0:
				frag = new Section1Fragment();
				break;
			case 1:
				frag = new Section2Fragment();
				break;
			case 2:
				
				frag = new Section3Fragment();
				break;
			}
			return frag;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	public static class Section1Fragment extends Fragment {
		public Section1Fragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View root = inflater.inflate(R.layout.fragment_patient_details_page1, container, false);
			pageView1 = (TextView) root.findViewById(R.id.section1_label);
			return root;
		}
	}
	
	public static class Section2Fragment extends Fragment {
		public Section2Fragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View root = inflater.inflate(R.layout.fragment_patient_details_page2, container, false);
			pageView2 = (TextView) root.findViewById(R.id.section2_label);
			return root;
		}
	}
	
	public static class Section3Fragment extends Fragment {
		public Section3Fragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View root = inflater.inflate(R.layout.fragment_patient_details_page3, container, false);
			pageView3 = (TextView) root.findViewById(R.id.section3_label);
			return root;
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
	
	public class PatientTask extends AsyncTask<Void, String, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;
			BasicNameValuePair creds[] = { new BasicNameValuePair(Constants.KEY_EMAIL, mEmail) };
			HttpResponse response = NetworkTools.postData(Constants.EHEALTHCARE_URL_GET_READ_KEYS, creds);
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
					
					// Change keys on tag
					byte[] S1RKey = Tools.stringToByteArray(s1r);
					byte[] S2RKey = Tools.stringToByteArray(s2r);
					byte[] S3RKey = Tools.stringToByteArray(s3r);
					
					StringBuilder msg = new StringBuilder();
					
					section1 = Tools.byteArrayToString(helper.readSection(0, S1RKey));
					msg.append(helper.getStatus()+"\n");
					section2 = Tools.byteArrayToString(helper.readSection(1, S2RKey));
					msg.append(helper.getStatus()+"\n");
					section3 = Tools.byteArrayToString(helper.readSection(2, S3RKey));
					msg.append(helper.getStatus());
					
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
			mPatientTask = null;
			setViews();
			Tools.showProgress(formView, statusView, false, m);
			if (success) {
				publishProgress("Tag read complete");
			}
		}

		@Override
		protected void onCancelled() {
			mPatientTask = null;
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

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition(), true);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

}
