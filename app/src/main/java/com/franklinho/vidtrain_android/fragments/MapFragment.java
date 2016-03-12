package com.franklinho.vidtrain_android.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.franklinho.vidtrain_android.Manifest;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.adapters.CustomWindowAdapter;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@RuntimePermissions
public class MapFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMarkerClickListener {

    private long UPDATE_INTERVAL = 10 * 60000;  /* 10 * 60 secs */
    private long FASTEST_INTERVAL = 10 * 5000; /* 5 secs */
    private SupportMapFragment mapFragment;
    private OnFragmentInteractionListener mListener;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private List<VidTrain> vidTrains;
    private Map<String, VidTrain> vidTrainsMap;

    List<Marker> markers;

    /*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LayoutInflater mInflater;
    private VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vidTrains = new ArrayList<>();
        markers = new ArrayList<>();
        vidTrainsMap = new HashMap<>();
//        if (getArguments() != null) {}
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        if (map != null) {
            // Now that map has loaded, let's get our location!
            map.setMyLocationEnabled(true);
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            connectClient();
        }
    }

    protected void connectClient() {
        // Connect the client.
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mapFragment = new SupportMapFragment();
        android.support.v4.app.FragmentTransaction
                transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.map, mapFragment).commit();
        setUpMapIfNeeded();
    }

    private void requestVidTrains(final boolean newTimeline) {
        final int currentSize;
        if (newTimeline) {
            vidTrains.clear();
            currentSize = 0;
        } else {
            currentSize  = vidTrains.size();
        }

        // TODO: query based on current location
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.addDescendingOrder("createdAt");
        query.setSkip(currentSize);
        query.setLimit(5);
        final BitmapDescriptor defaultMarker = BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_GREEN);
        query.findInBackground(new FindCallback<VidTrain>() {
            @Override
            public void done(List<VidTrain> objects, ParseException e) {
                if (e == null) {
                    for (VidTrain vidTrain : objects) {
                        vidTrainsMap.put(vidTrain.getObjectId(), vidTrain);
                        Log.d("Vidtrain", vidTrain.getTitle());
                        Log.d("Vidtrain", vidTrain.getLatLong().toString());

//                        vidTrain.getVideos().get(0)

                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(vidTrain.getLatLong())
                                .title(vidTrain.getTitle())
                                .snippet(vidTrain.getObjectId())
                                .icon(defaultMarker));

                        markers.add(marker);

                    }
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker marker : markers) {
                        builder.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder.build();
                    int padding = 100;
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                    map.animateCamera(cu);
//                    map.setInfoWindowAdapter(new CustomWindowAdapter(mInflater, vidTrainsMap,
//                            mVideoPlayerManager));
                }

            }
        });
    }

    // The Map is verified. It is now safe to manipulate the map.
    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            map.setOnMarkerClickListener(this);
            // Map is ready
            Toast.makeText(getContext(), "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
//            map.setOnMapLongClickListener(this);
            MapFragmentPermissionsDispatcher.getMyLocationWithCheck(this);
        } else {
            Toast.makeText(getContext(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    protected void setUpMapIfNeeded() {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                loadMap(map);
                requestVidTrains(true);
            }
        });

    }

    /*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
    @Override
    public void onConnected(Bundle bundle) {
        // Display the connection status
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            Toast.makeText(getContext(), "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            map.animateCamera(cameraUpdate);
        } else {
            Toast.makeText(getContext(), "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    /*
     * Called by Location Services if the connection to the location client
     * drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(getContext(), "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(getContext(), "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
//        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        FragmentManager fm = getFragmentManager();
        MapDialogFragment mapDialogFragment = MapDialogFragment.newInstance(marker.getSnippet());
        Bundle bundle = new Bundle();
        bundle.putString("vidTrainId", marker.getSnippet());
        mapDialogFragment.setArguments(bundle);
        mapDialogFragment.show(fm, "custom_info_window");
        return true;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
