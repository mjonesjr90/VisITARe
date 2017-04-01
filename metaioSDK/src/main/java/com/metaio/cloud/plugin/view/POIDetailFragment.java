/**
 * POIDetailDialog.java Junaio 2.0 Android
 * 
 * 
 * @author Created by Arsalan Malik on 09.04.2010 Copyright 2010 metaio GmbH. All rights reserved.
 * 
 */
package com.metaio.cloud.plugin.view;

import java.util.ArrayList;
import java.util.List;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.metaio.R;
import com.metaio.cloud.plugin.MetaioCloudPlugin;
import com.metaio.cloud.plugin.util.MetaioCloudUtils;
import com.metaio.sdk.jni.IARELObject;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.ObjectButton;
import com.metaio.sdk.jni.ObjectButtonVector;
import com.metaio.sdk.jni.ObjectPopup;

public class POIDetailFragment extends ListFragment
{

	/**
	 * POI object
	 */
	private IARELObject mPOI;

	private RemoteImageView poiThumbnail;

	private TextView poiName;

	private TextView poiDescription;

	private RatingBar poiRating;

	private RemoteImageView attributionIcon;

	private TextView poiLocation;

	private boolean isDescriptionExpanded = false;

	public POIDetailFragment()
	{}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mPOI = MetaioCloudPlugin.getDataManager().getSelectedPOI();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.poidetaildialog, container, false);

		poiName = (TextView) v.findViewById(R.id.textPOIName);
		poiDescription = (TextView) v.findViewById(R.id.textPOIDescription);
		poiDescription.setMovementMethod(new ScrollingMovementMethod());

		if (Build.VERSION.SDK_INT >= 16)
			((ViewGroup) poiDescription.getParent()).getLayoutTransition().enableTransitionType(
					LayoutTransition.CHANGING);

		poiRating = (RatingBar) v.findViewById(R.id.ratingbar);
		attributionIcon = (RemoteImageView) v.findViewById(R.id.imageAttribution);
		poiLocation = (TextView) v.findViewById(R.id.textPOILocation);

		poiThumbnail = (RemoteImageView) v.findViewById(R.id.imagePOIThumbnail);

		updateGUI(v);
		return v;
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		MetaioCloudPlugin.log("POIDetailDialog.onCreate()");

		// get the selected poi, if it is null, there is nothing to see here, return. to select a
		// POI call
		// JunaioPlugin.getDataManager(this).selectPOI(poi);
