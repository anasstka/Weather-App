package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class SplashScreen extends AppCompatActivity {

    private static final String APP_ID = "77d93a8a07e1b1c76cdfbf7d3f5401e1";

    private String city = "";
    private final String lang = "ru";
    private final String units = "metric";

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

    final StringBuilder sb = new StringBuilder();

    CurrentWeather currentWeather;

    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES.APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if(sharedPreferences.contains(PREFERENCES.APP_PREFERENCES_CITY)) {
            city = sharedPreferences.getString(PREFERENCES.APP_PREFERENCES_CITY, "");
        } else {
            city = "Омск";
        }

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
                                Activity activity = SplashScreen.this;

                                ThemeUtils.changeTheme(currentWeather.getIdIcon());
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra(PREFERENCES.APP_PREFERENCES_WEATHER, currentWeather);
                                activity.startActivity(intent);
                                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                activity.finish();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "ОШИБКА! Город введен некорректно!", Toast.LENGTH_SHORT).show();
                                editor.putString(PREFERENCES.APP_PREFERENCES_CITY, "омск");
                                editor.apply();
                                Activity activity = SplashScreen.this;
                                activity.startActivity(new Intent(getApplicationContext(), SplashScreen.class));
                                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                activity.finish();
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

        node = doc.getElementsByTagName("city").item(0);
        NamedNodeMap city_attributes = node.getAttributes();
        String current_city = city_attributes.getNamedItem("name").getNodeValue();

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
        int wind_speed = (int) Math.round(Double.parseDouble(
                wind_speed_attributes.getNamedItem("value").getNodeValue()));

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
                current_city,
                temperature,
                feels_like,
                humidity,
                pressure_mmHg,
                wind_speed,
                wind_direction_ru.toString(),
                precipitation_ru,
                weather,
                icon_code,
                icon
        );
    }
}