package Fragments;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.woofmeow.ConversationActivity2;
import com.example.woofmeow.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.vanniktech.emoji.EmojiEditText;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import Consts.MessageType;

public class DelayedLocationFragment extends Fragment {


    private FusedLocationProviderClient client;
    private Geocoder geocoder;
    private LocationCallback locationCallback;
    private double longitude, latitude;
    private String gpsAddress;
    private EditText location;
    boolean address = false;
    EditText longitudeText, latitudeText;
    public interface fragmentInfoChange{
        void onTextChange(String text);
        void onDataChange(List<Object>data);
    }

    private fragmentInfoChange listener;

    public void setListener(fragmentInfoChange listener)
    {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_layout,container,false);
        longitudeText = view.findViewById(R.id.longitude);
        latitudeText = view.findViewById(R.id.latitude);
        location = view.findViewById(R.id.address);
        location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                latitudeText.setEnabled(editable.length() == 0);
                longitudeText.setEnabled(editable.length() == 0);
                address = editable.length() != 0;
            }
        });

        latitudeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                location.setEnabled(editable.length() == 0);
                address = editable.length() != 0;
            }
        });
        longitudeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                location.setEnabled(editable.length() == 0);
                address = editable.length() != 0;
            }
        });
        geocoder = new Geocoder(requireContext());
        Button find = view.findViewById(R.id.locationBtn);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!address) {
                    longitude = Double.parseDouble(String.valueOf(longitudeText.getText()));
                    latitude = Double.parseDouble(String.valueOf(latitudeText.getText()));
                    geoCoder();
                }
                else
                {

                }
            }
        });
        Button currentLocation = view.findViewById(R.id.currentLocation);
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (askPermission())
                    findLocation();
            }
        });
        return view;
    }

    private boolean askPermission()
    {
        return true;
    }

    private void findLocation()
    {

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location lastLocation = locationResult.getLastLocation();
                longitude = lastLocation.getLongitude();
                latitude = lastLocation.getLatitude();
                latitudeText.setText(String.valueOf(latitude));
                longitudeText.setText(String.valueOf(longitude));
                geoCoder();
                client.removeLocationUpdates(locationCallback);
            }
        };
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        client.requestLocationUpdates(locationRequest, locationCallback, Objects.requireNonNull(Looper.myLooper()));
    }

    private void geoCoder()
    {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = addresses.get(0);
            setAddress(address.getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setAddress(String address)
    {
        gpsAddress = address;
        location.setText(address);
    }
}
