package com.franklinho.vidtrain_android.fragments;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.franklinho.vidtrain_android.Manifest;
import com.franklinho.vidtrain_android.R;
import com.franklinho.vidtrain_android.activities.HomeActivity;
import com.franklinho.vidtrain_android.models.VidTrain;
import com.franklinho.vidtrain_android.networking.VidtrainApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@RuntimePermissions
public class MapFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMarkerClickListener {

    /*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private List<VidTrain> vidTrains;
    private Map<String, VidTrain> vidTrainsMap;
    private List<Marker> markers;
    @Bind(R.id.bottomsheet) BottomSheetLayout bottomsheet;
    @Bind(R.id.btnSearchMap) Button btnSearchMap;
    boolean userGeneratedCameraChange = false;

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
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);
        btnSearchMap.setVisibility(View.GONE);
        return view;
    }

    @OnClick(R.id.btnSearchMap)
    public void searchMap(View view) {
        Log.d(VidtrainApplication.TAG, "search map clicked!");
        requestVidTrains(false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mapFragment = new SupportMapFragment();
        android.support.v4.app.FragmentTransaction
                transaction = getChildFragmentManager().beginTransaction();

        transaction.replace(R.id.map, mapFragment).commit();
        setUpMapIfNeeded();
    }

    private void requestVidTrains(final boolean initialRequest) {
        hideMapSearchButton();
        final HomeActivity homeActivity = (HomeActivity) getActivity();
        homeActivity.showProgressBar();
        final int currentSize;
        vidTrains.clear();
        currentSize = 0;

        // TODO: query based on current location
        ParseQuery<VidTrain> query = ParseQuery.getQuery("VidTrain");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        if (initialRequest) {
            LocationManager lm = (LocationManager) getContext().getSystemService(
                    Context.LOCATION_SERVICE);
            Location lc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lc != null) {
                query.whereNear("ll", new ParseGeoPoint(lc.getLatitude(), lc.getLongitude()));
            }
        } else {
            LatLng currentMapTarget = map.getCameraPosition().target;
            query.whereNear("ll", new ParseGeoPoint(currentMapTarget.latitude, currentMapTarget.longitude));
        }
        query.addDescendingOrder("rankingValue");
        query.setSkip(currentSize);
        query.setLimit(10);
        final BitmapDescriptor defaultMarker = BitmapDescriptorFactory.defaultMarker(
                160.0F);
        query.findInBackground(new FindCallback<VidTrain>() {
            @Override
            public void done(List<VidTrain> objects, ParseException e) {
                homeActivity.hideProgressBar();
                if (e == null) {
                    map.clear();
                    for (VidTrain vidTrain : objects) {
                        vidTrainsMap.put(vidTrain.getObjectId(), vidTrain);
                        Log.d(VidtrainApplication.TAG, vidTrain.getTitle());

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

                    userGeneratedCameraChange = false;
                    map.animateCamera(cu);
                    map.animateCamera(cu, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            userGeneratedCameraChange = false;
                        }

                        @Override
                        public void onCancel() {
                            userGeneratedCameraChange = true;

                        }
                    });


                }

            }
        });
    }

    // The Map is verified. It is now safe to manipulate the map.
    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            map.setOnMarkerClickListener(this);
            map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if (userGeneratedCameraChange) {
                        // User caused onCameraChange...
                        if (btnSearchMap.getVisibility() != View.VISIBLE) {
                            showSearchMapButton();
                        }
                    } else {
                        // The next map move will be caused by user, unless we
                        // do another move programmatically
                        userGeneratedCameraChange = true;

                        // onCameraChange caused by program...
                    }
                }
            });
            MapFragmentPermissionsDispatcher.getMyLocationWithCheck(this);
        } else {
            Toast.makeText(getContext(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
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
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            map.moveCamera(cameraUpdate);
//            map.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
//                @Override
//                public void onFinish() {
//                    userGeneratedCameraChange = false;
//                }
//
//                @Override
//                public void onCancel() {
//                    userGeneratedCameraChange = true;
//
//                }
//            });
        }
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
//        FragmentManager fm = getFragmentManager();
////        MapDialogFragment mapDialogFragment = MapDialogFragment.newInstance(marker.getSnippet());
//        ImagePreviewDialogFragment imagePreviewDialogFragment =  ImagePreviewDialogFragment.newInstance(marker.getSnippet());
//        Bundle bundle = new Bundle();
//        bundle.putString("vidTrainId", marker.getSnippet());
//        imagePreviewDialogFragment.setArguments(bundle);
//        imagePreviewDialogFragment.show(fm, "fragment_image_preview");
//        bottomsheet.showWithSheetView(LayoutInflater.from(getContext()).inflate(R.layout.fragment_image_preview, bottomsheet, false));
        ImagePreviewFragment imagePreviewFragment = ImagePreviewFragment.newInstance(
                marker.getSnippet());
        Bundle bundle = new Bundle();
        bundle.putString("vidTrainId", marker.getSnippet());
        imagePreviewFragment.setArguments(bundle);

        imagePreviewFragment.show(getFragmentManager(), R.id.bottomsheet);
        HomeActivity homeActivity = (HomeActivity) getActivity();
        homeActivity.exitReveal();

        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        // do nothing
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

    public void showSearchMapButton() {
        btnSearchMap.setVisibility(View.VISIBLE);
        ObjectAnimator fadeAltAnim = ObjectAnimator.ofFloat(btnSearchMap, View.ALPHA, 0, 1);
        fadeAltAnim.setDuration(1000);
        fadeAltAnim.start();
    }

    public void hideMapSearchButton() {

        ObjectAnimator fadeAltAnim = ObjectAnimator.ofFloat(btnSearchMap, View.ALPHA, 0, 1);
        fadeAltAnim.setDuration(1000);
        fadeAltAnim.start();
        btnSearchMap.setVisibility(View.GONE);
    }


}
