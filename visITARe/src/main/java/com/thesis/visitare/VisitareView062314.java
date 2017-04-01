package com.thesis.visitare;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.AnnotatedGeometriesGroupCallback;
import com.metaio.sdk.jni.IAnnotatedGeometriesGroup;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.IRadar;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.MetaioSDK;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.SensorValues;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.SystemInfo;
import com.metaio.tools.io.AssetsManager;
import com.thesis.visitare.NodeLocation;

public class VisitareView062314 extends ARViewActivity{
	String TAG = "Thesis";
	String siteURL = "http://malcomjonesjr.com/json/nodes.json";
	
	//Radar
	private IRadar mRadar;
	
	//Array for nodes
	private ArrayList<Node> nodeArray= new ArrayList<Node>();
	
	//Geometry Group
	private IAnnotatedGeometriesGroup mAnnotatedGeometriesGroup;
	private MyAnnotatedGeometriesGroupCallback mAnnotatedGeometriesGroupCallback;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.v(TAG,"Metaio key: "+ getResources().getString(R.string.metaioSDKSignature));
		MetaioSDK.CreateMetaioSDKAndroid(this, getResources().getString(R.string.metaioSDKSignature));
		
		//Set GPS tracking configuration on the user interface thread
		Log.v(TAG, "Set GPS config");
		boolean result = metaioSDK.setTrackingConfiguration("GPS", false);
		Log.v(TAG, "GPS config set: " + result);
	}

	protected void onDestroy(){
		// Break circular reference of Java objects
				if (mAnnotatedGeometriesGroup != null)
				{
					mAnnotatedGeometriesGroup.registerCallback(null);
				}
				
				if (mAnnotatedGeometriesGroupCallback != null)
				{
					mAnnotatedGeometriesGroupCallback.delete();
					mAnnotatedGeometriesGroupCallback = null;
				}

				super.onDestroy();
	}
	
	public void onButtonClick(View v){
		Log.v(TAG,"ARView closing");
		finish();
		Log.v(TAG,"ARView closed");
	}
	
	public void refresh(View view){
		Log.v(TAG, "Trying to Refresh");
		Toast.makeText(getApplicationContext(), "This is the refresh button!", Toast.LENGTH_LONG).show();
		siteURL = "http://malcomjonesjr.com/json/nodes2.json";
		Log.v(TAG, "Pull node data");
//		ReadJSON read = new ReadJSON();
//		read.execute(siteURL);
		loadEverything(siteURL);
	}
	
	protected int getGUILayout() {
		Log.v(TAG,"Set visitare view");
		return R.layout.activity_visitaire_view;
	}

	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
		Log.v(TAG,"Entering getMetaioSDKCallbackHandler");
		return null;
	}
	
	public void setArray(ArrayList<Node> newList){
		Log.v(TAG, "Entering setArray");
		nodeArray = newList;
		Log.v(TAG, "setArray Finished");
	}
	
	public class ReadJSON extends AsyncTask<String, Void, ArrayList<Node>>{
		
		@Override
		protected ArrayList<Node> doInBackground(String... params) {
			Log.v(TAG, "doInBackground gets called with "+params[0]);
			nodeArray = JSONHelper.parseAll(params[0]);
			Log.v(TAG, "doInBackground Finished");
			return nodeArray;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Node> result) {
//			super.onPostExecute(result);
			Log.v(TAG, "onPostExecute called");
			//loadEverything(result);
			setArray(result);
			Log.v(TAG, "onPostExecute Finished");
//			cancel(true);
//			CustomAdapter adapter = new CustomAdapter(context, R.id.relLayout, data, siteURL);
//			Log.d(TAG, "Adapter created");
//			setListAdapter(adapter);
//			Log.d(TAG, "Set Adapter");
		}
    }
	
	public void loadEverything(String site){
		
//		Log.v(TAG, "Pull node data");
		ReadJSON read = new ReadJSON();
		read.execute(site);
		try {
			read.get(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		nodeArray = JSONHelper.parseAll(site);
		Log.d(TAG, "Trying to read "+site);
		
		//Create locations for the cities/nodes
		//For VisITARe, these coordinates will be loaded from the nodes themselves
		Log.v(TAG, "Create locations for nodes");
//		mAnnotatedGeometriesGroup.delete();
		
		Log.v(TAG, "Iterate to make LLA's");
//		//Iterate through nodes to make LLAs
		Log.v(TAG, "Array size " + nodeArray.size() );
		for(int i = 0; i < nodeArray.size(); i++){
			mAnnotatedGeometriesGroup.addGeometry(
					createPOIGeometry(
							new LLACoordinate(
									nodeArray.get(i).getLocation().getLat(), 
									nodeArray.get(i).getLocation().getLon(), 
									nodeArray.get(i).getLocation().getAlt(), 0)), 
							String.valueOf(nodeArray.get(i).nodeID)+": "+String.valueOf(nodeArray.get(i).data));	//Sets the title for the node (NodeID and Temperature)
			mRadar.add(createPOIGeometry(
							new LLACoordinate(
									nodeArray.get(i).getLocation().getLat(), 
									nodeArray.get(i).getLocation().getLon(), 
									nodeArray.get(i).getLocation().getAlt(), 0)));
		}
		Log.v(TAG, "Finish loadEverything");
	}
	
	protected void loadContents() {		
		//This method will be called inside of loadContents to refresh the POIs
		Log.v(TAG,"Entering loadContents()");
		
		//Creates group that places geometries at their real locations
		Log.v(TAG, "Create Geometry Group");
		mAnnotatedGeometriesGroup = metaioSDK.createAnnotatedGeometriesGroup();
		//Create the interface for geometry events and callbacks
		mAnnotatedGeometriesGroupCallback = new MyAnnotatedGeometriesGroupCallback();
		//Attach the Geometry Group to the Callback
		mAnnotatedGeometriesGroup.registerCallback(mAnnotatedGeometriesGroupCallback);
		
		// Clamp geometries' Z position to range [5000;200000] no matter how close or far they are away.
		// This influences minimum and maximum scaling of the geometries (easier for development).
		metaioSDK.setLLAObjectRenderingLimits(5, 200);
		
		// Set render frustum accordingly
		//Distance is in millimeters
		metaioSDK.setRendererClippingPlaneLimits(10, 220000);
		
		// create radar
		mRadar = metaioSDK.createRadar();
		mRadar.setBackgroundTexture(AssetsManager.getAssetPath(getApplicationContext(), "VisitareView/radar.png"));
		mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath(getApplicationContext(), "VisitareView/yellow.png"));
		mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);
		
//		Log.v(TAG, "Pull node data");
//		ReadJSON read = new ReadJSON();
//		read.execute(siteURL);
//		//nodeArray = JSONHelper.parseAll(site);
//		Log.d(TAG, "Trying to read "+siteURL);
		
		
		//Iterate through nodes to make LLAs
//		Log.v(TAG, "Array size " + nodeArray.size() );
//		for(int i = 0; i < nodeArray.size(); i++){
//			mAnnotatedGeometriesGroup.addGeometry(
//					createPOIGeometry(
//							new LLACoordinate(
//									nodeArray.get(i).getLocation().getLat(), 
//									nodeArray.get(i).getLocation().getLon(), 
//									nodeArray.get(i).getLocation().getAlt(), 0)), 
//							String.valueOf(nodeArray.get(i).nodeID)+": "+String.valueOf(nodeArray.get(i).data));	//Sets the title for the node (NodeID and Temperature)
//			mRadar.add(createPOIGeometry(
//							new LLACoordinate(
//									nodeArray.get(i).getLocation().getLat(), 
//									nodeArray.get(i).getLocation().getLon(), 
//									nodeArray.get(i).getLocation().getAlt(), 0)));
//		}
		loadEverything(siteURL);
		
//		Log.v(TAG, "Wait 10 seconds to reload content");
//		long startTime = System.currentTimeMillis();
//		while(System.currentTimeMillis() - startTime <= 50000){
//			Log.v(TAG, "Waiting...");
//		}
//		loadContents();
	}

	private IGeometry createPOIGeometry(LLACoordinate lla){
		String path = AssetsManager.getAssetPath(getApplicationContext(), "VisitareView/ExamplePOI.obj");
		Log.v(TAG, "The path is " + path);
		if(path != null){
			IGeometry geo = metaioSDK.createGeometry(path);
			geo.setTranslationLLA(lla);
			geo.setLLALimitsEnabled(true);
			geo.setScale(100);
			return geo;
		}
		else
		{
			Log.v(TAG, "Missing files for POI geometry");
			return null;
		}
	}
	
	private String getAnnotationImageForTitle(String title){
		Bitmap billboard = null;
		Log.v(TAG, "The title is " + title);
		try
		{
			final String texturepath = getCacheDir() + "/" + title + ".png";
			Paint mPaint = new Paint();
			Log.v(TAG, "The texture path is " + texturepath);
			// Load background image and make a mutable copy
			
			float dpi = SystemInfo.getDisplayDensity(getApplicationContext());
			int scale = dpi > 240 ? 2 : 1;
			String filepath = AssetsManager.getAssetPath(getApplicationContext(), "VisitareView/POI_bg" + (scale == 2 ? "@2x" : "") + ".png");
			Log.v(TAG, "The filepath is " + filepath);
			Bitmap mBackgroundImage = BitmapFactory.decodeFile(filepath);

			billboard = mBackgroundImage.copy(Bitmap.Config.ARGB_8888, true);

			Canvas c = new Canvas(billboard);

			mPaint.setColor(Color.WHITE);
			mPaint.setTextSize(24);
			mPaint.setTypeface(Typeface.DEFAULT);
			mPaint.setTextAlign( Paint.Align.CENTER );

			float y = 40 * scale;
			float x = 30 * scale;

			// Draw POI name
			if (title.length() > 0)
			{
				String n = title.trim();

				final int maxWidth = 160 * scale;

				int i = mPaint.breakText(n, true, maxWidth, null);

				int xPos = (c.getWidth() / 2);
				int yPos = (int) ((c.getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2)) ; 
				c.drawText(n.substring(0, i), xPos, yPos, mPaint);
				
				// Draw second line if valid
				if (i < n.length())
				{
					 n = n.substring(i);
					 y += 20 * scale;
					 i = mPaint.breakText(n, true, maxWidth, null);

					 if (i < n.length())
					 {
							i = mPaint.breakText(n, true, maxWidth - 20*scale, null);
							c.drawText(n.substring(0, i) + "...", x, y, mPaint);
					 }
					 else
					 {
							c.drawText(n.substring(0, i), x, y, mPaint);
					 }
				}
			}

			// Write texture file
			try
			{
				FileOutputStream out = new FileOutputStream(texturepath);
				billboard.compress(Bitmap.CompressFormat.PNG, 90, out);
				Log.v(TAG, "Texture file is saved to "+texturepath);
				return texturepath;
			}
			catch (Exception e)
			{
				Log.v(TAG, "Failed to save texture file");
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			Log.v(TAG, "Error creating annotation texture: " + e.getMessage());
			MetaioDebug.printStackTrace(Log.DEBUG, e);
			return null;
		}
		finally
		{
			if (billboard != null)
			{
				billboard.recycle();
				billboard = null;
			}
		}

		return null;
	}

	protected void onGeometryTouched(final IGeometry geometry) {
		Log.v(TAG,  "Geometry selected: " + geometry);
		
		mSurfaceView.queueEvent(new Runnable()
		{
			public void run() 
			{
				mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath(getApplicationContext(), "VisitareView/yellow.png"));
				mRadar.setObjectTexture(geometry, AssetsManager.getAssetPath(getApplicationContext(), "VisitareView/red.png"));
			}
		
				
		});
	}

	final class MyAnnotatedGeometriesGroupCallback extends AnnotatedGeometriesGroupCallback
	{
		@Override
		public IGeometry loadUpdatedAnnotation(IGeometry geometry, Object userData,
				IGeometry existingAnnotation)
		{
			if (userData == null)
			{
				return null;
			}

			if (existingAnnotation != null)
			{
				// We don't update the annotation if e.g. distance has changed
				return existingAnnotation;
			}

			String title = (String)userData; // as passed to addGeometry
			String texturePath = getAnnotationImageForTitle(title);

			return metaioSDK.createGeometryFromImage(texturePath, true, false);
		}
	}

}