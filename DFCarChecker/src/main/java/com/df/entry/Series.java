package com.df.entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-10-16.
 */
// 系列
public class Series {
    public String name;
    public String id;
    public List<Model> models;
    private List<String> modelNames;

    public Series() {
        models = new ArrayList<Model>();
    }

    public List<String> getModelNames() {
        modelNames = new ArrayList<String>();
        modelNames.add("");
        for(int i = 0; i < models.size(); i++) {
            modelNames.add(models.get(i).name);
        }

        return modelNames;
    }

    public Model getModelById(String id) {
        Model model = null;

        for(int i = 0; i < models.size(); i++) {
            if(models.get(i).id.equals(id)) {
                model = models.get(i);
            }
        }

        return model;
    }
}