//		mPOI = MetaioCloudPlugin.getDataManager().getSelectedPOI();
//
//		if (mPOI == null)
//		{
//			MetaioCloudPlugin.log(Log.ERROR, "Selected POI is null!");
//			getActivity().finish();
//		}
//		else
//		{
//			updateGUI(null);
//		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		if (poiThumbnail != null)
			poiThumbnail.cancelDownload();

		// MetaioCloudUtils.unbindDrawables(getView().findViewById(android.R.id.content));

	}

	/**
	 * Update GUI from currently selected POI Load POI actions
	 */
	private void updateGUI(View v)
	{
		try
		{

			if (v == null)
				v = getView();

			if (mPOI.getThumbnailURL().length() > 0)
			{
				final String thumbnailURL = mPOI.getThumbnailURL();
				poiThumbnail.setRemoteSource(new String(thumbnailURL));
			}
			else
			{
				poiThumbnail.setVisibility(View.INVISIBLE);
			}

			final String name = mPOI.getName();
			if (name != null && name.length() >= 0)
				poiName.setText(name);

			poiDescription.setText(mPOI.getDescription());

			// add clickable links to strings like emails and websites
			Linkify.addLinks(poiDescription, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);

			poiDescription.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v)
				{
					TextView tv = (TextView) v;

//					if (isDescriptionExpanded)
//					{
////						if (Build.VERSION.SDK_INT > 10)
////						{
////							ObjectAnimator animation =
////									ObjectAnimator.ofInt(tv, "maxLines", tv.getLineCount(), 5);
////							animation.setDuration(300);
////							animation.start();
////						}
////						else
////						{
//
//						tv.setMaxLines(5);
//						tv.setEllipsize(TruncateAt.END);
////						}
//						isDescriptionExpanded = false;
//
//					}
//					else
//					// is collapsed
//					{
////						if (Build.VERSION.SDK_INT > 16)
////						{
////							ObjectAnimator animation = ObjectAnimator.ofInt(tv, "maxLines", 20);
////							animation.setDuration(500);
////							animation.start();
////						}
////						else
////						{
//						tv.setMaxLines(Integer.MAX_VALUE);
//						tv.setEllipsize(null);
////						}
//						isDescriptionExpanded = true;
//
//					}
					toggleTextView(tv);

				}
			});

			// show location information only if the POI has LLA coordinates
			if (mPOI.hasLLA())
			{

				LLACoordinate mylocation =
						MetaioCloudPlugin.getSensorsManager(getActivity().getApplicationContext())
								.getLocation();

				// get the distance and store in results[0], get the bearing and
				// store it in results[1]
				float[] results = new float[2];
				Location.distanceBetween(mylocation.getLatitude(), mylocation.getLongitude(), mPOI
						.getLocation().getLatitude(), mPOI.getLocation().getLongitude(), results);

				// get the proper units. To change units see JunaioPlugin.Settings.useImperialUnits
				poiLocation.setText(MetaioCloudUtils.getRelativeLocationString(
						mPOI.getCurrentDistance(), 0, false,
						MetaioCloudPlugin.Settings.useImperialUnits));

				MetaioCloudPlugin.log("Bearing: " + results[1]);

				poiLocation.setVisibility(View.VISIBLE);
			}
			else
			{
				poiLocation.setVisibility(View.INVISIBLE);
			}

			String url = mPOI.getARELParameter("poi-attribution-image");
			if (!TextUtils.isEmpty(url))
				attributionIcon.setRemoteSource(url);
			else
				attributionIcon.setVisibility(View.GONE);

			String rating = mPOI.getARELParameter("poi-rating");
			if (!TextUtils.isEmpty(rating))
			{
				poiRating.setRating(Float.parseFloat(rating));
			}
			else
			{
				poiRating.setVisibility(View.INVISIBLE);
			}

			loadPOIActions();

		}
		catch (Exception e)
		{
			MetaioCloudPlugin.log("POIDetailDialog.updateGUI: " + e.getMessage());
		}
	}

	private void expandTextView(TextView tv)
	{
		LinearLayout.LayoutParams expanded =
				new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		LinearLayout parent = (LinearLayout) tv.getParent();
		parent.setLayoutParams(expanded);
		tv.setEllipsize(null);
		isDescriptionExpanded = true;
	}

	private void collapseTextView(TextView tv)
	{
		LinearLayout.LayoutParams collapsed =
				new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 100);
		LinearLayout parent = (LinearLayout) tv.getParent();
		parent.setLayoutParams(collapsed);
		tv.setEllipsize(TruncateAt.END);
		isDescriptionExpanded = false;
	}

	private void toggleTextView(TextView tv)
	{
		if (isDescriptionExpanded)
			collapseTextView(tv);
		else
			expandTextView(tv);
	}

	class PoiActionsAdapter extends BaseAdapter
	{
		ArrayList<ObjectButton> buttonList;

		public void add(ObjectButton button)
		{
			buttonList.add(button);
		}

		public PoiActionsAdapter(ObjectButtonVector buttons)
		{
			buttonList = new ArrayList<ObjectButton>((int) buttons.size());
			for (int i = 0, j = (int) buttons.size(); i < j; i++)
			{
				ObjectButton button = buttons.get(i);
				buttonList.add(button);
			}
		}

		@Override
		public int getCount()
		{
			return buttonList.size();
		}

		@Override
		public Object getItem(int index)
		{
			return buttonList.get(index);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;
			ObjectButton button = buttonList.get(position);

			if (v == null)
			{
				v =
						LayoutInflater.from(getActivity()).inflate(R.layout.button_action_detail,
								parent, false);
			}

			String text =
					MetaioCloudPlugin.getResourceString(getActivity().getApplicationContext(),
							button.getButtonName());
			TextView tv = (TextView) v;
			if (text != null)
				tv.setText(text);
			else
			{
				tv.setText(button.getButtonName());
			}

			v.setTag(button.getButtonValue());

			return v;
		}

	}

	/**
	 * Set POI action buttons
	 */
	private void loadPOIActions()
	{
		// get the popup objet and add the buttons to the container
		ObjectPopup popup = mPOI.getObjectPopup();

		PoiActionsAdapter adapter = new PoiActionsAdapter(popup.getButtons());

		// if the routing is enabled, check if we have a navigation intent handler and add a button
		// for navigation
		if (mPOI.isRoutingEnabled())
		{
			Intent intent =
					new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("google.navigation:q="
							+ mPOI.getLocation().getLatitude() + ","
							+ mPOI.getLocation().getLongitude()));
			List<ResolveInfo> list =
					getActivity().getPackageManager().queryIntentActivities(intent,
							PackageManager.MATCH_DEFAULT_ONLY);
			if (list.size() > 0)
			{
				ObjectButton button = new ObjectButton();
				button.setButtonName(getString(R.string.MSG_TITLE_DIRECTIONS));
				button.setButtonValue("google.navigation:q=" + mPOI.getLocation().getLatitude()
						+ "," + mPOI.getLocation().getLongitude());
				adapter.add(button);
			}
		}

		setListAdapter(adapter);
		getListView().setSelector(R.drawable.simple_button_background_selector);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		String url = (String) v.getTag();

		if (url.toLowerCase().startsWith("junaio://") || url.toLowerCase().startsWith("javascript"))
		{
			Intent result = new Intent();
			result.putExtra("url", url);
			getActivity().setResult(Activity.RESULT_OK, result);
			getActivity().finish();
		}
		else
		{
			Intent intent = new Intent(getActivity().getPackageName() + ".PROCESSURL");
			intent.putExtra("url", url);
			LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
		}
	}

