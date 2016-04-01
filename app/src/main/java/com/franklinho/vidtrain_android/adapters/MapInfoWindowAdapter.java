package com.franklinho.vidtrain_android.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.franklinho.vidtrain_android.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by franklinho on 3/31/16.
 */
public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context = null;

    public MapInfoWindowAdapter(Context context) {
        this.context = context;
    }

    // Hack to prevent info window from displaying: use a 0dp/0dp frame
    @Override
    public View getInfoWindow(Marker marker) {
        View v = ((Activity) context).getLayoutInflater().inflate(R.layout.no_info_window, null);
        return v;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}