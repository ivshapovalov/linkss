package ru.ivan.linkss.repository.entity;


public class Domain {
    private String name;
    private String visitsActual ="0";
    private String visitsHistory="0";

    public Domain(String name, String visitsActual, String visitsHistory) {
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

    public String getVisitsActual() {
        return visitsActual;
    }

    public void setVisitsActual(String visitsActual) {
        this.visitsActual = visitsActual;
    }

    public String getVisitsHistory() {
        return visitsHistory;
    }

    public void setVisitsHistory(String visitsHistory) {
        this.visitsHistory = visitsHistory;
    }
}
