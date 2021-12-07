package com.choqnet.budget;

import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Service
public class UtilBean {
    @Autowired
    private DataManager dataManager;

    public String giveProp(Object o, String property) {
        String methodName = "get" + property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
        Class clazz = o.getClass();
        Method method = null;
        try {
            method = clazz.getMethod(methodName, null);
            return  method.invoke(o, null).toString();
        } catch (Exception e) {
            return "???";
        }
    }

}
