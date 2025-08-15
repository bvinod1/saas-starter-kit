/*******************************************************************************
 *
 * Copyright (c) 2020-21 Mindsprint Limited
 *
 * All information contained herein is, and remains the property of Mindsprint
 * Limited. The intellectual and technical concepts contained herein are
 * proprietary to Mindsprint and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from Mindsprint
 * Limited
 *
 *******************************************************************************/
package com.saasstarterkit.user.dto;

public class IAMIDPUserUpdate {
    private String newemail;
    private String userid;
    
    // Getters
    public String getNewemail() { return newemail; }
    public String getUserid() { return userid; }
    
    // Setters
    public void setNewemail(String newemail) { this.newemail = newemail; }
    public void setUserid(String userid) { this.userid = userid; }
}