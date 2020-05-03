package com.example.pothole;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private static final float DEFAULT_ZOOM = 15f;
    private ArrayList<Loc> locations;
    private ArrayList<Loc> myLocations;

    private String username;
    private static final float MY = BitmapDescriptorFactory.HUE_MAGENTA;
    private static final float OTHERS = BitmapDescriptorFactory.HUE_ROSE;



    String whichMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        whichMap = intent.getStringExtra("which_potholes");


        myLocations = new ArrayList<>();
        locations = new ArrayList<>();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MapActivity.this);

        if(account!=null)
            username = account.getEmail();
        else{
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            username = user.getPhoneNumber();
        }
        intiMap();

    }

    private void intiMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
        getLocations();
    }

    private void getLocations() {


        FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(username).collection("potholes")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        Loc loc = new Loc(document.get("latitude"), document.get("longitude"));
                        myLocations.add(loc);
                       // myLocations.add(temp);

                        LatLng latLng = new LatLng(Double.parseDouble(document.get("latitude").toString()), Double.parseDouble(document.get("longitude").toString()));
                        addMarker(latLng, document.getId(), MY);
                    }
                }
            });


        if(whichMap.contains("all")){
           // Toast.makeText(this, "HERE", Toast.LENGTH_SHORT).show();
            db.collection("all").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for(QueryDocumentSnapshot document : task.getResult()){

                        Loc loc = new Loc(document.get("latitude"), document.get("longitude"));

                        if(!myLocations.contains(loc)) {
                         //  locations.add(temp);
                           LatLng latLng = new LatLng(Double.parseDouble(document.get("latitude").toString()), Double.parseDouble(document.get("longitude").toString()));
                           addMarker(latLng, document.getId(), OTHERS);

                       }


                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }




    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
       // Toast.makeText(this, "Map Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        FusedLocationProviderClient  mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
        try{

            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                       Location currentLocation = (Location) task.getResult();
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        moveCamera(latLng, DEFAULT_ZOOM, "ME");


                    }
                }
            });

        } catch (SecurityException e){

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    private void addMarker(LatLng latLng, String timestamp, float markerColor){

//        Float markerColor;
//        if(whichMap.equals("all")){
//            markerColor = BitmapDescriptorFactory.HUE_AZURE;
//        }else markerColor = BitmapDescriptorFactory.HUE_ROSE;

        MarkerOptions marker = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                .title(timestamp);

        mMap.addMarker(marker);
    }

    private void moveCamera(LatLng latLng, float zoom, String title){

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions marker = new MarkerOptions()
                .position(latLng)
                .title(title);


        if(!title.equals("ME"))
            mMap.addMarker(marker);



    }
}
