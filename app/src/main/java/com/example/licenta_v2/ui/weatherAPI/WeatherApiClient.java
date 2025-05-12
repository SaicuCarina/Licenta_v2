package com.example.licenta_v2.ui.weatherAPI;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherApiClient {
    private static final String API_KEY = "a341b7bd650d4efe97b152901250205";
    private static final String BASE_URL = "https://api.weatherapi.com/v1";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public Map<String, Double> getHistoricalRainfall(String location, int numDays) {
        Map<String, Double> rainData = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < numDays; i++) {
            String date = sdf.format(calendar.getTime());
            String url = BASE_URL + "/history.json?key=" + API_KEY + "&q=" + location + "&dt=" + date;
            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e("WeatherAPI", "Error: " + response.code());
                    continue;
                }

                String responseBody = response.body().string();
                JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                double precip = json.getAsJsonObject("forecast")
                        .getAsJsonArray("forecastday")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("day")
                        .get("totalprecip_mm").getAsDouble();

                rainData.put(date, precip);
            } catch (IOException e) {
                Log.e("WeatherAPI", "Exception: " + e.getMessage());
            }

            calendar.add(Calendar.DAY_OF_YEAR, -1); // move to previous day
        }

        return rainData;
    }

    public String getEffectiveRainDate(Map<String, Double> rainData) {
        List<String> sortedDates = new ArrayList<>(rainData.keySet());
        sortedDates.sort(String::compareTo);

        for (int i = 0; i < sortedDates.size(); i++) {
            double val = rainData.get(sortedDates.get(i));
            if (val >= 20) return sortedDates.get(i);

            if (i < sortedDates.size() - 1) {
                double nextVal = rainData.get(sortedDates.get(i + 1));
                if (val > 10 && nextVal > 10) return sortedDates.get(i + 1);
            }
        }
        return null;
    }

}
