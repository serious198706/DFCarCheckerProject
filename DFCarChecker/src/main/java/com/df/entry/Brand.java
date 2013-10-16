package com.df.entry;

import java.util.ArrayList;
import java.util.List;

public class Brand {
    public String name;
    public String id;
    public List<Production> productions;
    private List<String> productionNames;

    public Brand() {
        productions = new ArrayList<Production>();
    }

    public List<String> getProductionNames() {
        productionNames = new ArrayList<String>();
        for(int i = 0; i < productions.size(); i++) {
            productionNames.add(productions.get(i).name);
        }

        return productionNames;
    }
}

