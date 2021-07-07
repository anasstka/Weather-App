package com.example.weatherapp;

import android.app.Activity;
import android.content.Intent;
import android.widget.ActionMenuView;
import android.widget.RelativeLayout;

public class ThemeUtils {

    private static String sTheme = "01d";

    public static void changeTheme(Activity activity, String idTheme) {
        sTheme = idTheme;
        activity.recreate();
//        activity.finish();
//        activity.startActivity(new Intent(activity, activity.getClass()));
//        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        switch (sTheme) {
            case "01d":
                activity.setTheme(R.style.ThemeSunny);
                break;
            case "02d": case "03d": case "04d":
                activity.setTheme(R.style.ThemeCloudy);
                break;
            case "11d": case "11n":
                activity.setTheme(R.style.ThemeThunderstorm);
                break;
            case "09d": case "10d":
                activity.setTheme(R.style.ThemeRain);
                break;
            case "01n": case "02n": case "03n": case "04n":
                activity.setTheme(R.style.ThemeNight);
                break;
            case "09n": case "10n":
                activity.setTheme(R.style.ThemeRainNight);
                break;
            case "50d": case "50n":
                activity.setTheme(R.style.ThemeFog);
                break;
        }
    }
}
