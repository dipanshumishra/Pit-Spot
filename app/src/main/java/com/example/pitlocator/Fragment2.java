package com.example.pitlocator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.pitlocator.databinding.ComplainLayoutBinding;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class  Fragment2 extends Fragment {

    ComplainLayoutBinding binding;
    int range ;
    String condition;
    StorageReference storageReference;
    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseUser user;
    String userKey;
    private static final String TAG = "Upload ###";
    private double currLat,currLong;

    private static  final int REQUEST_LOCATION=1;

    TextView textViewLongLat;
    LocationManager locationManager;
    String provider;
    Location location;
    ArrayList<LatLng> locationArrayList = new ArrayList<>();

    private Uri imagePath;
    String imageUrl;
    Map<String, String> config = new HashMap<>();
    String currentUser;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference ref = db.getReference().child("User's Data");
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ComplainLayoutBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userKey  = String.valueOf(user.getUid());

        locationManager =(LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria c=new Criteria();
        //if we pass false than
        //it will check first satellite location than Internet and than Sim Network
        provider=locationManager.getBestProvider(c, false);
        if ((ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            location=locationManager.getLastKnownLocation(provider);
        }
        if(location!=null) {
            double lng=location.getLongitude();
            double lat=location.getLatitude();
            currLat = lat;
            currLong = lng;
        }

        binding.submitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String str = binding.imgview.getDrawable().toString();
                if(str.equals("android.graphics.drawable.BitmapDrawable@11a41a6")){
                    Toast.makeText(getActivity(), "Select Image", Toast.LENGTH_SHORT).show();
                }
                else{
                    HashMap<String,String> map = new HashMap<>();
                    map.put("Condition",condition);
                    map.put("Range",String.valueOf(range));
                    map.put("Latitude",String.valueOf(currLat));
                    map.put("Longitude",String.valueOf(currLong));
                    ref.child(String.valueOf(userKey)).child(String.valueOf(imagePath).substring(57)).setValue(map);
                    Toast.makeText(getActivity(), "Data Added Successfully", Toast.LENGTH_SHORT).show();
                }

            }
        });

        storage = FirebaseStorage.getInstance();
        storageReference  = storage.getReference();


        requestPermission();

        //       #################       image select and upload code  ######################################

        binding.imgview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
                    selectImage();
                Log.d(TAG, ": "+"request permission");
                Log.d(TAG, ": "+"request permission");
            }
        });

        binding.rangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                binding.rangeTV.setText(""+i+ "(in metres)");
                range = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        binding.conditionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i<32){
                    binding.conditionTV.setText("Normal");
                    condition = "Normal";
                }
                else if(i>32 && i<66){
                    binding.conditionTV.setText("Moderate");
                    condition = "Moderate";
                }
                else{
                    binding.conditionTV.setText("Extreme");
                    condition = "Extreme";
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    private void requestPermission() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED)
        {

            int IMAGE_REQ = 1;
            ActivityCompat.requestPermissions(getActivity(),new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, IMAGE_REQ);
        }
//        else
//        {
//
//        }

    }

    private void selectImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        someActivityResultLauncher.launch(intent);
        binding.selectimg.setVisibility(View.INVISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        imagePath=data.getData();  // image URI
                        Picasso.get().load(imagePath).into(binding.imgview);
                        uploadPicture();
                    }
                }
            });

    private void uploadPicture() {
        final String randomKey  = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child(userKey).child(String.valueOf(imagePath).substring(57));
        riversRef.putFile(imagePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        binding.progressBar.setVisibility(View.INVISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                binding.progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity().getApplicationContext(), "Wrong Data Provided", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
