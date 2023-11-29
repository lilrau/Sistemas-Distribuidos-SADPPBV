package org.example.util;

public class Segment {
    private int id;
    private int startPointId;
    private int endPointId;
    private String direction;
    private int distance;
    private String obs;

    // Construtor
    public Segment(int id, int startPointId, int endPointId, String direction, int distance, String obs) {
        this.id = id;
        this.startPointId = startPointId;
        this.endPointId = endPointId;
        this.direction = direction;
        this.distance = distance;
        this.obs = obs;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStartPointId() {
        return startPointId;
    }

    public void setStartPointId(int startPointId) {
        this.startPointId = startPointId;
    }

    public int getEndPointId() {
        return endPointId;
    }

    public void setEndPointId(int endPointId) {
        this.endPointId = endPointId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }
}
