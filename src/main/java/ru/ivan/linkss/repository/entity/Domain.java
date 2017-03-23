package ru.ivan.linkss.repository.entity;


public class Domain {
    private String name;
    private long visitsActual =0;
    private long visitsHistory=0;

    public Domain(String name, long visitsActual, long visitsHistory) {
        this.name = name;
        this.visitsActual = visitsActual;
        this.visitsHistory = visitsHistory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getVisitsActual() {
        return visitsActual;
    }

    public void setVisitsActual(long visitsActual) {
        this.visitsActual = visitsActual;
    }

    public long getVisitsHistory() {
        return visitsHistory;
    }

    public void setVisitsHistory(long visitsHistory) {
        this.visitsHistory = visitsHistory;
    }

    @Override
    public String toString() {
        return "Domain{" +
                "name='" + name + '\'' +
                '}';
    }
}
