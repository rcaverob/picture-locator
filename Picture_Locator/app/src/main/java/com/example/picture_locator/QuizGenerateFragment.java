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

import androidx.exifinterface.media.ExifInterface;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.picture_locator.Models.Quizbank;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


import static android.app.Activity.RESULT_OK;


public class QuizGenerateFragment extends Fragment {
    private final String TAG = "QuizGenerateFragment";
    private static final int GALLERY_REQUEST_CODE = 12;
    private boolean isFabOpen = false;
    private static boolean camera_clicked;
    private FloatingActionButton fab,takeImage,locate, pickFromGalleryFab;
    private Animation fab_open,fab_close;
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
    private double mLatitude, mLongitude;
    private  Bitmap imgBitmap;
    private boolean validImg;
    private final static String [] IMG_CATERGORIES = {"Building","Plant","Palace","Road",
            "Event","Leisure","Stadium","Space"};
    private ProgressBar progressBar;
    public QuizGenerateFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_generate_quiz, container, false);
        ImageView quizImage = v.findViewById(R.id.quiz_image_id);
        quizImage.setClipToOutline(true);
        setHasOptionsMenu(true);

        //Initializing different widgets.
        uploadImg = v.findViewById(R.id.quiz_image_id);
        progressBar = v.findViewById(R.id.upload_progressbar);
        validImg = false;
        camera_clicked = false;
        checkPermission(false);
        imgBitmap = null;
        fab = v.findViewById(R.id.fab);
        takeImage = v.findViewById(R.id.fab1);
        locate = v.findViewById(R.id.fab2);
        pickFromGalleryFab = v.findViewById(R.id.fab3);
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

        pickFromGalleryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkGalleryPermission();
            }
        });

        //Fetch user current location after user take a image.
        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    //Helper function that get image from gallery.
    private void pickFromLocal(){
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    //Helper function that start camera intent.
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
                //Set the image to the imageview after taking from camera
                Picasso.with(getActivity()).load(mImageUri).fit().into(uploadImg);
                try {
                    imgBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getImageCaterogry();
                break;
            case GALLERY_REQUEST_CODE:
                displayImageFromGallery(data);
                getImageCaterogry();
        }
    }

    private void displayImageFromGallery(Intent data) {
        mImageUri = data.getData();
        try {
            imgBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExifInterface ei = null;
        try {
            InputStream imageStream = Objects.requireNonNull(getActivity())
                    .getContentResolver().openInputStream(mImageUri);
            if (imageStream != null) {
                Log.d(TAG, "InputStream is not null");
                ei = new ExifInterface(imageStream);
                double arr[] = ei.getLatLong();
                if (arr != null){
                    Picasso.with(getActivity()).load(mImageUri).fit().into(uploadImg);
                    Log.d(TAG, "Latitude: "+arr[0] +" Longitude: "+arr[1]);
                    mLatitude = arr[0];
                    mLongitude = arr[1];

                    String address = getAddress(mLatitude, mLongitude);

                    locationName.setText(address);
                    locationName.setTextSize(22);

                }else {
                    Toast.makeText(getContext(), "Image must have Geolocation data",
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.sync).setVisible(true);
        super.onPrepareOptionsMenu(menu);

    }

    //Helper function that handle floating button animation.
    private void animateFab(){
        if(isFabOpen){
            takeImage.startAnimation(fab_close);
            locate.startAnimation(fab_close);
            pickFromGalleryFab.startAnimation(fab_close);
            takeImage.setClickable(false);
            locate.setClickable(false);
            pickFromGalleryFab.setClickable(false);
            isFabOpen = false;
        }else{
            takeImage.startAnimation(fab_open);
            locate.startAnimation(fab_open);
            pickFromGalleryFab.startAnimation(fab_open);
            takeImage.setClickable(true);
            locate.setClickable(true);
            pickFromGalleryFab.setClickable(true);
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

    //Private helper function that asks user for read external storage permission.
    private void checkGalleryPermission(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            pickFromLocal();
        }
    }

    //Callback function when user handle the check permission dialog.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            takeImage(camera_clicked);
        } else if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            pickFromLocal();
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

    //Helper functoin that upload quiz image and location to the firebase.
    private void uploadQuiz(){
        //Check if the image user trying to upload is spam or not.
        if(mImageUri!=null && !locationName.getText().toString().equals("Location Name")&& validImg){
            progressBar.setVisibility(View.VISIBLE);
            //Upload image file to firebase storage.
            final StorageReference filePath = storage.child("quiz_imgs").child(mImageUri.getLastPathSegment()+System.currentTimeMillis());
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Fetch the image uri that just uploaded to firebase storage and store it into the firebase realtime database.
                    filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            downloadUrl = task.getResult();
                            // Upload Quiz with dowloadUrl hash as key
                            String quiz_key = ""+downloadUrl.toString().hashCode();

                            com.example.picture_locator.Models.LatLng location = new com.example.picture_locator.Models.LatLng(mLatitude, mLongitude);
                            Quizbank newQuiz = new Quizbank(mCurrentUser.getDisplayName(), downloadUrl.toString(), location,locationName.getText().toString());
                            databaseRef.child(quiz_key).setValue(newQuiz).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), "Succesfully Uploaded", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Failed uploading quiz");
                                }
                            });
                        }
                    });
                }
            });
        }
        else if(validImg == false){
            Toast.makeText(getActivity(), "Please upload a appropriate picture", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getActivity(), "Information missing", Toast.LENGTH_LONG).show();
        }
    }

    //Helper function that fetch the current location .
    private void getCurrentLocation(Location location){
        String latLongString = "N/A";
        String address = "N/A";
        Log.d("FAB","getCurrentLocation()");
        if(location!=null){
            LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            mLatitude = lat;
            mLongitude = lng;
            address = getAddress(lat,lng);
            locationName.setText(address);
            locationName.setTextSize(22);
            Log.d("FAB","Current Location: "+address);
        }
    }

    //Helper function that get the address name from latitude and longtitude.
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
                    for (int i = 0; i <= ads.getMaxAddressLineIndex(); i++){
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

    //Create a GPS location listener
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
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


    //Helper function that use Image labeling API from ML kit to determine the image is related to location or places
    private void getImageCaterogry(){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imgBitmap);
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();
        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {

                for (FirebaseVisionImageLabel label: firebaseVisionImageLabels) {
                    String text = label.getText();
                    float confidence = label.getConfidence();
                    validImg = validCatergory(text,confidence);
                    if (validImg) break;

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("IMD","image label fails");

            }
        });
    }

    //Helper function that determine if the image is a valid catergory or not.
    private boolean validCatergory(String type,float confidence){
        for(int i =0 ; i<IMG_CATERGORIES.length;i++){
            if(type.equals(IMG_CATERGORIES[i])&&confidence >= 0.7){
                return true;
            }
        }
        return false;
    }

}

