package com.example.carreservations;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class ManageCarsActivity extends AppCompatActivity {

    Spinner spinnerRegion, spinnerCity;
    TableLayout tableCars;

    String[] regionList;
    List<String> cityNames = new ArrayList<>();
    List<String> cityIDs = new ArrayList<>();
    String selectedCityID = "";

    String URL_GET_CITIES = "http://smarthostsite-002-site13.jtempurl.com/get_cities_by_region.php";
    String URL_GET_CARS = "http://smarthostsite-002-site13.jtempurl.com/get_cars_by_city.php";
    String URL_DELETE_CAR = "http://smarthostsite-002-site13.jtempurl.com/delete_car.php";
    String URL_APPROVE_CAR = "http://smarthostsite-002-site13.jtempurl.com/approve_car.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_cars);

        spinnerRegion = findViewById(R.id.spinnerRegion);
        spinnerCity = findViewById(R.id.spinnerCity);
        tableCars = findViewById(R.id.tableCars);

        regionList = getResources().getStringArray(R.array.saudi_regions);
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, regionList);
        spinnerRegion.setAdapter(regionAdapter);

        spinnerRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                fetchCities(regionAdapter.getItem(pos));
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCityID = cityIDs.get(position);
                fetchCars(selectedCityID);
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchCities(String regionName) {
        cityNames.clear();
        cityIDs.clear();

        StringRequest request = new StringRequest(Request.Method.POST, URL_GET_CITIES,
                response -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            cityNames.add(obj.getString("city_name"));
                            cityIDs.add(obj.getString("city_id"));
                        }
                        spinnerCity.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityNames));
                    } catch (Exception e) {
                        Toast.makeText(this, "City Parse Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error loading cities", Toast.LENGTH_SHORT).show()) {
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("region_name", regionName);
                return map;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void fetchCars(String cityId) {
        tableCars.removeAllViews();

        StringRequest request = new StringRequest(Request.Method.POST, URL_GET_CARS,
                response -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            TableRow row = new TableRow(this);
                            row.setPadding(8, 8, 8, 8);

                            TextView info = new TextView(this);
                            info.setText("Brand: " + obj.getString("car_brand") +
                                    "\nModel: " + obj.getString("car_model") +
                                    "\nYear: " + obj.getString("car_year") +
                                    "\nPrice/Day: " + obj.getString("price_per_day") +
                                    "\nPickup: " + obj.getString("pickup_location_details") +
                                    "\nOwner: " + obj.getString("full_name") +
                                    "\nUser Type: " + obj.getString("user_kind") +
                                    "\nStatus: " + obj.getString("approval_status"));
                            info.setPadding(12, 0, 12, 0);

                            Button btnDelete = new Button(this);
                            btnDelete.setText("Delete");
                            btnDelete.setBackgroundColor(0xFFFF4444);
                            String carId = obj.getString("car_id");
                            btnDelete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteCar(carId);
                                }
                            });

                            Button btnApprove = new Button(this);
                            btnApprove.setText("Approve");
                            btnApprove.setBackgroundColor(0xFF4CAF50);
                            btnApprove.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    approveCar(carId);
                                }
                            });

                            row.addView(info);
                            row.addView(btnDelete);
                            row.addView(btnApprove);
                            tableCars.addView(row);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing cars", Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching cars", Toast.LENGTH_SHORT).show()) {
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("city_id", cityId);
                return map;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void deleteCar(String carId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Car")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    StringRequest request = new StringRequest(Request.Method.POST, URL_DELETE_CAR,
                            response -> fetchCars(selectedCityID),
                            error -> Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()) {
                        protected Map<String, String> getParams() {
                            Map<String, String> map = new HashMap<>();
                            map.put("car_id", carId);
                            return map;
                        }
                    };
                    Volley.newRequestQueue(this).add(request);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void approveCar(String carId) {
        StringRequest request = new StringRequest(Request.Method.POST, URL_APPROVE_CAR,
                response -> fetchCars(selectedCityID),
                error -> Toast.makeText(this, "Approval failed", Toast.LENGTH_SHORT).show()) {
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("car_id", carId);
                return map;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}
