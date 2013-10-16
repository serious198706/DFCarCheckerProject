package com.df.entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-10-16.
 */
// 系列
public class Serial {
    public String name;
    public String id;
    public List<Model> models;
    private List<String> modelNames;

    public Serial() {
        models = new ArrayList<Model>();
    }

    public List<String> getModelNames() {
        modelNames = new ArrayList<String>();
        for(int i = 0; i < models.size(); i++) {
            modelNames.add(models.get(i).name);
        }

        return modelNames;
    }
}