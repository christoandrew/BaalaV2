package com.iconasystems.christo.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iconasystems.christo.baalafinal.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Christo on 11/19/2014.
 */
public class EventListAdapter extends BaseAdapter {

    public final Activity activity;
    public final ArrayList<HashMap<String, String>> eventsList;
    private LayoutInflater inflater;

    private static final String TAG_EVENT_DATE = "event_date";
    private static final String TAG_EVENT_NAME = "event_name";
    private static final String TAG_EVENT_ID = "event_id";
    private static final String TAG_EVENT_LOCATION = "name";
    private static final String TAG_EVENT_DRESS_CODE = "event_dress_code";
    private static final String TAG_EVENT_IMAGE = "event_image";
    private static final String TAG_EVENT_ENTRANCE = "entrance";


    private TextView mEventName;
    private TextView mEventDate;
    private TextView mEventLocation;
    private TextView mEventDressCode;
    private TextView mEventId;
    private TextView mEventEntrance;
    private ImageView mEventImage;
    private DisplayImageOptions options;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    public EventListAdapter(Activity activity, ArrayList<HashMap<String, String>> eventsList) {
        this.activity = activity;
        this.eventsList = eventsList;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return eventsList.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return position;
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_empty)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) view = inflater.inflate(R.layout.events_list_item, null);

        mEventDate = (TextView) view.findViewById(R.id.event_list_date);
        mEventDressCode = (TextView) view.findViewById(R.id.event_list_dress_code);
        mEventEntrance = (TextView) view.findViewById(R.id.event_list_entrance);
        mEventId = (TextView) view.findViewById(R.id.event_list_id);
        mEventLocation = (TextView) view.findViewById(R.id.event_list_location);
        mEventName = (TextView) view.findViewById(R.id.event_list_name);
        mEventImage = (ImageView) view.findViewById(R.id.event_list_image);

        HashMap<String, String> map;
        map = eventsList.get(position);

        mEventDressCode.setText(map.get(TAG_EVENT_DRESS_CODE));
        mEventDate.setText(map.get(TAG_EVENT_DATE));
        mEventName.setText(map.get(TAG_EVENT_NAME));
        mEventEntrance.setText(map.get(TAG_EVENT_ENTRANCE));
        mEventLocation.setText(map.get(TAG_EVENT_LOCATION));
        mEventId.setText(map.get(TAG_EVENT_ID));

        String image_url = "http://10.0.3.2/baala/assets/images/events/"+map.get(TAG_EVENT_IMAGE);

        ImageLoader.getInstance().displayImage(image_url, mEventImage, options, animateFirstListener);

       /* Picasso.with(activity.getApplicationContext())
                .load(image_url)
                .error(R.drawable.ic_action_error)
                .into(mEventImage);*/

        return view;
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }
}
