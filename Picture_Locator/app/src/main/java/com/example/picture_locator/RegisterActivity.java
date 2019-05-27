package com.example.picture_locator;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.picture_locator.Models.Quizbank;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_IMAGE_PICK = 17;
    private final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    EditText  mEmailInput, mPasswordInput, mPhoneInput,mUserNameInput;
    ImageButton registerBtn;
    private DatabaseReference mDatabase;
    private Uri mImageUri;
    private ImageView mProfileImage;
    private Bitmap rotatedBitmap;
    private StorageReference mStorageRef;
    private Uri downloadUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mEmailInput = findViewById(R.id.layout_register_email);
        mPasswordInput = findViewById(R.id.layout_register_password);
        mPhoneInput = findViewById(R.id.layout_register_phone);
        mUserNameInput = findViewById(R.id.layout_register_userName);
        registerBtn = findViewById(R.id.button_register);
        mProfileImage = findViewById(R.id.profile_img);
        mProfileImage.setClipToOutline(true);
        mStorageRef = FirebaseStorage.getInstance().getReference().child("profile_images");
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public void onButtonClick(View v){
        registerUser();
    }

    public void registerUser(){
        final String username = mUserNameInput.getText().toString();
        final String email = mEmailInput.getText().toString();
        final String password = mPasswordInput.getText().toString();
        final String phone = mPhoneInput.getText().toString();

        if(validName()&&validEmail()&&validPassword()&&validPhone()){
            mAuth.createUserWithEmailAndPassword(mEmailInput.getText().toString(), mPasswordInput.getText().toString())
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String userId = mAuth.getCurrentUser().getUid();
                                final DatabaseReference userDB = mDatabase.child(userId);

                                if(mImageUri != null){
                                    final StorageReference filePath = mStorageRef.child(mImageUri.getLastPathSegment()+userId);
                                    filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    downloadUrl = task.getResult();
                                                    userDB.child("Profile Image").setValue(downloadUrl.toString());
                                                    userDB.child("Username").setValue(username);
                                                    userDB.child("Email").setValue(email);
                                                    userDB.child("Password").setValue(password);
                                                    userDB.child("Phone").setValue(phone);
                                                    userDB.child("Highest Score").setValue(0);

                                                    mAuth.signOut();
                                                }
                                            });
                                        }
                                    });

                                }else{
                                    userDB.child("Profile Image").setValue("Default");
                                    userDB.child("Username").setValue(username);
                                    userDB.child("Email").setValue(email);
                                    userDB.child("Password").setValue(password);
                                    userDB.child("Phone").setValue(phone);
                                    userDB.child("Highest Score").setValue(0);
                                    mAuth.signOut();
                                }
                                Toast.makeText(RegisterActivity.this, "Account registered successfully",
                                        Toast.LENGTH_LONG).show();

                            } else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(getApplicationContext(), "Account already exisit", Toast.LENGTH_LONG).show();
                                }
                            }
                            finish();
                        }
                    });
        }
    }




    //Private helper function that check if user enter a valid name.
    private boolean validName() {
        String name = mUserNameInput.getText().toString().trim();
        //Throw error if user didnt enter the name.
        if (name.isEmpty()) {
            mUserNameInput.setError("Name field can't be empty.");
            return false;
        } else {
            mUserNameInput.setError(null);
        }
        return true;
    }

    //Private helper function that check if user enter a valid Email.
    private boolean validEmail() {
        String emailInput = mEmailInput.getText().toString().trim();
        if (emailInput.isEmpty()) {
            mEmailInput.setError("Email can not be empty.");
            return false;
            //Use Util.Pattern library to check if the email is valid
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            mEmailInput.setError("Please enter the valid email.");
            return false;
        } else {
            mEmailInput.setError(null);
        }
        return true;
    }

    //Private helper function that check if user enter a valid password.
    private boolean validPassword() {
        String passwordInput = mPasswordInput.getText().toString().trim();
        //Check if user enter the password or not.
        if (passwordInput.isEmpty()) {
            mPasswordInput.setError("Password can not be empty.");
            return false;
            //Check if user enter a password with length greater than 3 or not.
        } else if (passwordInput.length() < 3) {
            mPasswordInput.setError("Minimum length for the password is 3.");
            return false;
        } else {
            mPasswordInput.setError(null);
        }
        return true;
    }

    //Private helper function that check if user enter a valid phone number.
    private boolean validPhone() {
        String phone = mPhoneInput.getText().toString().trim();
        if (phone.isEmpty()) {
            mPhoneInput.setError("Phone can't be empty");
        } else if (phone.length() > 11 || phone.length() < 10) {
            mPhoneInput.setError("Not valid phone number.");
        } else {
            mPhoneInput.setError(null);
        }

        return true;
    }

    public void selectProfileImg(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,REQUEST_CODE_IMAGE_PICK);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_IMAGE_PICK:
                //Call crop image function after user finish selecting image from gallery.
                Uri selectedImageUri = data.getData();
                cropImageHelper(selectedImageUri);
                break;
            case Crop.REQUEST_CROP:
                //Call handle crop image function and save the image to profile Imageview after user finish croping the image.
                processCropImage(resultCode, data);
                break;

        }
    }

    //Private helper function that implement the cropping funcionality.
    private void cropImageHelper(Uri imageUri) {
        //Handle exception.
        try {
            //Getting the image uri.
            Uri des = Uri.fromFile(new File(getCacheDir(), "cropped"));
            //Start the cropping activity using the external Crop library.
            Crop.of(imageUri, des).asSquare().start(this);
        } catch (ActivityNotFoundException cerr) {
            cerr.printStackTrace();
        }
    }

    //Private helper function handle the image after croping.
    private void processCropImage(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            Uri imageUri = Crop.getOutput(intent);
            mImageUri = imageUri;
            Log.d("Main", "mIgameUri" + mImageUri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                mProfileImage.setImageBitmap(imageOreintationValidator(bitmap, imageUri.getPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            //Throw error message.
            Toast.makeText(this, Crop.getError(intent).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // code to handle image orientation issue -- sometimes the orientation is not right on the imageview
    // https://github.com/jdamcd/android-crop/issues/258
    private Bitmap imageOreintationValidator(Bitmap bitmap, String path) {
        ExifInterface ei;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            rotatedBitmap = null;
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);

                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);

                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);

                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;

                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotatedBitmap;
    }

    //Private helper function that handle the orientation problem.
    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}
