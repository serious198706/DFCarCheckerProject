package com.df.entry;

import java.util.ArrayList;
import java.util.List;

public class Brand {
    public String name;
    public String id;
    public List<Manufacturer> manufacturers;
    private List<String> manufacturerNames;

    public Brand() {
        manufacturers = new ArrayList<Manufacturer>();
    }

    public List<String> getManufacturerNames() {
        manufacturerNames = new ArrayList<String>();
        manufacturerNames.add("");
        for(int i = 0; i < manufacturers.size(); i++) {
            manufacturerNames.add(manufacturers.get(i).name);
        }

        return manufacturerNames;
    }

    public Manufacturer getProductionById(String id) {
        Manufacturer manufacturer = null;

        for(int i = 0; i < manufacturers.size(); i++) {
            if(manufacturers.get(i).id.equals(id)) {
                manufacturer = manufacturers.get(i);
            }
        }

        return manufacturer;
    }
}

