package com.df.entry;

/**
 * Created by 岩 on 13-10-16.
 */
// 系列
public class Model {
    public String name;
    public String id;

    public String getName() {
        return name;
    }

    public String getNameById(String id) {
        String name = "";

        if(this.id.equals(id)) {
            name = this.name;
        }

        return name;
    }
}
