package com.df.entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 13-10-15.
 */

public class VehicleModel {
    // 国家列表
    public List<Country> countries;
    private List<String> countryNames;
    public String version;

    public VehicleModel() {
        countries = new ArrayList<Country>();
    }

    public VehicleModel GetVehicleModelInstance() {
        return new VehicleModel();
    }

    public List<Country> GetCountries() {
        return countries;
    }

    public List<String> getCountryNames() {
        countryNames = new ArrayList<String>();

        countryNames.add("");

        for(int i = 0; i < countries.size(); i++) {
            countryNames.add(countries.get(i).name);
        }

        return countryNames;
    }

    public Country getCountryById(String id) {
        Country country = null;

        for(int i = 0; i < countries.size(); i++) {
            if(countries.get(i).id.equals(id)) {
                country = countries.get(i);
            }
        }

        return country;
    }
}


