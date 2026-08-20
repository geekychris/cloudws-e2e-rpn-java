package com.geekychris.rpncalc.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SymbolicResponse(String result, String error) {

    public static SymbolicResponse success(String result) {
        return new SymbolicResponse(result, null);
    }
}
