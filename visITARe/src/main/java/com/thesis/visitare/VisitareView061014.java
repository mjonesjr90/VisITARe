package com.thesis.visitare;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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

public class VisitareView061014 extends ARViewActivity{
	String TAG = "Thesis";
	String siteURL = "http://malcomjonesjr.com/json/nodes.json";
	//Model geometry
	private IGeometry mDC, mMunichGeo, mRomeGeo, mTokyoGeo, mParisGeo, mNewportNews;
	
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

	public void onDrawFrame()
	{
		if (metaioSDK != null && mSensors != null)
		{
			SensorValues sensorValues = mSensors.getSensorValues();

			float heading = 0.0f;
			if (sensorValues.hasAttitude())
			{
				float m[] = new float[9];
				sensorValues.getAttitude().getRotationMatrix(m);

				Vector3d v = new Vector3d(m[6], m[7], m[8]);
				v = v.normalize();

				heading = (float)(-Math.atan2(v.getY(), v.getX()) - Math.PI/2.0);
			}

			IGeometry geos[] = new IGeometry[] {mDC, mParisGeo, mRomeGeo, mTokyoGeo, mNewportNews};
			Rotation rot = new Rotation((float)(Math.PI/2.0), 0.0f, -heading);
			for (IGeometry geo : geos)
			{
				if (geo != null)
				{
					geo.setRotation(rot);
				}
			}
		}

		super.onDrawFrame();
	}
	
	public void onButtonClick(View v){
		Log.v(TAG,"ARView closing");
		finish();
		Log.v(TAG,"ARView closed");
	}
	
	protected int getGUILayout() {
		Log.v(TAG,"Set visitare view");
		return R.layout.activity_visitaire_view;
	}

	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
		Log.v(TAG,"Entering getMetaioSDKCallbackHandler");
		return null;
	}

	public String convertJsonToStringUsingHttpResponse(String url) throws IOException {
    	Log.v(TAG, "Entering HTTP conversion method");
    	InputStream contentStream = null;
        StringBuilder builder = new StringBuilder();
        
        HttpClient client = new DefaultHttpClient();
        Log.d(TAG, "Created HTTPClient");
        HttpGet httpGet = new HttpGet(url);
        Log.d(TAG, "Created HTTPGet");
        HttpResponse response = client.execute(httpGet);
        Log.d(TAG, "Created HTTPResponse");
        //int statusCode = response.getStatusLine().getStatusCode();
        //if (statusCode == 200) {
            // get contents from stream through entity
            contentStream = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(contentStream));
            String line;
            while ((line = reader.readLine()) != null){
            	builder.append(line);
            }
            Log.d(TAG, "HTTP: "+ builder.toString());
        return builder.toString();
    }
	
	private void loadNodeData(ArrayList<Node> arr){
		//Setup HTTP connection
		Node n1 = new Node(00, new NodeLocation(38.90775, -77.04341, 0), String.valueOf(new java.util.Date()), "Washington");
		Node n2 = new Node(01, new NodeLocation(37.09784, -76.46007, 0), String.valueOf(new java.util.Date()), "Newport News");
		arr.add(n1);
		arr.add(n2);
	}
	
	protected void loadContents() {
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
		
		Log.v(TAG, "Pull node data");
		//ReadJSON read = new ReadJSON();
		nodeArray = JSONHelper.parseAll(siteURL);
		Log.d(TAG, "Trying to read "+siteURL);
		//read.execute(siteURL);
		//nodeArray = JSONHelper.parseAll("http://malcomjonesjr.com/json/nodes.json");
		//This is where I would call loadNodeData doing the JSON method
		//loadNodeData(nodeArray);
		
		// create radar
		//Log.v(TAG, "AsyncTask complete");
		mRadar = metaioSDK.createRadar();
		mRadar.setBackgroundTexture(AssetsManager.getAssetPath(getApplicationContext(), "VisitareView/radar.png"));
		mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath(getApplicationContext(), "VisitareView/yellow.png"));
		mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);
		
		//Create locations for the cities/nodes
		//For VisITARe, these coordinates will be loaded from the nodes themselves
		Log.v(TAG, "Create locations for nodes");
		
		//Iterate through nodes to make LLAs
		Log.v(TAG, "Array size " + nodeArray.size() );
		for(int i = 0; i < nodeArray.size(); i++){
			mAnnotatedGeometriesGroup.addGeometry(
					createPOIGeometry(
							new LLACoordinate(
									nodeArray.get(i).getLocation().getLat(), 
									nodeArray.get(i).getLocation().getLon(), 
									nodeArray.get(i).getLocation().getAlt(), 0)), 
							String.valueOf(nodeArray.get(i).nodeID)+": "+String.valueOf(nodeArray.get(i).data));
			mRadar.add(createPOIGeometry(
							new LLACoordinate(
									nodeArray.get(i).getLocation().getLat(), 
									nodeArray.get(i).getLocation().getLon(), 
									nodeArray.get(i).getLocation().getAlt(), 0)));
		}
