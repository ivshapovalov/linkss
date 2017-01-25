package ru.ivan.linkss.repository;


public class Domain {
    private String name;
    private long visits;

    public Domain(String name, long visits) {
        this.name = name;
        this.visits = visits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getVisits() {
        return visits;
    }

    public void setVisits(long visits) {
        this.visits = visits;
    }
}
