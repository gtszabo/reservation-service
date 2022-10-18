package com.example.reservation.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExceptionResponse {

    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> errors;
}