//		LLACoordinate dc = new LLACoordinate(nodeArray.get(i).getLocation().getLat(), nodeArray.get(i).getLocation().getLon(), nodeArray.get(i).getLocation().getAlt(), 0);
//		LLACoordinate munich = new LLACoordinate(48.142573, 11.550321, 0, 0);
//		LLACoordinate dc = new LLACoordinate(38.90775, -77.04341, 0, 0);
//		LLACoordinate tokyo = new LLACoordinate(35.657464, 139.773865, 0, 0);
//		LLACoordinate rome = new LLACoordinate(41.90177, 12.45987, 0, 0);
//		LLACoordinate paris = new LLACoordinate(48.85658, 2.348671, 0, 0);
//		LLACoordinate newportnews = new LLACoordinate(37.09784, -76.46007, 0, 0);
		
		// Load some POIs. Each of them has the same shape at its geoposition. We pass a string
		// (const char*) to IAnnotatedGeometriesGroup::addGeometry so that we can use it as POI title
		// in the callback, in order to create an annotation image with the title on it.
//		Log.v(TAG, "Create dc");
//		mDC = createPOIGeometry(dc);
//		Log.v(TAG, "dc: " + dc);
//		Log.v(TAG, "dc Geo: " + mDC);
//		mAnnotatedGeometriesGroup.addGeometry(mDC, "Washington");
//
//		mParisGeo = createPOIGeometry(paris);
//		mAnnotatedGeometriesGroup.addGeometry(mParisGeo, "Paris");
//
//		mRomeGeo = createPOIGeometry(rome);
//		mAnnotatedGeometriesGroup.addGeometry(mRomeGeo, "Rome");
//
//		mTokyoGeo = createPOIGeometry(tokyo);
//		mAnnotatedGeometriesGroup.addGeometry(mTokyoGeo, "Tokyo");
//		
//		mNewportNews = createPOIGeometry(newportnews);
//		mAnnotatedGeometriesGroup.addGeometry(mNewportNews, "Newport News");
		
//		String metaioManModel = AssetsManager.getAssetPath(getApplicationContext(), "VisitareView/metaioman.md2");
//		if (metaioManModel != null)
//		{
//			mMunichGeo = metaioSDK.createGeometry(metaioManModel);
//			if (mMunichGeo != null)
//			{
//				mMunichGeo.setTranslationLLA(munich);
//				mMunichGeo.setLLALimitsEnabled(true);
//				mMunichGeo.setScale(500);
//			}
//			else
//			{
//				MetaioDebug.log(Log.ERROR, "Error loading geometry: " + metaioManModel);
//			}
//		}
						
		// add geometries to the radar
//		mRadar.add(mDC);
//		mRadar.add(mMunichGeo);
//		mRadar.add(mTokyoGeo);
//		mRadar.add(mParisGeo);
//		mRadar.add(mRomeGeo);
//		mRadar.add(mNewportNews);
		
//		while(true){
//			try {
//			    Thread.sleep(1000);
//			} catch(InterruptedException ex) {
//			    Thread.currentThread().interrupt();
//			}
//			loadContents();
//		}
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
	
	private class ReadJSON extends AsyncTask<String, Void, ArrayList<Node>>{
    	String TAG = "AsyncTask";
		@Override
		protected void onPostExecute(ArrayList<Node> result) {
//			super.onPostExecute(result);
			Log.d(TAG, "onPostExecute called");
			cancel(true);
//			CustomAdapter adapter = new CustomAdapter(context, R.id.relLayout, data, siteURL);
//			Log.d(TAG, "Adapter created");
//			setListAdapter(adapter);
//			Log.d(TAG, "Set Adapter");
		}

		@Override
		protected ArrayList<Node> doInBackground(String... params) {
			Log.d(TAG, "doInBackground gets called with "+params[0]);
			nodeArray = JSONHelper.parseAll(params[0]);
			Log.d(TAG, "ParseAll called");
			return null;
		}
    	
    }
}