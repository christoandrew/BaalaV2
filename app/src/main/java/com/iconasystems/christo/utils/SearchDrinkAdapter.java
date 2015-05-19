package com.iconasystems.christo.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.view.LayoutInflater;
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
 * Created by Christo on 11/11/2014.
 */
public class SearchDrinkAdapter extends BaseAdapter {
    private final Activity activity;
    private final ArrayList<HashMap<String, String>> drinksList;
    private LayoutInflater inflater;
    private TextView mDrinkName;
    private TextView mDrinkPrice;
    private ImageView mBeerPhoto;
    private TextView mDrinkId;
    private TextView mBarName;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    private static final String TAG_DRINK_NAME = "drink_name";
    private static final String TAG_DRINK_ID = "drink_id";
    private static final String TAG_DRINK_PRICE = "price";
    private static final String TAG_DRINK_IMAGE = "drink_image";
    private static final String TAG_BAR_NAME = "bar_name";
    private DisplayImageOptions options;


    public SearchDrinkAdapter(Activity activity, ArrayList<HashMap<String, String>> drinksList) {
        this.activity = activity;
        this.drinksList = drinksList;
    }

    @Override
    public int getCount() {
        return this.drinksList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

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
        if (view == null) view = inflater.inflate(R.layout.drink_search_item, null);

        mDrinkId = (TextView) view.findViewById(R.id.drink_id);
        mDrinkName = (TextView) view.findViewById(R.id.drink_name);
        mBeerPhoto = (ImageView) view.findViewById(R.id.search_beer_photo);
        mDrinkPrice = (TextView) view.findViewById(R.id.price);
        mBarName = (TextView) view.findViewById(R.id.bar_search_name);

        HashMap<String, String> map;
        map = drinksList.get(position);

        mDrinkId.setText(map.get(TAG_DRINK_ID));
        mDrinkName.setText(map.get(TAG_DRINK_NAME));
        mDrinkPrice.setText(new StringBuilder().append("Shs:").append(map.get(TAG_DRINK_PRICE)));
        mBarName.setText(new StringBuilder().append("At ").append(map.get(TAG_BAR_NAME)));

        String image_url = map.get(TAG_DRINK_IMAGE);

        ImageLoader.getInstance().displayImage(image_url, mBeerPhoto, options, animateFirstListener);

        /*Picasso.with(activity.getApplicationContext())
                .load(image_url)
                .into(mBeerPhoto);*/
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
