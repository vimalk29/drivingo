package com.example.drivingo.Fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drivingo.Activities.Login;
import com.example.drivingo.Activities.MainActivity;
import com.example.drivingo.Common.AlertMessage;
import com.example.drivingo.Common.Common;
import com.example.drivingo.Common.CommonValues;
import com.example.drivingo.Common.SessionManagement;
import com.example.drivingo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;


public class Profile extends Fragment implements View.OnClickListener {
    private View myFragment;
    private OnFragmentInteractionListener mListener;
    private ImageButton editProfilePic, editProfileInfo;
    private Button btnLogout;
    private ImageView profilePic;
    private TextView tvName, tvEmail, tvMobile;
    private AVLoadingIndicatorView progressbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private Context context;
    private static final int PICK_IMAGE_REQUEST=1;
    private Uri profileImgPath;

    public Profile() {
        // Required empty public constructor
    }

    public static Profile newInstance() {
        return new Profile();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myFragment = inflater.inflate(R.layout.fragment_profile, container, false);
        tvName = myFragment.findViewById(R.id.textViewUsernameSurname);
        tvEmail = myFragment.findViewById(R.id.textViewEmailProfile);
        tvMobile = myFragment.findViewById(R.id.textViewPhoneProfile);
        profilePic = myFragment.findViewById(R.id.imageViewProfile);
        editProfilePic = myFragment.findViewById(R.id.buttonEditProfilePicture);
        editProfileInfo = myFragment.findViewById(R.id.buttonEditProfileInfo);
        btnLogout = myFragment.findViewById(R.id.buttonLogout);
        progressbar = myFragment.findViewById(R.id.progressBar);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        editProfilePic.setOnClickListener(this);
        editProfileInfo.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        editProfilePic.setOnClickListener(this);
        return myFragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
    }

    private void loadData() {
        enableButtons(false);
        if(user!=null){
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri pic = user.getPhotoUrl();
            if(name==null || name.isEmpty())
                name = "Name";
            if(email==null || email.isEmpty())
                email= "Email";
            if(pic!=null)
                Picasso.with(getContext()).load(pic).into(profilePic, new Callback() {
                    @Override
                    public void onSuccess() {
                        enableButtons(true);

                    }

                    @Override
                    public void onError() {
                        enableButtons(true);
                    }
                });
            else
                enableButtons(true);

            tvName.setText(name);
            tvEmail.setText(email);
            tvMobile.setText(user.getPhoneNumber());

        }
    }

    private void enableButtons(boolean b) {
        if(b)
            progressbar.hide();
        else
            progressbar.show();
        editProfilePic.setEnabled(b);
        btnLogout.setEnabled(b);
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonEditProfilePicture:
                if(CommonValues.storageTask!=null&&CommonValues.storageTask.isInProgress())
                    Toast.makeText(context, "already in progress", Toast.LENGTH_SHORT).show();
                else
                    chooseImage();
                break;
            case R.id.buttonEditProfileInfo:
                takeUserInput();
                break;
            case R.id.buttonLogout:
                LogOut();
                break;
        }
    }

    private void LogOut() {
        if (mListener != null) {
            mListener.onFragmentInteraction(1);
        }
    }

    private void takeUserInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogBox = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_info, null);
        final EditText editTextName = dialogBox.findViewById(R.id.editProfileName);
        final EditText editTextEmail = dialogBox.findViewById(R.id.editProfileEmail);
        final EditText editTextPhone = dialogBox.findViewById(R.id.editProfileNumber);
        
        editTextName.setText(user.getDisplayName());
        editTextEmail.setText(user.getEmail());
        editTextPhone.setText(user.getPhoneNumber());
        
        builder.setTitle("Enter Info")
                .setView(dialogBox)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strname= editTextName.getText().toString().trim();
                        String strEmail = editTextEmail.getText().toString();
                        String strNumber = editTextPhone.getText().toString();
                        if( !strname.equals("")&& strname.matches("^[a-zA-Z\\s]*$")){
                            updateUserProfile(strname,user.getPhotoUrl());
                        }
                        else
                            Toast.makeText(context,"Invalid Name", Toast.LENGTH_LONG).show();
                        if((!TextUtils.isEmpty(strEmail) && Patterns.EMAIL_ADDRESS.matcher(strEmail).matches())){
                            enableButtons(false);
                            user.updateEmail(strEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadData();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if(e instanceof FirebaseAuthRecentLoginRequiredException){
                                        AlertMessage.showMessageDialog((MainActivity) context, "Session time out...Login Again to continue...", new AlertMessage.OkListener() {
                                            @Override
                                            public void onOkClicked() {
                                                LogOut();
                                            }
                                        });
                                    }
                                }
                            })
                            ;
                        }else
                            Toast.makeText(context,"Invalid Email", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        builder.show();
    }

    public void updateUserProfile(String username,Uri pic){
        enableButtons(false);
        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .setPhotoUri(pic)
                .build();
        user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadData();
            }
        });
    }
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int code);
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData()!=null){
            profileImgPath = data.getData();
            long fileSize=0;
            Uri uri  = data.getData();
            String scheme = uri.getScheme();
            if(scheme!=null&&scheme.equals(ContentResolver.SCHEME_CONTENT))
            {
                try {
                    InputStream fileInputStream=context.getContentResolver().openInputStream(uri);
                    if(fileInputStream!=null)
                    fileSize = fileInputStream.available();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if(scheme!=null&&scheme.equals(ContentResolver.SCHEME_FILE))
            {
                String path = uri.getPath();
                try {
                    File f = new File(path);
                    fileSize=f.length();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(fileSize<(1024*1024*1.5)){//1.5Mb
                try{
                    DatabaseReference connectRef = FirebaseDatabase.getInstance().getReference(".info/connected");
                    connectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Boolean connected;
                            if(dataSnapshot.exists()){
                                connected = dataSnapshot.getValue(Boolean.class);
                                if(connected!=null && connected){
                                    Picasso.with(context).load(profileImgPath).into(profilePic);
                                    uploadImage();
                                }else{
                                    Toast.makeText(context, "Check your connection", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(context, "Something went wrong..try again", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(context, "Image is too large", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage() {
        enableButtons(false);
        final StorageReference ref = FirebaseStorage.getInstance().getReference().child("profileImg/"+user.getUid()+"."+getFileExtension(profileImgPath));
        CommonValues.storageTask = ref.putFile(profileImgPath)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(context, "updated successfully", Toast.LENGTH_SHORT).show();
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    updateUserProfile(user.getDisplayName(),uri);
                                }
                            });

                        }else{
                            Toast.makeText(context, "Couldn't upload", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                ;
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }



}
