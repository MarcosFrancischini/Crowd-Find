package com.example.projetosistemas.models;

import android.support.annotation.Size;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class Marker {
    private String id;
    private String userId;
    private LatLng latLng;
    private String categoria;
    private String descricao;

    public Marker() {

    }

    public Marker(String id, String userId, LatLng latLng, String categoria, String descricao) {
        this.id = id;
        this.userId = userId;
        this.latLng = latLng;
        this.categoria = categoria;
        this.descricao = descricao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return String.format("ID: " + id +
                "User ID: " + userId +
                "LatLng: " + latLng +
                "Categoria: " + categoria +
                "Descrição: " + descricao);
    }
}
