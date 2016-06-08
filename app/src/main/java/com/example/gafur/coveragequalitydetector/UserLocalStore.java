package com.example.gafur.coveragequalitydetector;

import android.content.Context;
import android.content.SharedPreferences;

public class UserLocalStore {//store user data on the phone
    public static final String SP_NAME="userDetails";
    SharedPreferences userLocalDatabase;//data on the phone

    public UserLocalStore(Context context){
        userLocalDatabase=context.getSharedPreferences(SP_NAME,0);
    }

    public void storeUserData(User user){
        SharedPreferences.Editor spEditor=userLocalDatabase.edit();
        spEditor.putString("kullanici_adi", user.getLogin());
        spEditor.putString("sifre",user.getPassw());
        spEditor.commit();
    }

    public User getLoggedInUser(){
        String username=userLocalDatabase.getString("kullanici_adi", "");
        String password=userLocalDatabase.getString("sifre","");

        return new User(username,password);
    }

    public void clearUserData(){
        SharedPreferences.Editor spEditor=userLocalDatabase.edit();
        spEditor.clear();
        spEditor.commit();
    }
}