package com.healthcare.ehealthcard;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.healthcare.taghelper.Constants;
import com.healthcare.taghelper.NetworkTools;
import com.healthcare.taghelper.Tools;

public class LoginActivity extends Activity {
	private UserLoginTask mAuthTask = null;

	private String mEmail;
	private String mPassword;

	private Context c;
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		c = this;
		
		mEmail = getIntent().getStringExtra(Constants.EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {						
					attemptLogin();
				}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	private void resetFields() {
		mEmailView.setText("");
		mPasswordView.setText("");
	}

	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 3) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			Tools.showProgress(mLoginFormView, mLoginStatusView, true, c);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			
			boolean result = false;
			BasicNameValuePair creds[] = { new BasicNameValuePair(Constants.KEY_EMAIL, mEmail),
										new BasicNameValuePair(Constants.KEY_PASSWORD, mPassword)};
			NetworkTools.initNetworking();
			HttpResponse response = NetworkTools.postData(Constants.EHEALTHCARE_URL_LOGIN, creds);
			JSONObject jObj = NetworkTools.getJSONObjectFromHttpResponse(response);
			
			if(NetworkTools.isResponseValid(response, jObj)) {
				result = true;
				
				if(NetworkTools.getValueOfKey(jObj, "found").equals("0"))
					result = false;
				
				if(result) {
					String role = NetworkTools.getValueOfKey(jObj, "role");
					
					if(role.equals("p")) {
						startActivity(new Intent(LoginActivity.this, PatientActivity.class).putExtra("email", mEmail).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
					}
					else if(role.equals("d")) {
						startActivity(new Intent(LoginActivity.this, DoctorActivity.class).putExtra("email", mEmail).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
					}
					else if(role.equals("ti")) {
						startActivity(new Intent(LoginActivity.this, TagIssuerActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
					}
				}
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			Tools.showProgress(mLoginFormView, mLoginStatusView, false, c);

			if (success) {
				resetFields();
			} else {
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			Tools.showProgress(mLoginFormView, mLoginStatusView, false, c);
		}
	}
}
