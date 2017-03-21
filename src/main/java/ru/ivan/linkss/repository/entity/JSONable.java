package ru.ivan.linkss.repository.entity;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface JSONable {

    String toJSON() throws JsonProcessingException;

}
