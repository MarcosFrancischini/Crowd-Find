package com.example.projetosistemas.activities;

import android.app.Dialog;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetosistemas.R;
import com.example.projetosistemas.models.Marker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TelaMapaLocal extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLngBounds ARARAQUARA;
    private Marker marker;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference rootReference;

    private Dialog myDialog;

    private String[] categorias = {"Buraco", "Entulho", "Enchente", "Acessibilidade", "Outros"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_mapa_local);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        myDialog = new Dialog(this);

        // Write a message to the database
        rootReference = FirebaseDatabase.getInstance().getReference();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        float zoomLevel = 16.0f;

        ARARAQUARA = new LatLngBounds(
                new LatLng(-21.816712, -48.228790), new LatLng(-21.711537, -48.120545));

        mMap.setLatLngBoundsForCameraTarget(ARARAQUARA);

        LatLng araraquara = new LatLng(-21.774528, -48.174242);
        //mMap.addMarker(new MarkerOptions().position(araraquara).title("Marker in Araraquara"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(araraquara, zoomLevel));

        getMarkers();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                showPopUp(latLng);
            }
        });
    }

    private void showPopUp(final LatLng latLng) {
        myDialog.setContentView(R.layout.custom_pop_up);

        //final TextView labelClose = (TextView) myDialog.findViewById(R.id.labelClose);
        final Spinner spinnerCategoria = (Spinner) myDialog.findViewById(R.id.spinnerCategoria);
        final EditText fieldDescricao = (EditText) myDialog.findViewById(R.id.fieldDescricao);
        Button buttonCancelar = (Button) myDialog.findViewById(R.id.buttonCancelar);
        Button buttonSalvar = (Button) myDialog.findViewById(R.id.buttonSalvar);

        fillSpinner(spinnerCategoria);
        myDialog.show();

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marker = new Marker();
                marker.setId("1");
                marker.setUserId(currentUser.getUid());
                marker.setLatLng(latLng);
                marker.setCategoria(spinnerCategoria.getSelectedItem().toString());
                marker.setDescricao(fieldDescricao.getText().toString());
                rootReference.child("Markers").push().setValue(marker);
                myDialog.dismiss();
                getMarkers();
            }
        });

        /*
        labelClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        */

        buttonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
    }

    private void fillSpinner(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias);
        spinner.setAdapter(adapter);
    }

    private void getMarkers() {
        rootReference.addValueEventListener(new ValueEventListener() {
            List<Marker> marcadores = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot infoSnapshot : dataSnapshot.child("Markers").getChildren()) {
                    String id = (String) infoSnapshot.getKey();
                    String userId = (String) infoSnapshot.child("userId").getValue();
                    HashMap<Double, Double> latLng =  (HashMap<Double, Double>) infoSnapshot.child("latLng").getValue();
                    List<Double> variaveis = new ArrayList<>(latLng.values());
                    String categoria = (String) infoSnapshot.child("categoria").getValue();
                    String descricao = (String) infoSnapshot.child("descricao").getValue();
                    Marker marker = new Marker(id, userId, new LatLng(variaveis.get(0), variaveis.get(1)), categoria, descricao);
                    marcadores.add(marker);
                }
                generateMarkers(marcadores);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),
                        "Erro ao buscar marcadores",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateMarkers(List<Marker> markers) {
        if(markers != null && !markers.isEmpty()) {
            for(Marker marker : markers) {
                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(marker.getLatLng());

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title(marker.getLatLng().latitude + " : " + marker.getLatLng().longitude);

                // Clears the previously touched position
                //mMap.clear();

                // Animating to the touched position
                //mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);
            }
        }
    }

}
