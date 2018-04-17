package com.example.soporte.rapiasistencias;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class AsistentesMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient nGoogleApiClient;
    Location nLastLocation;
    LocationRequest nLocationRequest;

    private Button mSalir;

    private String usuarioId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistentes_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSalir = (Button) findViewById(R.id.Salir);
        mSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(AsistentesMapsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        getUsuarioAsignado();
    }


    private void getUsuarioAsignado(){
        String asistenteId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usuarioAsignadoRef = FirebaseDatabase.getInstance().getReference().child("Usuarios").child("Asistentes").child(asistenteId).child("usuarioRideId");
        usuarioAsignadoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    usuarioId = dataSnapshot.getValue().toString();
                    getUsuarioAsignadoPickupLocation();
                    }
                }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUsuarioAsignadoPickupLocation(){

        DatabaseReference UsuarioAsignadoPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("usuarioSolicitud").child(usuarioId).child("1");
        UsuarioAsignadoPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0, locationLng= 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng asistenteLatLng = new LatLng(locationLat,locationLng);

                    mMap.addMarker(new MarkerOptions().position(asistenteLatLng).title("Lugar de asistencia"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }

    protected synchronized void buildGoogleApiClient(){
        nGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        nGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext() !=null){



            nLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            String asistenteId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refDisponible = FirebaseDatabase.getInstance().getReference("asistenteDisponible");
            DatabaseReference refTrabajando = FirebaseDatabase.getInstance().getReference("asistenteTrabajando");
            GeoFire geoFireDisponible = new GeoFire(refDisponible);
            GeoFire geoFireTrabajando = new GeoFire(refTrabajando);

            switch (usuarioId){
                case"":
                    geoFireTrabajando.removeLocation(usuarioId);
                    geoFireDisponible.setLocation(asistenteId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

                default:
                    geoFireDisponible.removeLocation(usuarioId);
                    geoFireTrabajando.setLocation(asistenteId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

            }
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        nLocationRequest = new LocationRequest();
        nLocationRequest.setInterval(1000);
        nLocationRequest.setFastestInterval(10000);
        nLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(nGoogleApiClient, nLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onStop() {
        super.onStop();

        String asistenteId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("asistenteDisponible");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(asistenteId);

    }

}