//	private OnItemClickListener poiActionButtonClickListener = new OnItemClickListener() {
//		@Override
//		public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
//		{
//			String url = (String) v.getTag();
//
//			if (url.toLowerCase().startsWith("junaio://")
//					|| url.toLowerCase().startsWith("javascript"))
//			{
//				Intent result = new Intent();
//				result.putExtra("url", url);
//				getActivity().setResult(Activity.RESULT_OK, result);
//				getActivity().finish();
//			}
//			else
//			{
//				Intent intent = new Intent(getActivity().getPackageName() + ".PROCESSURL");
//				intent.putExtra("url", url);
//				LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
//			}
//
//		}
//
//	};


//	/**
//	 * Adds a button from the ObjectButton data to the root container
//	 * 
//	 * @param button ObjectButton to add
//	 * @param root ViewGroup where it will be added
//	 */
//	private void addButton2(ObjectButton button, ViewGroup root)
//	{
//		Button actionButton =
//				(Button) LayoutInflater.from(getActivity()).inflate(R.layout.button_action_detail,
//						root, false);
//		String text =
//				MetaioCloudPlugin.getResourceString(getActivity().getApplicationContext(),
//						button.getButtonName());
//		if (text != null)
//			actionButton.setText(text);
//		else
//		{
//			actionButton.setText(button.getButtonName());
//		}
//
//		actionButton.setOnClickListener(actionClickListener2);
//		// add the value (probably an URL) to the tag so it can be extracted on
//		// the listener
//		actionButton.setTag(button.getButtonValue());
//		root.addView(actionButton, 0);
//	}



//	private final View.OnClickListener actionClickListener2 = new View.OnClickListener() {
//
//		@Override
//		public void onClick(View v)
//		{
//			String url = (String) v.getTag();
//
//			if (url.toLowerCase().startsWith("junaio://")
//					|| url.toLowerCase().startsWith("javascript"))
//			{
//				Intent result = new Intent();
//				result.putExtra("url", url);
//				getActivity().setResult(Activity.RESULT_OK, result);
//				getActivity().finish();
//			}
//			else
//			{
//				Intent intent = new Intent(getActivity().getPackageName() + ".PROCESSURL");
//				intent.putExtra("url", url);
//				LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
//			}
//
//		}
//	};
}
