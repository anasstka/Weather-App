package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.LayoutTransition;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final Map<Character, String> WIND_DIRECTION = new HashMap<Character, String>() {{
        put('N', "С");
        put('E', "В");
        put('S', "Ю");
        put('W', "З");
    }};

    private static final Map<String, String> PRECIPITATION = new HashMap<String, String>() {{
        put("no", "нет");
        put("rain", "дождь");
        put("snow", "снег");
    }};

    private static final String APP_ID = "77d93a8a07e1b1c76cdfbf7d3f5401e1";

    private String city = "Омск";
    private final String lang = "ru";
    private final String units = "metric";

    final StringBuilder sb = new StringBuilder();

    CurrentWeather currentWeather;

    TextView tv_temperature;
    TextView tv_feels_like;
    TextView tv_humidity;
    TextView tv_pressure;
    TextView tv_wind;
    TextView tv_precipitation;
    TextView tv_weather;
//    TextView tv_weather_card;
    ImageView iv_icon;

    SearchView searchView;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_temperature = findViewById(R.id.temperature);
        tv_feels_like = findViewById(R.id.feels_like);
        tv_humidity = findViewById(R.id.humidity);
        tv_pressure = findViewById(R.id.pressure);
        tv_wind = findViewById(R.id.wind);
        tv_precipitation = findViewById(R.id.precipitation);
        tv_weather = findViewById(R.id.current_weather);
//        tv_weather_card = findViewById(R.id.weather);
        iv_icon = findViewById(R.id.icon_weather);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.light_blue, R.color.middle_blue, R.color.deep_blue
        );

        searchView = findViewById(R.id.search);
        searchView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
                    Toast.makeText(getApplicationContext(), "УРА", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        makeTransparentStatusAndNavigationBar();

        Async();
    }

    private void Async() {
        System.out.println("Async");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String api = "https://api.openweathermap.org/data/2.5/weather?q="+ city +"&appid=" + APP_ID +"&lang="+ lang +"&units=" + units + "&mode=xml";

                HttpURLConnection connection = null;

                System.out.println("run");

                try {
                    URL url = new URL(api);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    System.out.println(connection.getResponseCode() + "!!!!");

                    if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

                        String str;
                        // проверка строки из xml И добавление ее в StringBuilder
                        while ((str = br.readLine()) != null) {
                            sb.append(str).append("\n");
                        }

                        br.close();

                        InputSource is = new InputSource(new StringReader(sb.toString()));
                        parser(is);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                unloadingDataForActivity();
                            }
                        });
                    }
                } catch (Throwable cause) {
                    cause.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        });
    }

    private void parser(InputSource file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);

        Node node = null;

        node = doc.getElementsByTagName("temperature").item(0);
        NamedNodeMap temperature_attributes = node.getAttributes();
        int temperature = (int) Math.round(Double.parseDouble(
                temperature_attributes.getNamedItem("value").getNodeValue()));

        node = doc.getElementsByTagName("feels_like").item(0);
        NamedNodeMap feels_like_attributes = node.getAttributes();
        int feels_like = (int) Math.round(Double.parseDouble(
                feels_like_attributes.getNamedItem("value").getNodeValue()));

        node = doc.getElementsByTagName("humidity").item(0);
        NamedNodeMap humidity_attributes = node.getAttributes();
        int humidity = Integer.parseInt(
                humidity_attributes.getNamedItem("value").getNodeValue());

        node = doc.getElementsByTagName("pressure").item(0);
        NamedNodeMap pressure_attributes = node.getAttributes();
        double pressure_hPa = Double.parseDouble(
                pressure_attributes.getNamedItem("value").getNodeValue());
        pressure_hPa = pressure_hPa / 1.333;
        int pressure_mmHg = (int) Math.round(pressure_hPa);

        node = doc.getElementsByTagName("wind").item(0);
        Element el_wind = (Element) node;

        Node node_wind_speed = el_wind.getElementsByTagName("speed").item(0);
        NamedNodeMap wind_speed_attributes = node_wind_speed.getAttributes();
        int wind_speed = Integer.parseInt(
                wind_speed_attributes.getNamedItem("value").getNodeValue());

        Node node_wind_direction = el_wind.getElementsByTagName("direction").item(0);
        NamedNodeMap wind_direction_attributes = node_wind_direction.getAttributes();
        String wind_direction_en = wind_direction_attributes.getNamedItem("code").getNodeValue();
        StringBuilder wind_direction_ru = new StringBuilder();
        for (char ch : wind_direction_en.toCharArray()) {
            wind_direction_ru.append(WIND_DIRECTION.get(ch));
        }

        node = doc.getElementsByTagName("precipitation").item(0);
        NamedNodeMap precipitation_attributes = node.getAttributes();
        String precipitation_en = precipitation_attributes.getNamedItem("mode").getNodeValue();
        String precipitation_ru = PRECIPITATION.get(precipitation_en.toLowerCase());

        node = doc.getElementsByTagName("weather").item(0);
        NamedNodeMap weather_attributes = node.getAttributes();
        String weather = weather_attributes.getNamedItem("value").getNodeValue();

        String icon_code = weather_attributes.getNamedItem("icon").getNodeValue();
        int icon = getResources().getIdentifier(
                "image_" + icon_code,
                "drawable",
                getApplicationContext().getPackageName());

        System.out.println(" - " + temperature + " - " + feels_like + " - " + humidity + " - " + pressure_mmHg + " - " +wind_speed + " - " + wind_direction_ru.toString() + " - " + precipitation_ru + " - " + weather + " - " + icon + " - ");

        // запись всех данных в класс
        currentWeather = new CurrentWeather(
                temperature,
                feels_like,
                humidity,
                pressure_mmHg,
                wind_speed,
                wind_direction_ru.toString(),
                precipitation_ru,
                weather,
                icon
        );
    }

    private void unloadingDataForActivity() {
        tv_temperature.setText(currentWeather.getTemperature() + "°C");
        tv_feels_like.setText(currentWeather.getFeelsLike() + "°");
        tv_humidity.setText(currentWeather.getHumidity() + "%");
        tv_pressure.setText(String.valueOf(currentWeather.getPressure()));
        tv_wind.setText(currentWeather.getWindSpeed() + " " + currentWeather.getWindDirection());
        tv_precipitation.setText(currentWeather.getPrecipitation());
        tv_weather.setText(firstUpperCase(currentWeather.getWeather()));
//        tv_weather_card.setText(firstUpperCase(currentWeather.getWeather()));
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
                Async();
            }
        }, 3500);
    }
}