package joseph.at.androidconnection;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class MyPref {

    SharedPreferences settings;
    SharedPreferences.Editor editor;
    public static final String PREFS_NAME = "joseph";

    public MyPref(Context context){
        if(context == null){
            Log.d("devdx", "MyPref: null");
        }
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
    }



    // device mac address
    public static final String PREF_DEVICE_MAC = "PREF_DEVICE_MAC";
    public String prefDeviceMac = "";
    public String getPrefDeviceMac() { this.prefDeviceMac = settings.getString(PREF_DEVICE_MAC,"");return this.prefDeviceMac; }
    public void setPrefDeviceMac(String prefDeviceMac) { editor.putString(PREF_DEVICE_MAC,prefDeviceMac);editor.commit();}

}
