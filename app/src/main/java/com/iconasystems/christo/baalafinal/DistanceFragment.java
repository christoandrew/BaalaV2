package com.iconasystems.christo.baalafinal;


import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;


/**
 * A simple {@link Fragment} subclass.
 */
public class DistanceFragment extends Fragment {


    private ProgressBar mProgressBar;
    private SmoothProgressDrawable d;

    public DistanceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_distance, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mProgressBar = new ProgressBar(getActivity(), null, R.attr.spbStyle);
        mProgressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 24));
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.GONE);

        final FrameLayout decorView = (FrameLayout) getActivity().getWindow().getDecorView();

        SmoothProgressDrawable.Builder builder = new SmoothProgressDrawable.Builder(getActivity());
        builder.speed(5)
                .sectionsCount(3)
                .separatorLength(dpToPx(0))
                .width(dpToPx(4))
                .mirrorMode(true)
                .reversed(true);

        Interpolator interpolator = new AccelerateDecelerateInterpolator();
        builder.interpolator(interpolator);

        builder.colors(this.getResources().getIntArray(R.array.uganda));
        d = builder.build();

        d.setBounds(mProgressBar.getIndeterminateDrawable().getBounds());
        mProgressBar.setIndeterminateDrawable(d);
        d.start();

        decorView.addView(mProgressBar);

        final ViewTreeObserver observer = mProgressBar.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                View contentView = decorView.findViewById(android.R.id.content);
                mProgressBar.setY(contentView.getY() - 10);

                ViewTreeObserver observer1 = mProgressBar.getViewTreeObserver();
                observer1.removeOnGlobalLayoutListener(this);
            }
        });
    }

    public int dpToPx(int dp) {
        Resources r = getActivity().getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, r.getDisplayMetrics());
        return px;
    }
}
