package com.example.gafur.coveragequalitydetector;

/**
 * Created by gafur on 5/29/2016.
 */
public class User {
    private String login;
    private String passw;

    public User(String a,String b){
        this.login=a;
        this.passw=b;
    }

    public String getLogin(){
        return login;
    }

    public String getPassw(){
        return passw;
    }
}
