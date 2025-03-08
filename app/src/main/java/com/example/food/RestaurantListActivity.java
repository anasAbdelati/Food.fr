package com.example.food;

import android.Manifest;

import android.annotation.SuppressLint;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import adapter.PlaceAdapter;

import model.NearbyRestaurantFetcher;
import model.api.response.PlaceResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static Location coordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_restaurant_list);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check if permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Get the location
            getLastLocation();
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        callNearbyRestaurantsApi();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        Task<Location> locationResult;
        locationResult = fusedLocationClient.getLastLocation();
        locationResult.addOnSuccessListener(new OnSuccessListener<Location>() {

            @Override
            public void onSuccess(Location location) {
                // Check if the location is null
                if (location != null) {
                    coordinates = location;
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Do something with the location (e.g., display it in a Toast or update UI)
                    Toast.makeText(RestaurantListActivity.this, "Lat: " + latitude + ", Lon: " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    // Location is null, handle accordingly
                    Toast.makeText(RestaurantListActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void callNearbyRestaurantsApi() {
        NearbyRestaurantFetcher fetcher = new NearbyRestaurantFetcher();

        // Sample coordinates (latitude, longitude) and parameters
        double latitude = coordinates != null ? coordinates.getLatitude() : 43.600000;
        double longitude = coordinates != null ? coordinates.getLongitude() : 1.433333;
        double radius = 1500; // in meters
        String includedType = "restaurant";
        int maxResultCount = 10;

        fetcher.searchNearbyRestaurants(latitude, longitude, radius, includedType, maxResultCount)
                .enqueue(new Callback<PlaceResponse>() { // Explicitly use Callback<>
                    @Override
                    public void onFailure(@NonNull Call<PlaceResponse> call, @NonNull Throwable t) {
                        Log.e("API_ERROR", "Request failed: " + t.getMessage());
                    }
                    @Override
                    public void onResponse(@NonNull Call<PlaceResponse> call, @NonNull Response<PlaceResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<PlaceResponse.Place> places = response.body().getPlaces();
                            PlaceAdapter placeAdapter = new PlaceAdapter(places);
                            recyclerView.setAdapter(placeAdapter);
                        } else {
                            Log.e("API_ERROR", "Request failed with code: " + response.code());
                        }
                    }
                });
    }
}
