package com.back.global.rq;

import javax.crypto.DecapsulateException;
import java.util.HashMap;
import java.util.Map;

public class Rq {
    private final String actionName;
    private final Map<String, String> params = new HashMap<>();

    public Rq(String input){
        String[] parts = input.split("\\?", 2);
        // command 받아오기
        actionName = parts[0];

        // 파싱한 쿼리가 한개면 그대로 리턴
        if (parts.length < 2) return;

        for (String param : parts[1].split("&")) {
            // keywords = "lava" 처럼 답이 있는 경우
            String [] pair = param.split("=", 2);
            if(pair.length == 2){
                params.put(pair[0], pair[1]);
            }
        }

    }

    public String getActionName() {
        return actionName;
    }

    public String getParam(String key, String defaultValue) {
        return params.getOrDefault(key, defaultValue);
    }

    public int getParamAsInt(String key, int defaultValue) {
        try{
            return Integer.parseInt(params.get(key));
        }
        catch (NumberFormatException e){
            return defaultValue;
        }
    }

}
