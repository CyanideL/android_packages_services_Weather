/*
 * Copyright (C) 2013 David van Tonder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use context file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.cyanide.weather;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.cyanide.weather.WeatherInfo.DayForecast;
import net.cyanide.weather.R;

public class ForecastBuilder {
    private static final String TAG = "ForecastBuilder";

    /**
     * This method is used to build the full current conditions and horizontal forecasts
     * panels
     *
     * @param context
     * @param w = the Weather info object that contains the forecast data
     * @return = a built view that can be displayed
     */
    @SuppressLint("SetJavaScriptEnabled")
    public static View buildFullPanel(Context context, int resourceId, WeatherInfo w) {

        // Load some basic settings
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        View view = inflater.inflate(resourceId, null);

        // Set the weather source
        TextView weatherSource = (TextView) view.findViewById(R.id.weather_source);
        weatherSource.setText(Config.getProviderId(context));

        // Set the current conditions
        // Weather Image
        ImageView weatherImage = (ImageView) view.findViewById(R.id.weather_image);
        weatherImage.setImageDrawable(w.getConditionIcon(Config.getConditionImageType(context), w.getConditionCode()));

        // Weather Condition
        TextView weatherCondition = (TextView) view.findViewById(R.id.weather_condition);
        weatherCondition.setText(w.getCondition());

        // Weather Temps
        TextView weatherTemp = (TextView) view.findViewById(R.id.weather_temp);
        weatherTemp.setText(w.getFormattedTemperature());

        // City
        TextView city = (TextView) view.findViewById(R.id.weather_city);
        city.setText(w.getCity());

        // Weather Update Time
        Date lastUpdate = w.getTimestamp();
        StringBuilder sb = new StringBuilder();
        sb.append(DateFormat.format("E", lastUpdate));
        sb.append(" ");
        sb.append(DateFormat.getTimeFormat(context).format(lastUpdate));
        TextView updateTime = (TextView) view.findViewById(R.id.update_time);
        updateTime.setText(sb.toString());

        // Weather Temps Panel additional items
        final String low = w.getFormattedLow();
        final String high = w.getFormattedHigh();
        TextView weatherLowHigh = (TextView) view.findViewById(R.id.weather_low_high);
        weatherLowHigh.setText(low + " | " + high);

        // Get things ready
        LinearLayout forecastView = (LinearLayout) view.findViewById(R.id.forecast_view);
        final View progressIndicator = view.findViewById(R.id.progress_indicator);

        // Build the forecast panel
        if (buildSmallPanel(context, forecastView, w)) {
            // Success, hide the progress container
            progressIndicator.setVisibility(View.GONE);
        }

        return view;
    }

    /**
     * This method is used to build the small, horizontal forecasts panel
     * @param context
     * @param smallPanel = a horizontal linearlayout that will contain the forecasts
     * @param w = the Weather info object that contains the forecast data
     */
    public static boolean buildSmallPanel(Context context, LinearLayout smallPanel, WeatherInfo w) {
        if (smallPanel == null) {
            Log.d(TAG, "Invalid view passed");
            return false;
        }

        // Get things ready
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 

        ArrayList<DayForecast> forecasts = w.getForecasts();
        if (forecasts == null || forecasts.size() <= 1) {
            smallPanel.setVisibility(View.GONE);
            return false;
        }

        TimeZone MyTimezone = TimeZone.getDefault();
        Calendar calendar = new GregorianCalendar(MyTimezone);

        // Iterate through the forecasts
        int forcastsCount = 5;
        if (forecasts.size() < 5) {
            forcastsCount = forecasts.size();
        }
        for (int i = 0; i < forcastsCount; i++) {
            DayForecast d = forecasts.get(i);

            // Load the views
            View forecastItem = inflater.inflate(R.layout.forecast_item, null);

            // The day of the week
            TextView day = (TextView) forecastItem.findViewById(R.id.forecast_day);
            day.setText(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
            calendar.roll(Calendar.DAY_OF_WEEK, true);

            // Weather Image
            ImageView image = (ImageView) forecastItem.findViewById(R.id.weather_image);
            image.setImageDrawable(w.getConditionIcon(Config.getConditionImageType(context), d.getConditionCode()));

            // Temperatures
            String dayLow = d.getFormattedLow();
            String dayHigh = d.getFormattedHigh();
            TextView temps = (TextView) forecastItem.findViewById(R.id.weather_temps);
            temps.setText(dayLow + " | " + dayHigh);

            // Add the view
            smallPanel.addView(forecastItem,
                  new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        }
        return true;
    }
}
