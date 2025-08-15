package com.saasstarterkit.user.dto;

import org.springframework.beans.factory.annotation.Value;

public class Tenant {

    @Value("${default.introspection-uri}")
    private static String introspectionuri;
    @Value("${default.client-id}")
    private static String clientid;
    @Value("${default.client-secret}")
    private static String clientsecret;

    private static String appname;
    
    public static String getAppname() {
        return appname;
    }

    public static void setAppname(String aname) {
        appname = aname;
    }

    public static String getIntrospectionuri() {
        return introspectionuri;
    }

    public static void setIntrospectionuri(String iuri) {
        introspectionuri = iuri;
    }

    public static String getClientid() {
        return clientid;
    }

    public static void setClientid(String cid) {
        clientid = cid;
    }

    public static String getClientsecret() {
        return clientsecret;
    }

    public static void setClientsecret(String csecret) {
        clientsecret = csecret;
    }

}
