package com.df.entry;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by 岩 on 13-10-16.
 */
// 国家
public class Country {
    public String name;
    public String id;
    public List<Brand> brands;
    private List<String> brandNames;

    public Country() {
        brands = new ArrayList<Brand>();
    }

    public List<String> getBrandNames() {
        brandNames = new ArrayList<String>();
        brandNames.add("");
        for(int i = 0; i < brands.size(); i++) {
            brandNames.add(brands.get(i).name);
        }

        return brandNames;
    }
}
