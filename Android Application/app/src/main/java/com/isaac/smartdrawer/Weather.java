package com.isaac.smartdrawer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016/8/29.
 */
public class Weather {
    private static String getGPSLocation(Context context) {
        double latitude=0.0, longitude=0.0;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return null;
                }
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else {
                LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(String provider) {
                    }

                    public void onProviderDisabled(String provider) {
                    }
                };
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.d("Weather", latitude + "");
                    Log.d("Weather", longitude + "");
                }
            }
        }
        return "http://api.map.baidu.com/geocoder/v2/?" +
                "ak=LBNXTFqa42PkA6R5lDQHWyw7VaX9sCe8&callback=renderReverse" +
                "&location="+latitude+","+longitude+"&output=json&pois=0&mcode=4B:28:16:5C:B0:FA:52:83:32:92:0A:" +
                "3D:62:62:74:60:67:CC:F2:A0;com.example.admin.diagram1";
    }

    private static String solveWeather(String s) {
        String todayWeather = "";
        String tomorrowWeather = "";
        Pattern pattern = Pattern.compile("<h1>(.+?)（今天）.+?\"wea\">(.+?)</p>.+?<i>(.+?)</i>");
        Matcher matcher = pattern.matcher(s);
        String today = null;
        while (matcher.find()) {
            String str1 = matcher.group(1);
            String str2 = matcher.group(2);
            today = str2;
            String str3 = matcher.group(3);
            todayWeather += "\""+"今日" + "\":[\"" + str2 + "\",\"" + str3+"\"],";
        }

        Pattern pattern1 = Pattern.compile("<h1>([0-9]{1}|[0-9]{2})日（明天）.+?class=\"wea\">(.+?)</p>.+?<span>(.+?)</span>/<i>(.+?)</i>");
        Matcher matcher1 = pattern1.matcher(s);//
        while (matcher1.find()) {
            String str1 = matcher1.group(1) + "日";
            String str2 = matcher1.group(2);
            String str3 = matcher1.group(3);
            String str4 = matcher1.group(4);
            tomorrowWeather += "\""+"明日"+ "\":[\"" + str2 + "\",\"" + str4 + "~" + str3+"℃"+"\"]";
        }
//        return "{\"weather\":{"+todayWeather + tomorrowWeather+"}}";
        return today;
    }


    private static String solveAir(String s) {
        String todayAir = "";
        Pattern pattern = Pattern.compile("AQI: ([0-9]{1}|[0-9]{2}|[0-9]{3});  PM2.5: ([0-9]{1}|[0-9]{2}|[0-9]{3});  空气质量: (.?+);");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            todayAir += matcher.group(3);
        }
        return todayAir;
    }


    private static String sendGet(String url) {
        String result = "";
        BufferedReader in = null;
        try {
            URL realurl = new URL(url);
            URLConnection connection = realurl.openConnection();
            connection.connect();
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static String[] getWeatherInfo(Context context) {
        String address = "";
        String[] weather = new String[2];
        try {
            address = sendGet(getGPSLocation(context));
            address = address.substring(29, address.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObj;
        String city = null;

        try {
            jsonObj = new JSONObject(address).optJSONObject("result");
            address = jsonObj.optString("formatted_address");
            JSONObject jsonObject = jsonObj.optJSONObject("addressComponent");
            city = jsonObject.optString("city");
            Log.d("city", city);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (city == null) city = "济南市";
        String cityId = LocalCity.getCityIdByName(city.substring(0, city.length() - 1));
        Log.d("cityId", cityId);
        String sourceWeather = sendGet("http://www.weather.com.cn/weather/" +
                cityId + ".shtml");
        weather[0] = solveWeather(sourceWeather);
        String cityPinYin =  LocalCity.getCityPinYinsByName(city);
        String sourceAir = null;
        if (cityPinYin != null) {
            sourceAir = sendGet("http://www.pm25.in/" + cityPinYin);
        } else {
//            Toast.makeText(Weather.this, "未找到对应城市", Toast.LENGTH_SHORT).show();
        }
//        airRes = solveAir(sourceAir);
        weather[1] = solveAir(sourceAir);

        return weather;
    }
}
