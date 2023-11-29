package org.example.util;

import java.util.HashMap;
import java.util.Map;

public class Point {
    private int id;
    private String name;
    private String obs;

    // Construtor
    public Point(int id, String name, String obs) {
        this.id = id;
        this.name = name;
        this.obs = obs;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    // Método toMap para converter um objeto Point em um mapa
    public Map<String, Object> toMap() {
        Map<String, Object> pointMap = new HashMap<>();
        pointMap.put("id", this.id);
        pointMap.put("name", this.name);
        pointMap.put("obs", this.obs);
        return pointMap;
    }
}

