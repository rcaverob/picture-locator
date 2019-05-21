package com.example.picture_locator;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import static android.app.Activity.RESULT_OK;


public class QuizGenerateFragment extends Fragment {
    private static final int REQUEST_CROPPING = 11;
    private static final int GALLERY_REQUEST_CODE =12 ;
    private boolean isFabOpen = false;
    private static boolean camera_clicked;
    private FloatingActionButton fab,takeImage,locate;
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

    public QuizGenerateFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.activity_generate_quiz, container, false);
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

        storage = FirebaseStorage.getInstance().getReference();
        databaseRef = database.getInstance().getReference().child("Quizbank");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());


        fab_open = AnimationUtils.loadAnimation(getContext(),R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(),R.anim.fab_close);

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
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
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
        if(mImageUri!=null && !locationName.equals("Location Name")){
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
                                    newQuiz.child("imgUrl").setValue(downloadUrl.toString());
                                    newQuiz.child("id").setValue(System.currentTimeMillis());
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

}
