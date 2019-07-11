package com.example.p09_gettingmylocations;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {

    TextView tvLag, tvLng;
    Button btnStart, btnStop, btnCheck;

    FusedLocationProviderClient client;
    LocationCallback mLocationCallback;

    String folderLocation;

    //Double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLag = findViewById(R.id.tvlag);
        tvLng = findViewById(R.id.tvlng);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnCheck = findViewById(R.id.btnCheck);

        client = LocationServices.getFusedLocationProviderClient(this);

//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult != null) {
//                    Location data = locationResult.getLastLocation();
//                    lat = data.getLatitude();
//                    lng = data.getLongitude();
//
//                    tvLag.setText("Latitude: " + lat);
//                    tvLng.setText("Longitude: " + lng);
//
//                    //Toast.makeText(MainActivity.this, "New Loc Detected \nLat: " + lat + ", Lng: " + lng, Toast.LENGTH_LONG).show();
//                }
//            }
//        };

        if (checkPermission()) {
            folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Problem9";
            File folder = new File(folderLocation);
            if (folder.exists() == false) {
                boolean result = folder.mkdir();
                if (result == true) {
                    Log.d("File Read/Write", "Folder created");
                }
            }
        } else {
            String msg = "Permission not granted to retrieve location info";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Task<Location> task = client.getLastLocation();
                    task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                tvLag.setText("Latitude: " + location.getLatitude());
                                tvLng.setText("Longitude: " + location.getLongitude());

                                Intent i = new Intent(MainActivity.this, MyService.class);
                                Toast.makeText(MainActivity.this, "Service is running", Toast.LENGTH_SHORT).show();
                                startService(i);

                            } else {
                                String msg = "No last known location found";
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    LocationRequest mLocationRequest = LocationRequest.create();
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setInterval(10000);
                    mLocationRequest.setFastestInterval(5000);
                    mLocationRequest.setSmallestDisplacement(100);
                    LocationCallback mLocationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult != null) {
                                Location data = locationResult.getLastLocation();
                                try {
                                    folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Problem9";
                                    File targetFile = new File(folderLocation, "data.txt");
                                    FileWriter writer = new FileWriter(targetFile, true);
                                    writer.write(data.getLatitude() + ", " + data.getLongitude() + "\n");
                                    writer.flush();
                                    writer.close();

                                } catch (Exception e) {
                                    Log.d("File Start Write", "Failed");
                                    Toast.makeText(MainActivity.this, "Failed to write!", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

                } else {
                    String msg = "Permission not granted to retrieve location info";
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyService.class);
                stopService(i);
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Problem9";
                File targetFile = new File(folderLocation, "data.txt");

                if (targetFile.exists() == true) {
                    String data = "";
                    try {
                        FileReader reader = new FileReader(targetFile);
                        BufferedReader br = new BufferedReader(reader);
                        String line = br.readLine();
                        while (line != null) {
                            data += line + "\n";
                            line = br.readLine();
                        }
                        br.close();
                        reader.close();
                    } catch (Exception e) {
                        Log.d("File Read", "Failed to read!");
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

//    private boolean checkPermission() {
//        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
//                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
//        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
//                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
//        int permissionCheck_Write = ContextCompat.checkSelfPermission(
//                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        int permissionCheck_Read = ContextCompat.checkSelfPermission(
//                MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
//
//
//        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
//                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED
//                || permissionCheck_Write == PermissionChecker.PERMISSION_GRANTED
//                || permissionCheck_Read == PermissionChecker.PERMISSION_GRANTED) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    private boolean checkPermission() {
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck_Write = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck_Read = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            if (permissionCheck_Write == PermissionChecker.PERMISSION_GRANTED && permissionCheck_Read == PermissionChecker.PERMISSION_GRANTED) {
                return true;

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                return false;
            }

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return false;
        }
    }
}
