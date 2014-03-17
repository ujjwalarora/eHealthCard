package com.healthcare.ehealthcard;

import java.util.Locale;
import java.util.StringTokenizer;

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
import android.widget.TextView;
import android.widget.Toast;

import com.healthcare.taghelper.Constants;
import com.healthcare.taghelper.NetworkTools;
import com.healthcare.taghelper.TagHelper;
import com.healthcare.taghelper.Tools;

public class DoctorActivity extends FragmentActivity implements ActionBar.TabListener {
	
	private final String TAG = "com.healthcare.ehealthcard.DoctorActivity";
	
	private NfcAdapter mNfcAdapter;  
    private IntentFilter[] mNdefExchangeFilters;  
    private PendingIntent mNfcPendingIntent;
    private Context mContext;
    
    private static TextView patientInfoTagView;
	private static TextView patientInfoCloudView;
    private static EditText patientNameView;
    private static EditText patientIDView;
    private static EditText doctorNameView;
    private static EditText problemView;
    private static EditText testView;
    private static EditText medicineView;
    
    private DoctorTaskInfo mDoctorTaskInfo = null;
    private DoctorTaskPrescription mDoctorTaskPrescription = null;
    private Tag mTag = null;
    
    private boolean isPatientTagRead = false;
    private View formView, statusView;
    private String mEmail = "", mtagID = "", mPatientName = "", mPatientID = "", mProblem = "", mTest = "", mMedicine = "", mDoctorName = "";
    private String mPatientInfo = "", mPatientData = "";

	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doctor);
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
	
	private void setData() {
		mtagID = mPatientName = mPatientID = mProblem = mTest = mMedicine = mDoctorName = "";
		mPatientInfo = mPatientData = getResources().getString(R.string.label_tag_instructions);
	}
	
	private void resetData() {
		mProblem = mTest = mMedicine = mDoctorName = "";
	}
	
	private void findViews() {
		formView = findViewById(R.id.doctor_pager);
		statusView = findViewById(R.id.doctor_status);
		mViewPager = (ViewPager) formView;
	}
	
	private void setViews() {
		if(patientInfoTagView != null) {
			patientInfoTagView.setText(mPatientInfo);
		}
		if(patientInfoCloudView != null) {
			patientInfoCloudView.setText(mPatientData);
		}
		if(patientNameView != null) {
			patientNameView.setText(mPatientName);
		}
		if(patientIDView != null) {
			patientIDView.setText(mPatientID);
		}
		if(doctorNameView != null) {
			doctorNameView.setText(mDoctorName);
		}
		if(problemView != null) {
			problemView.setText(mProblem);
		}
		if(testView != null) {
			testView.setText(mTest);
		}
		if(medicineView != null) {
			medicineView.setText(mMedicine);
		}
	}
	
	private void getDataFromViews() {
		if(doctorNameView != null) {
			mDoctorName = doctorNameView.getText().toString();
		}
		if(problemView != null) {
			mProblem = problemView.getText().toString();
		}
		if(testView != null) {
			mTest = testView.getText().toString();
		}
		if(medicineView != null) {
			mMedicine = medicineView.getText().toString();
		}
	}
	
	private void init() {
		setData();
		mContext = this;
		mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);  
        mNfcPendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
        IntentFilter tagDiscovered = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);  
        mNdefExchangeFilters = new IntentFilter[] { tagDiscovered };
        
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        String value1 = extras.getString("email");
        if (value1 != null) {
        	mEmail = value1;
        } 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.doctor, menu);
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
				fragment = new DetailsFragment();
				break;
			case 1:
				fragment = new PrescriptionFragment();
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
				return getString(R.string.doctor_title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.doctor_title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	public static class DetailsFragment extends Fragment {
		public DetailsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_doctor_details, container, false);
			patientInfoTagView = (TextView) rootView.findViewById(R.id.doctor_details_patient_info);
			patientInfoCloudView = (TextView) rootView.findViewById(R.id.doctor_details_patient_data);
			return rootView;
		}
	}
	
	public static class PrescriptionFragment extends Fragment {
		public PrescriptionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_doctor_prescription, container, false);
			patientNameView = (EditText) rootView.findViewById(R.id.prescription_patient_name);
			patientIDView = (EditText) rootView.findViewById(R.id.prescription_patient_id);
			doctorNameView = (EditText) rootView.findViewById(R.id.prescription_doctor_name);
			problemView = (EditText) rootView.findViewById(R.id.prescription_problem);
			testView = (EditText) rootView.findViewById(R.id.prescription_test);
			medicineView = (EditText) rootView.findViewById(R.id.prescription_medicine);
			return rootView;
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
			
		mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		
		if(mTag != null)
			startTask();
		else
			Toast.makeText(mContext, "Tag wasn't recognized", Toast.LENGTH_LONG).show();
	}
	
	public void startTask() {
		mPatientID = mtagID = Tools.byteArrayToHexString(mTag.getId());
		
		switch(mViewPager.getCurrentItem()) {
		case 0:
			if (mDoctorTaskInfo != null) {
				return;
			}
			Tools.showProgress(formView, statusView, true, mContext);
			mDoctorTaskInfo = new DoctorTaskInfo();
			mDoctorTaskInfo.execute((Void) null);
			break;
		case 1:
			if (mDoctorTaskPrescription != null) {
				return;
			}
			if(!isPatientTagRead) {
				Toast.makeText(mContext, "Please read the mTag first", Toast.LENGTH_LONG).show();
				return;
			}
			getDataFromViews();
			Tools.showProgress(formView, statusView, true, mContext);
			mDoctorTaskPrescription = new DoctorTaskPrescription();
			mDoctorTaskPrescription.execute((Void) null);
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
            	Tools.showNFCSettingsDialogBox(mContext);
            }  
		}
		else {  
           	Toast.makeText(mContext, "Sorry, No NFC Adapter found.", Toast.LENGTH_LONG).show();  
       }  
	}
	
	public class DoctorTaskInfo extends AsyncTask<Void, String, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;
			BasicNameValuePair creds[] = { new BasicNameValuePair(Constants.KEY_EMAIL, mEmail), new BasicNameValuePair(Constants.KEY_TAG_ID, mtagID) };
			HttpResponse response = NetworkTools.postData(Constants.EHEALTHCARE_URL_DOCTOR, creds);
			JSONObject jObj = NetworkTools.getJSONObjectFromHttpResponse(response);
			
			if(NetworkTools.isResponseValid(response, jObj)) {
				result = true;
				String error = NetworkTools.getValueOfKey(jObj, Constants.KEY_ERROR);
				
				if(!error.equals("0"))
					result = false;
				
				if(result) {
					TagHelper helper = new TagHelper(mTag);
					
					String rKey = NetworkTools.getValueOfKey(jObj, Constants.KEY_READ_KEY);
					mPatientData = NetworkTools.getValueOfKey(jObj, Constants.KEY_DATA);
					
					mPatientInfo = Tools.byteArrayToString(helper.readSection(0, Tools.stringToByteArray(rKey)));
					
					publishProgress(helper.getStatus());
					
					// Weird hacks
					
					if(mPatientInfo != null && mPatientInfo.length() > 0) {
						Tools.debugLog(TAG, mPatientInfo);
						StringTokenizer strtok = new StringTokenizer(mPatientInfo,"\n ");
						strtok.nextToken();
						mPatientName = strtok.nextToken();
					}
					
					if(mPatientData != null && mPatientData.length() > 0) {
						mPatientData = mPatientData.replace("%", "\n");
					}
					
					// TODO: Change keys here
					
					
					
					helper.close();
					isPatientTagRead = true;
				}
				else {
					publishProgress(error);
				}
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mDoctorTaskInfo = null;
			Tools.showProgress(formView, statusView, false, mContext);
			if (success) {
				resetData();
				setViews();
				publishProgress("Complete");
			}
		}

		@Override
		protected void onCancelled() {
			mDoctorTaskInfo = null;
			Tools.showProgress(formView, statusView, false, mContext);
			publishProgress("Some error occurred");
		}

		@Override
		protected void onProgressUpdate(final String... values) {
			super.onProgressUpdate(values);
			Tools.debugLog(TAG, values[0]);
			Toast.makeText(mContext, values[0], Toast.LENGTH_LONG).show();
		}
	}
	
	public class DoctorTaskPrescription extends AsyncTask<Void, String, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;
			BasicNameValuePair creds[] = { new BasicNameValuePair(Constants.KEY_PATIENT_NAME, mPatientName),
											new BasicNameValuePair(Constants.KEY_PATIENT_ID, mPatientID),
											new BasicNameValuePair(Constants.KEY_PROBLEM, mProblem),
											new BasicNameValuePair(Constants.KEY_DOCTOR_NAME, mDoctorName),
											new BasicNameValuePair(Constants.KEY_TEST, mTest),
											new BasicNameValuePair(Constants.KEY_MEDICINE, mMedicine)};
			HttpResponse response = NetworkTools.postData(Constants.EHEALTHCARE_URL_UPDATE_PATIENT_DATA, creds);
			JSONObject jObj = NetworkTools.getJSONObjectFromHttpResponse(response);
			
			if(NetworkTools.isResponseValid(response, jObj)) {
				result = true;
				String error = NetworkTools.getValueOfKey(jObj, Constants.KEY_ERROR);
				
				if(!error.equals("0"))
					result = false;
				
				if(result) {
					// TODO: Write this data to mTag. But which section?
				}
				else {
					publishProgress(error);
				}
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mDoctorTaskPrescription = null;
			Tools.showProgress(formView, statusView, false, mContext);
			if (success) {
				resetData();
				setViews();
				publishProgress("Complete");
			}
		}

		@Override
		protected void onCancelled() {
			mDoctorTaskPrescription = null;
			Tools.showProgress(formView, statusView, false, mContext);
			publishProgress("Some error occurred");
		}

		@Override
		protected void onProgressUpdate(final String... values) {
			super.onProgressUpdate(values);
			Toast.makeText(mContext, values[0], Toast.LENGTH_LONG).show();
		}
	}

}
