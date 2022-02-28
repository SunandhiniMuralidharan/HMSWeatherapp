package com.example.weatherhms;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.banner.BannerView;


/**
 * @author Sunandhini Muralidharan
 * @version 1.0
 * @since 27.02.2022
 *
 * WeatherActivity is an controller class. It does the following:
 * 1. Detects Location permissions and requests the user for the same
 * 2. Gets the City name from current location
 * 3. Gets the Current Weather and Weather forecast
 * 4. Signs out of the Huawei ID using HMS Account Kit
 * 5. Displays Banner Ads at the bottom of the Screen using HMS Ads Kit
 */

public class WeatherActivity extends AppCompatActivity {

    /*Necessary Member Variables and UI Member Variables*/
    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV,temperatureView,conditionView;
    private TextInputEditText cityEdit;
    private ImageView viewBG,searchIcon,iconIV;
    private RecyclerView weather;
    private ArrayList<WeatherRVModel> weatherArray;
    private WeatherRVAdapter weatherAdapter;
    private LocationManager locationManager;
    private final int PERMISSION_CODE = 1;
    private String cityName;
    private static final String TAG = "Account";

    // AccountAuthService provides a set of APIs, including silentSignIn, getSignInIntent, and signOut.
    private AccountAuthService mAuthService;

    // Set HUAWEI ID sign-in authorization parameters.
    private AccountAuthParams mAuthParam;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_weather);

        // Initialize the HUAWEI Ads SDK.
        HwAds.init(this);

        // Obtain BannerView configured in the XML layout file and display at the bottom
        BannerView bottomBannerView = findViewById(R.id.hw_banner_view);
        AdParam adParam = new AdParam.Builder().build();
        bottomBannerView.loadAd(adParam);

        //Initializing all UI Member Variables
        homeRL = findViewById(R.id.idHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureView = findViewById(R.id.idTVTempView);
        conditionView = findViewById(R.id.idTVCondition);
        cityEdit = findViewById(R.id.idTETCity);
        viewBG = findViewById(R.id.idBGView);
        searchIcon = findViewById(R.id.idIVSearchIcon);
        iconIV = findViewById(R.id.idIVContent);
        weather = findViewById(R.id.idRVWeather);

        //Initializing member variables
        weatherArray = new ArrayList<>();
        weatherAdapter = new WeatherRVAdapter(this,weatherArray);

        //Setting the Adapter
        weather.setAdapter(weatherAdapter);

        mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setEmail()
                .createParams();

        // Use AccountAuthParams to build AccountAuthService.
        mAuthService = AccountAuthManager.getService(this, mAuthParam);

        //On Click Listener for Signout button
        findViewById(R.id.HuaweiIdSignOutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        //Fetching LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Checking for permissions
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(WeatherActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }

        //Fetching Current location of the user
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        //Getting city name using latitude and longitude
        cityName = getCityName(location.getLatitude(),location.getLongitude());

        //Getting the Current weather and weather forecast
        getWeatherForecast(cityName);

        //On Click Listener for Search Icon
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdit.getText().toString();
                if(city.isEmpty()) {
                    Toast.makeText(WeatherActivity.this, "Please Enter City Name", Toast.LENGTH_SHORT).show();
                }
                else{
                    cityNameTV.setText(city);
                    getWeatherForecast(city);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions Granted!!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Please provide necessary permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * This method gets the name of the city using latitude and longitude
     * @Param latitude
     * @Param longitude
     * @return String
     */
    private String getCityName(double latitude, double longitude){

       String cityName = "Not Found!";

       //Geocoder for getting city name from Latitude and Longitude
        Geocoder gcd = new Geocoder(this, Locale.getDefault());

        try{
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,1);

            Address adr = addresses.get(0);
            if(adr == null){
                Toast.makeText(this, "address is null", Toast.LENGTH_SHORT).show();
            }

            if(adr!=null){

                String city = adr.getSubAdminArea();

                if(city != null && !city.equals("")){

                    cityName = city;
                }
                else{
                    Log.d("TAG","City Not Found");
                    Toast.makeText(this,"City Not Found!!",Toast.LENGTH_SHORT).show();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    /**
     * This method calls the weatherapi by city name and onResponseSuccess bind the data
     * with associated model and set the data to show to the user. It shows Current Weather and
     * Weather Forecast
     * @Param cityName
     */

    private void getWeatherForecast(String cityName){

        String url = "http://api.weatherapi.com/v1/forecast.json?key=ce4d55624da4492a95f234728222202&q="+cityName+"&days=5&aqi=no&alerts=no";

        //Setting the City Name in the layout
        cityNameTV.setText(cityName);

        RequestQueue requestQueue = Volley.newRequestQueue(WeatherActivity.this);

        //Getting the JSON Object from the weatherapi
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherArray.clear();

                try {

                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureView.setText(temperature+"°c");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("https:".concat(conditionIcon)).into(iconIV);
                    conditionView.setText(condition);
                    if(isDay == 1){
                        Picasso.get().load("https://images.unsplash.com/photo-1508614999368-9260051292e5?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2070&q=80").into(viewBG);
                    }else{
                        Picasso.get().load("https://images.unsplash.com/photo-1530508777238-14544088c3ed?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1887&q=80").into(viewBG);
                    }

                    JSONObject forecast = response.getJSONObject("forecast");
                    JSONArray forecastArray = forecast.getJSONArray("forecastday");

                    for(int i = 0;i<forecastArray.length();i++){

                        JSONObject forecast0 = forecast.getJSONArray("forecastday").getJSONObject(i);
                        String date = forecast0.getString("date");
                        String temp = forecast0.getJSONObject("day").getString("avgtemp_c");
                        String img = forecast0.getJSONObject("day").getJSONObject("condition").getString("icon");
                        String text = forecast0.getJSONObject("day").getJSONObject("condition").getString("text");

                        weatherArray.add(new WeatherRVModel(date,temp,img,text));
                    }

                    weatherAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(WeatherActivity.this, "Please Enter a Valid City Name "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * This method signs out from the Huawei ID using HMS Account Kit
     */
    private void signOut() {
        Task<Void> signOutTask = mAuthService.signOut();
        signOutTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "signOut Success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "signOut fail");
            }
        });

        //Once Sign out is successful, the Login page (Sign in Page - MainActivity) should be displayed
        loginPage();
    }

    /**
     * This method makes a call to MainAcitivity once the sign out is success
     */
    private void loginPage() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(WeatherActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }

}
