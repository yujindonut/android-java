package ddwucom.mobileapplication.ma02_20181030.google_map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ddwucom.mobileapplication.ma02_20181030.R;
import ddwucom.mobileapplication.ma02_20181030.record_memo.AddMemoActivity;
import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    final static String TAG = "MapActivity";
    public static final int MAP_REQUEST_CODE = 100;
    final static int PERMISSION_REQ_CODE = 1;
    final static int DETAIL_SUCCESS_CODE = 2;
    Bitmap bitmap = null;
    LatLng lastLoc;
    /*UI*/
    private EditText etKeyword;
    private GoogleMap mGoogleMap;
    private MarkerOptions markerOptions;
    private Marker centerMarker;
    private LocationManager locationManager;


    ArrayList<Marker> marker_list;
    private PolylineOptions pOptions;
    private String addressOutput = null;
    private String bestProvider;

    private Geocoder geocoder;
    /*DATA*/
    private PlacesClient placesClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
//
//        etKeyword = findViewById(R.id.etKeyword);

        //??? ????????????
        mapLoad();
        //?????? ??????
        Places.initialize(getApplicationContext(), getResources().getString(R.string.api_key) );
        placesClient = Places.createClient(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        bestProvider = LocationManager.GPS_PROVIDER;
        geocoder = new Geocoder(this, Locale.getDefault());
        marker_list = new ArrayList<>();
        //????????? ??????
        pOptions = new PolylineOptions();
        pOptions.color(Color.BLUE);
        pOptions.width(5);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkPermission()){

            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //????????? ????????? ???????????? ????????? - ?????? ?????? ?????? ???????????? ???????????? ??????
            lastLoc = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            searchStart(PlaceType.CAFE);
        }

    }
    /*????????? ????????? ?????? ????????? ??????*/
    private void searchStart(String type) {
        new NRPlaces.Builder().listener(placesListener)
                .key(getResources().getString(R.string.api_key))
                .latlng(lastLoc.latitude,lastLoc.longitude)
                .radius(49999)
                .type(type)
                .build()
                .execute();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == DETAIL_SUCCESS_CODE){
            Intent intent = new Intent();
            setResult(MAP_REQUEST_CODE, intent);
        }
    }


    private void locationUpdate() {
        if(checkPermission()){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000,5,locationListener);
        }
    }
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc,17));

            MarkerOptions options = new MarkerOptions();
            options.title("?????? ??????");
            options.snippet(addressOutput);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            centerMarker.setPosition(currentLoc);
            centerMarker.showInfoWindow();
            centerMarker = mGoogleMap.addMarker(options);

            //?????? ????????? ??? ????????? ????????? ??????, ????????? ?????? ?????????
            pOptions.add(currentLoc);
            //???????????? ??????
            mGoogleMap.addPolyline(pOptions);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };
    /*Place ID ??? ????????? ?????? ???????????? ??????*/
    private void getPlaceDetail(String placeId) {
        //field : Google api??? field
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.OPENING_HOURS,
                Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(
                new OnSuccessListener<FetchPlaceResponse>() {

                    @Override
                    public void onSuccess(FetchPlaceResponse response) {
                        Place place = response.getPlace();//placeFileds??? ?????? ????????? ????????????

                        Log.d(TAG, "Place found " + place.getName());
                        Log.d(TAG, "Address: " + place.getAddress());
                        Log.d(TAG, "OpeningHours: " + place.getOpeningHours());

                        // Get the photo metadata.
                        final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
                        if (metadata == null || metadata.isEmpty()) {
                            Log.w(TAG, "No photo metadata.");
                            return;
                        }
                        final PhotoMetadata photoMetadata = metadata.get(0);

                        // Get the attribution text.
                        final String attributions = photoMetadata.getAttributions();
                        Log.d(TAG, "photo attribute: " + attributions);
                        // Create a FetchPhotoRequest.
                        final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                .setMaxWidth(500) // Optional.
                                .setMaxHeight(300) // Optional.
                                .build();
                        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                            bitmap = fetchPhotoResponse.getBitmap();
                            callDetailActivity(place);
                        }).addOnFailureListener((exception) -> {
                            if (exception instanceof ApiException) {
                                final ApiException apiException = (ApiException) exception;
                                Log.e(TAG, "Place not found: " + exception.getMessage());
                                final int statusCode = apiException.getStatusCode();
                                // TODO: Handle error with given status code.
                            }
                        });
                    }
                }
        ).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ApiException apiException = (ApiException) e;
                        int statusCode = apiException.getStatusCode();
                        Log.e(TAG, "Place not found: " + statusCode + " " + e.getMessage());
                    }
                }
        );
    }

    private void callDetailActivity(Place place) {
        Intent intent = new Intent(MapActivity.this, DetailActivity.class);
        intent.putExtra("place",place);
        startActivityForResult(intent,DETAIL_SUCCESS_CODE);
    }

    PlacesListener placesListener = new PlacesListener() {
        @Override
        public void onPlacesFailure(PlacesException e) {}
        @Override
        public void onPlacesStart() {}
        @Override
        public void onPlacesSuccess(final List<noman.googleplaces.Place> places) {
            Log.d(TAG, "Adding Markers");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //????????????
                    for(noman.googleplaces.Place place : places) {
                        markerOptions.title(place.getName());
                        markerOptions.position(new LatLng(place.getLatitude(), place.getLongitude()));
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                        Marker newMarker = mGoogleMap.addMarker(markerOptions);
                        newMarker.setTag(place.getPlaceId());
                        //????????? ???????????? ???????????? ??????????????? getTag??? google??????????????? ????????? ??? ??????
                        Log.d(TAG, place.getName() + " " + place.getPlaceId());
                    }
                }
            });
        }
        @Override
        public void onPlacesFinished() {}
    };

    public void onMapReady(GoogleMap googleMap) {
        markerOptions = new MarkerOptions();
        mGoogleMap = googleMap;

        Log.d(TAG,"Map ready");

        if(checkPermission()){
            mGoogleMap.setMyLocationEnabled(true);
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //????????? ????????? ???????????? ????????? - ?????? ?????? ?????? ???????????? ???????????? ??????
            LatLng lastLoc = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLoc,17));

        }
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Toast.makeText(MapActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        mGoogleMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
            @Override
            public void onMyLocationClick(@NonNull Location location) {
                Toast.makeText(MapActivity.this, String.format("????????????:(%f, %f)", location.getLatitude(), location.getLongitude()), Toast.LENGTH_SHORT).show();
            }
        });
        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                if(marker.getTag() == null){
                    List<String> address = getAddress(marker.getPosition().latitude, marker.getPosition().longitude);
                    Toast.makeText(MapActivity.this, address.toString(),Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MapActivity.this, AddMemoActivity.class);
                    intent.putExtra("address",address.toString());
                    startActivityForResult(intent,DETAIL_SUCCESS_CODE);
                } else {
                    String placeId = marker.getTag().toString();
                    getPlaceDetail(placeId);
                }
            }
        });
        //long????????? ?????? -> ?????? ??????
        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.title(getAddress(latLng.latitude,latLng.longitude).toString());
                options.snippet(addressOutput);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                Marker marker= mGoogleMap.addMarker(options);
                marker.setPosition(latLng);
                marker_list.add(marker);
                marker.showInfoWindow();

                //?????? ????????? ??? ????????? ????????? ??????, ????????? ?????? ?????????
                pOptions.add(latLng);
                //???????????? ??????
                mGoogleMap.addPolyline(pOptions);
            }
        });
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                String loc = String.format("??????:%f, ??????:%f",latLng.latitude, latLng.longitude);
                Toast.makeText(MapActivity.this, loc,Toast.LENGTH_SHORT).show();
            }
        });
    }
    /*???????????? ??????????????? ??????*/
    private void mapLoad() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);      // ???????????? this: MainActivity ??? OnMapReadyCallback ??? ???????????????
    }

    /* ?????? permission ?????? */
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQ_CODE);
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // ???????????? ??????????????? ?????? ??? ?????? ??????
                mapLoad();
                locationUpdate();
            } else {
                // ????????? ????????? ??? ???????????? ??????
                Toast.makeText(this, "??? ????????? ?????? ?????? ????????? ?????????", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //    Geocoding
    private List<String> getAddress(double latitude, double longitude) {

        List<Address> addresses = null;
        ArrayList<String> addressFragments = null;

//        ??????/????????? ???????????? ?????? ????????? Geocoder ?????? ??????
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (addresses == null || addresses.size()  == 0) {
            return null;
        } else {
            Address addressList = addresses.get(0);
            addressFragments = new ArrayList<String>();

            for(int i = 0; i <= addressList.getMaxAddressLineIndex(); i++) {
                addressFragments.add(addressList.getAddressLine(i));
            }
        }

        return addressFragments;
    }

}