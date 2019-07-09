package com.imgprocesadondk.interaction;

import com.google.gson.Gson;

public class Response<T> {

    Gson gson = new Gson();
    private String actionCode;
    private T t;


    public String castJSONObject() {
        return gson.toJson(this);
    }

    public Response castClass(String jsonObject) {
        return gson.fromJson(jsonObject, this.getClass());
    }

}
