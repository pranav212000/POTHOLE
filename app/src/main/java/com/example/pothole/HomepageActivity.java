package com.example.pothole;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomepageActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 9001;
    private static final String TAG = "HomepageActivity";
    private static final float RADIUS = 5;
    private FirebaseStorage mStorage;
    private File imageFile;
    private String currentImagePath;
    private ImageView mImageView;
    private Button mUpload;
    private Button mCapture;
    private ProgressBar mProgressBar;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean isAlreadyUploaded = false;
    private Location currentLocation;
    private String username;
    private ArrayList<Loc> locations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        locations = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.ACCESS_FINE_LOCATION}, 2);

        }

        mUpload = findViewById(R.id.upload);

        fetchLocations();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(HomepageActivity.this);

        if (account != null)
            username = account.getEmail();
        else {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            username = user.getPhoneNumber();
        }


        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // Double f =(Double) locations.get(0).get("latitude");

        imageFile = null;
        mProgressBar = findViewById(R.id.progressBar);
        mStorage = FirebaseStorage.getInstance();
        mUpload = findViewById(R.id.upload);
        mImageView = findViewById(R.id.imageView);
        mCapture = findViewById(R.id.btnCapture);

        mUpload.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.all_potholes) {
            Intent intent = new Intent(HomepageActivity.this, MapActivity.class);
            intent.putExtra("which_potholes", "all");
            startActivity(intent);
        }


        if (id == R.id.my_map) {
            Intent intent = new Intent(HomepageActivity.this, MapActivity.class);
            intent.putExtra("which_potholes", "my");
            startActivity(intent);
        }


        if (id == R.id.item_signout) {


            AlertDialog.Builder builder = new AlertDialog.Builder(HomepageActivity.this);
            builder.setTitle("Alert!");
            builder.setMessage("Do you want to sign out ?");
            builder.setPositiveButton("Signout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(HomepageActivity.this);

                    if (account != null) {
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build();

                        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(HomepageActivity.this, gso);
                        mGoogleSignInClient.signOut();
                    } else {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        FirebaseAuth.getInstance().signOut();
                    }


                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.signOut();
                    Toast.makeText(HomepageActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(HomepageActivity.this, StarterActivity.class));

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        }


        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    private void startUpload() {

        if (isAlreadyUploaded) {
            Toast.makeText(this, "Image already uploaded", Toast.LENGTH_SHORT).show();
            return;
        }
        mUpload.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);


        getCurrentLocation();


    }


    private void continueUpload() {

        // if(locationsAvaliable) {
        final String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        final StorageReference storageReference = mStorage.getReference().child(username);


        double latitude = currentLocation.getLatitude();
        double longitude = currentLocation.getLongitude();


//        final Map<String, Object> pothole = new HashMap<>();
//        pothole.put("latitude", latitude);
//        pothole.put("longitude", longitude);

        String subLocality = null;
        Geocoder geocoder = new Geocoder(HomepageActivity.this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            subLocality = addresses.get(0).getSubLocality();

            Log.d(TAG, "onSuccess: locality LINE  :      " + addresses.get(0).getLocality());


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(HomepageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
//
        final Loc pothole = new Loc(latitude, longitude, subLocality );

        if (!search(pothole)) {                  // TODO id(!search(pothole))


            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            final String finalSubLocality = subLocality;
            db.collection("users").document(username).collection("potholes")
                    .document(timestamp).set(pothole).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(HomepageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    locations.add(pothole);



                    db.collection("all").document(timestamp).set(pothole).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HomepageActivity.this, "COULD NOT ADD TO ALL DATABASE TRY AGAIN", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);

                        }
                    });

                    StorageReference myref = storageReference.child(timestamp + ".jpg");
                    myref.putFile(getUriForFile(imageFile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(HomepageActivity.this, "UPLOAD SUCCESSFUL \n YOU ARE CURRENTLY IN " + finalSubLocality, Toast.LENGTH_LONG).show();

                            mProgressBar.setVisibility(View.GONE);
                            isAlreadyUploaded = true;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HomepageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(HomepageActivity.this, "COULD NOT UPLOAD TO USERS DATABASE TRY AGAIN", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.GONE);
                }
            });


        } else {
            Toast.makeText(this, "POTHOLE ALREADY PRESENT IN DATABASE", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
        }


    }

    private Uri getUriForFile(File imageFile) {
        return FileProvider.getUriForFile(this, "com.example.pothole.fileprovider", imageFile);
    }


    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {


            imageFile = getImageFile();


            if (imageFile != null) {

                currentImagePath = imageFile.getAbsolutePath();
                //Uri imageUri = FileProvider.getUriForFile(HomepageActivity.this, "com.example.pothole.fileprovider", imageFile);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(imageFile));
                //Toast.makeText(this, "Starting activity", Toast.LENGTH_SHORT).show();

                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);


            } else {
                Toast.makeText(this, "image file null", Toast.LENGTH_SHORT).show();
            }


        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);
            mImageView.setImageBitmap(bitmap);
            mUpload.setVisibility(View.VISIBLE);
            isAlreadyUploaded = false;
        }


    }

    private File getImageFile() {

        String name = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
        //File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(name, ".jpg", storageDir);
        } catch (IOException e) {
            Log.d("My log", "createphotofile: " + e.toString());
        }

        //Toast.makeText(this, "Returning file", Toast.LENGTH_SHORT).show();
        return image;

    }


    private void getCurrentLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomepageActivity.this);
        try {

            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: LOcation acquired successfully");
                        currentLocation = (Location) task.getResult();
                        currentLocation.getAccuracy();


                        continueUpload();
                    } else {
                        Toast.makeText(HomepageActivity.this, "Couldn't find location", Toast.LENGTH_SHORT).show();
                       // Toast.makeText(HomepageActivity.this, "Unable to find location", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (SecurityException e) {

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private int comparator(Map<String, Object> obj1, Map<String, Object> obj2) {
        Double lat1 = (Double) obj1.get("latitude");
        Double lat2 = (Double) obj2.get("latitude");
        Double long1 = (Double) obj1.get("longitude");
        Double long2 = (Double) obj2.get("longitude");
        if (lat1 > lat2) {
            return 1;
        } else if (lat1 < lat2) {
            return -1;
        } else {
            if (long1 > long2) {
                return 1;
            } else if (long1 < long2) {
                return -1;
            } else {
                return 0;
            }
        }

    }


    private boolean search(Loc loc) {

        for (int i = 0; i < locations.size(); i++) {
            Location location = new Location("");
            location.setLatitude((Double) locations.get(i).getLatitude());
            location.setLongitude((Double) locations.get(i).getLongitude());

            Float distance = currentLocation.distanceTo(location);
            if (distance < 5) {
            //    Toast.makeText(this, location.toString(), Toast.LENGTH_SHORT).show();
            //    Toast.makeText(this, distance.toString(), Toast.LENGTH_LONG).show();
                return true;
            }


        }

        return false;


    }


    public void fetchLocations() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("all")
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Loc temp = new Loc(document.get("latitude"), document.get("longitude"));
                            locations.add(temp);
                        //    Toast.makeText(HomepageActivity.this, "ADDED", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: ADDED");
                        }

                        Toast.makeText(HomepageActivity.this, "LOCATIONS AVAILABLE", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete: LOCATIONS AVAILABLE");
                        // locationsAvaliable = true;
                        Log.d(TAG, "onComplete: SIZE : " + locations.size());

                        locations.sort(new Comparator<Loc>() {
                            @Override
                            public int compare(Loc l1, Loc l2) {
                                Double lat1 = (Double) l1.latitude;
                                Double lat2 = (Double) l2.latitude;
                                Double long1 = (Double) l1.longitude;
                                Double long2 = (Double) l2.longitude;


                                if (lat1 > lat2) {
                                    return 1;
                                } else if (lat1 < lat2) {
                                    return -1;
                                } else {
                                    return long1.compareTo(long2);
                                }
                            }
                        });

                        for (int i = 0; i < locations.size(); i++) {
                            Log.d(TAG, "onComplete: " + i + " : " + locations.get(i).toString());
                        }

                        mUpload.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startUpload();
                            }
                        });
                        Log.d(TAG, "onComplete: UPLOAD LISTENER ADDED");


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomepageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}








