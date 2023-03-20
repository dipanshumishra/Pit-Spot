package com.example.pitlocator;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.maps.GeoApiContext;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.example.pitlocator.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.internal.connection.RouteException;

public class  MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    public String str;
    double loc_lat=0;
    double loc_long = 0;
    FirebaseAuth auth;
    FirebaseUser user;
    double startLocLat,startLocLng,endLocLat,endLocong;
    LatLng startLocation,endLocation;
    private final static int LOCATION_REQUEST_CODE = 23;
    boolean locationPermission=false;


    DatabaseReference ref;

    // below are the latitude and longitude  of 4 different locations. (MULTIPLE MARKERS)
    LatLng Gadha = new LatLng(23.164103800000,79.8743312);
    LatLng Damohnaka = new LatLng(23.1928552, 79.9257413999);
    LatLng VijayNagar = new LatLng(23.1874, 79.9051);
    LatLng Adhartal = new LatLng(23.1991, 79.9474);

    // creating array list for adding all our locations.
    private ArrayList<LatLng> locationArrayList;
    private GeoApiContext GeoApiContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestPermision();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // in below line we are initializing our array list. (MULTIPLE MARKERS ON MAP)


        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();
        if(user==null){
            Intent intent = new Intent(MapsActivity.this,Login.class);
            startActivity(intent);
            finish();
        }

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MapsActivity.this,Login.class);
                startActivity(intent);
                finish();
            }
        });

        binding.continuebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Geocoder geocoder = new Geocoder(getApplicationContext());
                List<Address> addressList1,addressList2;

                try {
                    addressList1 = geocoder.getFromLocationName(binding.startPos.getText().toString().trim(), 1);
                    addressList2 = geocoder.getFromLocationName(binding.endPos.getText().toString().trim(), 1);

                    if (addressList1 != null && addressList2 != null){
                        double doublestartLat = addressList1.get(0).getLatitude();
                        double doublestartLong = addressList1.get(0).getLongitude();
                        startLocLat = doublestartLat;
                        startLocLng = doublestartLong;
                        double doubleendLat = addressList2.get(0).getLatitude();
                        double doubleendLong = addressList2.get(0).getLongitude();
                        endLocLat = doubleendLat;
                        endLocong = doubleendLong;

                        startLocation = new LatLng(startLocLat,startLocLng);
                        endLocation = new LatLng(endLocLat,endLocong);
                        Toast.makeText(MapsActivity.this, "START: "+startLocation, Toast.LENGTH_SHORT).show();
                        Toast.makeText(MapsActivity.this, "END: "+endLocation, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    ///     MULTIPLE MARKERS ON THE MAP DISPLAYING ( AUTO ZOOM AND CENTER TO JABALPUR)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User's Data");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot datas : dataSnapshot.getChildren()) {
                        for (DataSnapshot data : datas.getChildren()) {
                            HashMap<String,String> currMap = new HashMap<>();
                            String currLat = data.child("Latitude").getValue().toString();
                            String currLng = data.child("Longitude").getValue().toString();

                            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(currLat),Double.valueOf(currLng))).title(data.child("Condition").getValue(String.class)));
                            // below line is use to move our camera to the specific location.
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.valueOf(currLat),Double.valueOf(currLng))));
                        }
                    }
                    CameraUpdate center=
                            CameraUpdateFactory.newLatLng(new LatLng(23.1929, 79.9257));
                    CameraUpdate zoom=CameraUpdateFactory.zoomTo(13);

                    mMap.moveCamera(center);
                    mMap.animateCamera(zoom);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });



    }
///     MULTIPLE MARKERS ON THE MAP DISPLAYING ( AUTO ZOOM AND CENTER TO JABALPUR) END

    public GeoPoint getLocationFromAddress(String strAddress){

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        GeoPoint p1 = null;

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new GeoPoint((double) (location.getLatitude() * 1E6),
                    (double) (location.getLongitude() * 1E6));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return p1;
    }
    // this function provides  latitude and longitude of provided string address start

    // this function provides  latitude and longitude of provided string address end

    //back button pressed
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        catch (Exception e){

        }


    }

    private void requestPermision()
    {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }
        else{
            locationPermission=true;
        }
    }

    // function to find Routes.
}





