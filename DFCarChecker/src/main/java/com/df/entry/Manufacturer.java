package com.df.entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-10-16.
 */
// 厂商
public class Manufacturer {
    public String name;
    public String id;
    public List<Series> serieses;
    private List<String> serialNames;

    public Manufacturer() {
        serieses = new ArrayList<Series>();
    }

    public List<String> getSerialNames() {
        serialNames = new ArrayList<String>();
        serialNames.add("");
        for(int i = 0; i < serieses.size(); i++) {
            serialNames.add(serieses.get(i).name);
        }

        return  serialNames;
    }

    public Series getSerialById(String id) {
        Series series = null;

        for(int i = 0; i < serieses.size(); i++) {
            if(serieses.get(i).id.equals(id)) {
                series = serieses.get(i);
            }
        }

        return series;
    }
}
