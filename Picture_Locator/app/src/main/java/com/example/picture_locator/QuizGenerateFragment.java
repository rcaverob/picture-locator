package com.example.picture_locator;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.picture_locator.Models.Quizbank;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


public class QuizGenerateFragment extends Fragment {
    private static final int REQUEST_CROPPING = 11;
    private static final int GALLERY_REQUEST_CODE = 12;
    private boolean isFabOpen = false;
    private static boolean camera_clicked;
    private FloatingActionButton fab, takeImage, locate;
    private Animation fab_open, fab_close;
    private Uri mImageUri;
    private ImageView uploadImg;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    private StorageReference storage;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private Uri downloadUrl;
    private TextView locationName;
    LocationManager locationManager;
    private double latitude,longtitude;
    public QuizGenerateFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_generate_quiz, container, false);
        ImageView quizImage = v.findViewById(R.id.quiz_image_id);
        quizImage.setClipToOutline(true);
        setHasOptionsMenu(true);

        uploadImg = v.findViewById(R.id.quiz_image_id);
        camera_clicked = false;
        checkPermission(false);

        fab = v.findViewById(R.id.fab);
        takeImage = v.findViewById(R.id.fab1);
        locate = v.findViewById(R.id.fab2);
        locationName = v.findViewById(R.id.address_textview);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        storage = FirebaseStorage.getInstance().getReference();
        databaseRef = database.getInstance().getReference().child("Quizbank");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());


        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFab();
            }
        });

        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(true);
                // pickFromLocal();
            }
        });

        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("FAB","locate clicked");
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setSpeedRequired(false);
                criteria.setCostAllowed(true);
                String provider = locationManager.getBestProvider(criteria, true);
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }


                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, locationListenerNW);
                locationManager.requestLocationUpdates(provider, 10000, 0, locationListener);
            }
        });


        return v;
    }
    private void pickFromLocal(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }
    private void takeImage(boolean start) {
        if (start) {
            //Create a new intent.
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //Create a new content values that contains the path name of image captured by camera.
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            //Insert the image uri to content provider
            mImageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            intent.putExtra("return-data", true);

            //Handle exception.
            try {
                // Start a camera capturing activity
                startActivityForResult(intent, REQUEST_CODE_IMAGE_CAPTURE);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {

            case REQUEST_CODE_IMAGE_CAPTURE:
                Glide.with(getActivity()).load(mImageUri).into(uploadImg);
                break;
            case GALLERY_REQUEST_CODE:
                mImageUri = data.getData();
                uploadImg.setImageURI(mImageUri);
                break;

        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.sync).setVisible(true);
        super.onPrepareOptionsMenu(menu);

    }


    private void animateFab(){
        if(isFabOpen){
            takeImage.startAnimation(fab_close);
            locate.startAnimation(fab_close);
            takeImage.setClickable(false);
            locate.setClickable(false);
            isFabOpen = false;
        }else{
            takeImage.startAnimation(fab_open);
            locate.startAnimation(fab_open);
            takeImage.setClickable(true);
            locate.setClickable(true);
            isFabOpen = true;
        }
    }
    //Private helper function that ask user for camera and write external storage permission.

    private void checkPermission(boolean camera_clicked) {
        //Check if permissions were granted.
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            takeImage(camera_clicked);
        }
    }
    //Callback function when user handle the check permission dialog.

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            takeImage(camera_clicked);
        }
        if (grantResults[2] == PackageManager.PERMISSION_DENIED) {
            //Returns true if the user has previously denied the request, and retruns false if
            //if a user has denied a permission and selected the Dont'ask again option.
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            } else {
                //Never ask again and handle case without permission.
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sync:
                uploadQuiz();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void uploadQuiz(){
        if(mImageUri!=null && !locationName.getText().toString().equals("Location Name")){
            final StorageReference filePath = storage.child("quiz_imgs").child(mImageUri.getLastPathSegment());
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            downloadUrl = task.getResult();
                            Log.d("FAB","Sucessfully Uploaded");
                            Log.d("FAB","Sucessfully Uploaded");

                            Toast.makeText(getActivity(), "Succesfully Uploaded", Toast.LENGTH_LONG).show();
                            final DatabaseReference newQuiz = databaseRef.push();
                            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    //Quizbank(String userName, String imageUrl, LatLng locationCoord, String addressName)
                                    com.example.picture_locator.Models.LatLng loation = new com.example.picture_locator.Models.LatLng(latitude,longtitude);

                                    newQuiz.setValue(new Quizbank("userName",downloadUrl.toString(),loation,locationName.getText().toString()));
//                                    newQuiz.child("id").setValue(System.currentTimeMillis());
//                                    newQuiz.child("imgUrl").setValue(downloadUrl.toString());
//                                    newQuiz.child("location").setValue(new LatLng(latitude,longtitude));
//                                    newQuiz.child("address").setValue(locationName.getText().toString());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                }
            });
        }
        else{
            Toast.makeText(getActivity(), "Information missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation(Location location){
        String latLongString = "N/A";
        String address = "N/A";
        Log.d("FAB","getCurrentLocation()");
        if(location!=null){
            LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            latitude = lat;
            longtitude = lng;
            address = getAddress(lat,lng);
            locationName.setText(address);
            locationName.setTextSize(22);
            Log.d("FAB","Current Location: "+address);
        }
    }

    private String getAddress(double lat, double lng){
        String address = null;
        Geocoder geocode = new Geocoder(getActivity(), Locale.getDefault());
        if(!Geocoder.isPresent()){
            address = "No geocoder available";
        }
        else{
            try {
                List<Address> addresses = geocode.getFromLocation(lat,lng,1);
                StringBuilder sb = new StringBuilder();
                if(addresses.size()>0){
                    Address ads = addresses.get(0);
                    Log.d("FAB","GETTING ADDRESS: "+ads.getMaxAddressLineIndex());
                    for (int i = 0; i <= ads.getMaxAddressLineIndex(); i++){
                        Log.d("FAB","GETTING ADDRESS");
                        sb.append(ads.getAddressLine(i));
                    }
                }
                address = sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return address;
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d("FAB","onLocationChanged()");
            locationManager.removeUpdates(locationListenerNW);
            getCurrentLocation(location);
        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    //Create a network location listener.
    private final LocationListener  locationListenerNW = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("FAB","Network listener called");
            getCurrentLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public void onPause() {
        super.onPause();
        Log.d("FAB","QuizGenerateFragment onpause");
        locationManager.removeUpdates(locationListener);
        locationManager.removeUpdates(locationListenerNW);
    }
}
