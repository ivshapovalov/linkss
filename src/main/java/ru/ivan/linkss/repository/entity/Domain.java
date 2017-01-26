package ru.ivan.linkss.repository.entity;


public class Domain {
    private String name;
    private String visits="0";

    public Domain(String name, String visits) {
        this.name = name;
        this.visits = visits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVisits() {
        return visits;
    }

    public void setVisits(String visits) {
        this.visits = visits;
    }
}
