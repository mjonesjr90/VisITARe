package com.thesis.visitare;

/**
 * @author malcomjonesjr
 * The HomeActivity class defines the starting point for the VisITARe
 * application.
 */

import java.io.IOException;

import com.metaio.tools.io.AssetsManager;
import com.thesis.visitare.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;

public class HomeActivity extends Activity {

	String TAG = "Thesis";
	WebView mWebView;
	View mProgress;
	boolean mLaunchAR;
	AssetsExtracter mTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		Log.v(TAG, "Setting the HomeActivity view");
		setContentView(R.layout.activity_home);
		Log.v(TAG, "Finished setting the HomeActivity view");
		mTask = new AssetsExtracter();
		mTask.execute(0);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
	}
	
	@Override
	protected void onPause() 
	{
		super.onPause();
	}

	public void openARView(View view){	
		try 
		{
			Log.v(TAG, "Try to open the ARView class");
			final Class<?> activity = Class.forName(getPackageName()+".VisitareView");
			Log.v(TAG, "ARView started");
			startActivity(new Intent(getApplicationContext(), activity));
		} 
		catch (ClassNotFoundException e) 
		{
			Log.e(TAG, "ARView failed");
		}
	}
	
	public void openAboutView(View view){
		Log.v(TAG, "Try to open AboutView");
		Intent intent = new Intent(this, AboutView.class);
		Log.v(TAG, "AboutView intent created");
		startActivity(intent);
		Log.v(TAG, "AboutView started");
	}

	/**
	 * This task extracts all the assets to an external or internal location
	 * to make them accessible to metaio SDK
	 */
	private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean>
	{

		@Override
		protected void onPreExecute() 
		{
			Log.v(TAG, "Entering AssetsExtracter onPreExecute");
		}
		
		@Override
		protected Boolean doInBackground(Integer... params) 
		{
			Log.v(TAG, "Entering AssetsExtracter doInBackground");
			try 
			{
				Log.v(TAG,"Extracting Assets in AssetsExtracter");
				AssetsManager.extractAllAssets(getApplicationContext(), true);
			} 
			catch (IOException e) 
			{
				Log.v(TAG, "Error in doInBackground");
				return false;
			}

			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) 
		{
			Log.v(TAG, "Entering AssetsExtracter onPostExecute");
	    }
	}
}

