package com.imd;

import java.util.ArrayList;
import java.util.List;

public class Ponto {
    private double latitude, longitude, temperatura;
    private String data, hora;
    private List<Ponto> pontosProximos = new ArrayList();

    public Ponto(double latitude, double longitude, double temperatura, String data, String hora) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.temperatura = temperatura;
        this.data = data;
        this.hora = hora;
    }

    public Ponto(Ponto original) {
        this.latitude = original.latitude;
        this.longitude = original.longitude;
        this.temperatura = original.temperatura;
        this.data = original.data;
        this.hora = original.hora;
        this.pontosProximos = new ArrayList<>(original.pontosProximos);
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getTemperatura() {
        return this.temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHora() {
        return this.hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public List<Ponto> getPontosProximos() {
        return this.pontosProximos;
    }

    public void setPontosProximos(List<Ponto> pontosProximos) {
        this.pontosProximos = pontosProximos;
    }
}
