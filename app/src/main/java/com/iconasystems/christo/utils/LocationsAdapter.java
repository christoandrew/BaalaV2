package com.iconasystems.christo.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.iconasystems.christo.baalafinal.R;

public class LocationsAdapter extends BaseAdapter {
	JSONObject mResult;
	JSONArray _jRoutes = null;
	JSONArray jLegs = null;
	JSONArray jSteps = null;
	LayoutInflater inflater;
	Context _context;
	private static final String TAG_INSTRUCTION = "instruction";
	private static final String TAG_DURATION = "duration";
	private static final String TAG_DISTANCE = "distance";

	private ArrayList<HashMap<String, String>> _data;

	public LocationsAdapter(ArrayList<HashMap<String, String>> data,
			Context context) {
		this._data = data;
		this._context = context;

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this._data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (inflater == null)
			inflater = (LayoutInflater) _context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			convertView = inflater.inflate(R.layout.location_list_item, null);

		TextView mInstruction = (TextView) convertView
				.findViewById(R.id.location_instruction);
		TextView mDuration = (TextView) convertView
				.findViewById(R.id.location_time);
		TextView mDistance = (TextView) convertView
				.findViewById(R.id.location_distance);

		HashMap map = new HashMap<String, String>();
		map = _data.get(position);
		mInstruction.setText(Html.fromHtml((String) map.get(TAG_INSTRUCTION)));
		mDistance.setText((String) map.get(TAG_DISTANCE));
		mDuration.setText((String) map.get(TAG_DURATION));

		return convertView;
	}

}
