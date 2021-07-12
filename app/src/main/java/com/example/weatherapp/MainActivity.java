package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.security.keystore.KeyInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private String city = "Омск";

    CurrentWeather currentWeather;

    TextView tv_city;
    TextView tv_temperature;
    TextView tv_feels_like;
    TextView tv_humidity;
    TextView tv_pressure;
    TextView tv_wind;
    TextView tv_precipitation;
    TextView tv_weather;
    ImageView iv_icon;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFERENCES.APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        currentWeather = (CurrentWeather) getIntent().getSerializableExtra(PREFERENCES.APP_PREFERENCES_WEATHER);
        System.out.println(currentWeather);

        tv_city = findViewById(R.id.current_location);
        tv_temperature = findViewById(R.id.temperature);
        tv_feels_like = findViewById(R.id.feels_like);
        tv_humidity = findViewById(R.id.humidity);
        tv_pressure = findViewById(R.id.pressure);
        tv_wind = findViewById(R.id.wind);
        tv_precipitation = findViewById(R.id.precipitation);
        tv_weather = findViewById(R.id.current_weather);
        iv_icon = findViewById(R.id.icon_weather);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.light_blue, R.color.middle_blue, R.color.deep_blue
        );

        EditText search = findViewById(R.id.search);
        search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
//                    Toast.makeText(getApplicationContext(), "УРА", Toast.LENGTH_SHORT).show();
                    editor.putString(PREFERENCES.APP_PREFERENCES_CITY, search.getText().toString().trim().toLowerCase());
                    editor.apply();
                    reloadApp();
                    return true;
                }
                return false;
            }
        });

        makeTransparentStatusAndNavigationBar();
        unloadingDataForActivity();
    }

    private void unloadingDataForActivity() {
        tv_city.setText(currentWeather.getCity());
        tv_temperature.setText(currentWeather.getTemperature() + "°C");
        tv_feels_like.setText(currentWeather.getFeelsLike() + "°");
        tv_humidity.setText(currentWeather.getHumidity() + "%");
        tv_pressure.setText(String.valueOf(currentWeather.getPressure()));
        tv_wind.setText(currentWeather.getWindSpeed() + " " + currentWeather.getWindDirection());
        tv_precipitation.setText(currentWeather.getPrecipitation());
        tv_weather.setText(firstUpperCase(currentWeather.getWeather()));
        iv_icon.setImageResource(currentWeather.getIcon());
    }

    public String firstUpperCase(String text){
        if(text == null || text.isEmpty())
            return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void makeTransparentStatusAndNavigationBar() {
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                reloadApp();
            }
        }, 1500);
    }

    private void reloadApp() {
        Activity activity = MainActivity.this;
        activity.startActivity(new Intent(getApplicationContext(), SplashScreen.class));
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        activity.finish();
    }
}