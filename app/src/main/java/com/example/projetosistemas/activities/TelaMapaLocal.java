package com.example.projetosistemas.activities;

import android.app.Dialog;
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
import com.example.projetosistemas.models.Marcador;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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


public class TelaMapaLocal extends FragmentActivity implements OnMapReadyCallback, OnMarkerClickListener {

    private final float ZOOM_LEVEL = 16.0f;

    private GoogleMap mMap;
    private LatLngBounds ARARAQUARA;
    private Marcador dadosMarcador;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference rootReference;

    private Dialog myDialog;
    private Dialog dialogMarkerInfo;

    private String[] categorias = {"Buraco", "Entulho", "Enchente", "Acessibilidade", "Outros"};

    private boolean control = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_mapa_local);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        myDialog = new Dialog(this);
        dialogMarkerInfo = new Dialog(this);

        rootReference = FirebaseDatabase.getInstance().getReference();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();
        getMarkers();

        ARARAQUARA = new LatLngBounds(new LatLng(-21.816712, -48.228790),
                new LatLng(-21.711537, -48.120545));

        mMap.setLatLngBoundsForCameraTarget(ARARAQUARA);

        LatLng araraquara = new LatLng(-21.774528, -48.174242);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(araraquara, ZOOM_LEVEL));

        mMap.setOnMarkerClickListener(this);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                showPopUp(latLng);
            }
        });
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final Marcador informacao = (Marcador) marker.getTag();
        final DatabaseReference childUpdate = rootReference.child("Markers").child(informacao.getId());
        dialogMarkerInfo.setContentView(R.layout.custom_pop_up_marker);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(informacao.getLatLng()));

        final TextView labelClose = (TextView) dialogMarkerInfo.findViewById(R.id.labelClose);
        final Spinner spinnerCategoria = (Spinner) dialogMarkerInfo.findViewById(R.id.spinnerCategoriaMarker);
        final EditText fieldDescricao = (EditText) dialogMarkerInfo.findViewById(R.id.fieldDescricaoMarker);
        Button buttonExcluir = (Button) dialogMarkerInfo.findViewById(R.id.buttonExcluirMarker);
        Button buttonSalvar = (Button) dialogMarkerInfo.findViewById(R.id.buttonSalvarMarker);

        fillSpinner(spinnerCategoria);
        spinnerCategoria.setSelection(getIndex(informacao.getCategoria()));
        fieldDescricao.setText(informacao.getDescricao());
        dialogMarkerInfo.show();

        if(!currentUser.getUid().equals(informacao.getUserId())) {
            spinnerCategoria.setEnabled(false);
            fieldDescricao.setEnabled(false);
            buttonExcluir.setEnabled(false);
            buttonSalvar.setEnabled(false);
        }

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                informacao.setCategoria(spinnerCategoria.getSelectedItem().toString());
                informacao.setDescricao(fieldDescricao.getText().toString());
                childUpdate.setValue(informacao);
                Toast.makeText(getApplicationContext(),
                        "Dados atualizados com sucesso !",
                        Toast.LENGTH_SHORT).show();
            }
        });

        buttonExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                childUpdate.removeValue();
                marker.remove();
                Toast.makeText(getApplicationContext(),
                        "Denúncia excluída com sucesso !",
                        Toast.LENGTH_SHORT).show();
                dialogMarkerInfo.dismiss();
            }
        });

        labelClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogMarkerInfo.dismiss();
            }
        });

        return true;
    }

    private void showPopUp(final LatLng latLng) {
        myDialog.setContentView(R.layout.custom_pop_up);

        final Spinner spinnerCategoria = (Spinner) myDialog.findViewById(R.id.spinnerCategoria);
        final EditText fieldDescricao = (EditText) myDialog.findViewById(R.id.fieldDescricao);
        Button buttonCancelar = (Button) myDialog.findViewById(R.id.buttonCancelar);
        Button buttonSalvar = (Button) myDialog.findViewById(R.id.buttonSalvar);

        fillSpinner(spinnerCategoria);
        myDialog.show();

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference path = rootReference.child("Markers").push();
                String key = path.getKey();

                dadosMarcador = new Marcador();
                dadosMarcador.setId(key);
                dadosMarcador.setUserId(currentUser.getUid());
                dadosMarcador.setLatLng(latLng);
                dadosMarcador.setCategoria(spinnerCategoria.getSelectedItem().toString());
                dadosMarcador.setDescricao(fieldDescricao.getText().toString());
                path.setValue(dadosMarcador);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(dadosMarcador.getLatLng()));
                placeMarker(dadosMarcador); //linha acrescentada
                Toast.makeText(getApplicationContext(),
                        "Denúncia cadastrada com sucesso !",
                        Toast.LENGTH_SHORT).show();
                myDialog.dismiss();
            }
        });

        buttonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
    }

    private void getMarkers() {
        rootReference.addValueEventListener(new ValueEventListener() {
            List<Marcador> marcadores = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot infoSnapshot : dataSnapshot.child("Markers").getChildren()) {
                    String id = (String) infoSnapshot.getKey();
                    String userId = (String) infoSnapshot.child("userId").getValue();
                    HashMap<Double, Double> latLng =  (HashMap<Double, Double>) infoSnapshot.child("latLng").getValue();
                    List<Double> variaveis = new ArrayList<>(latLng.values());
                    String categoria = (String) infoSnapshot.child("categoria").getValue();
                    String descricao = (String) infoSnapshot.child("descricao").getValue();
                    Marcador marker = new Marcador(id, userId, new LatLng(variaveis.get(0), variaveis.get(1)), categoria, descricao);
                    marcadores.add(marker);
                }

                if(control) {
                    generateMarkers(marcadores);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),
                        "Erro ao buscar marcadores",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Marcador> generateMarkers(List<Marcador> markers) {
        control = false;

        if(markers != null && !markers.isEmpty()) {
            for(Marcador marker : markers) {
                placeMarker(marker);
            }
        }
        return markers;
    }

    private void placeMarker(Marcador marker) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(marker.getLatLng());
        Marker dataMarker = mMap.addMarker(markerOptions);
        dataMarker.setTag(marker);
    }

    private void fillSpinner(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias);
        spinner.setAdapter(adapter);
    }

    private int getIndex(String categoria) {
        int pos = -1;

        if(categoria != null) {
            for(int index = 0; index < categorias.length; index++) {
                if(categorias[index].equalsIgnoreCase(categoria)) {
                    pos = index;
                }
            }

        }
        return pos;
    }
}
