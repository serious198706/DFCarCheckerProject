package com.df.entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-10-16.
 */
// 厂商
public class Production {
    public String name;
    public String id;
    public List<Serial> serials;
    private List<String> serialNames;

    public Production() {
        serials = new ArrayList<Serial>();
    }

    public List<String> getSerialNames() {
        serialNames = new ArrayList<String>();
        serialNames.add("");
        for(int i = 0; i < serials.size(); i++) {
            serialNames.add(serials.get(i).name);
        }

        return  serialNames;
    }
}
