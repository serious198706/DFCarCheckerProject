package com.df.service;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.df.entry.Brand;
import com.df.entry.Country;
import com.df.entry.Model;
import com.df.entry.Manufacturer;
import com.df.entry.Series;
import com.df.entry.VehicleModel;
/**
 * Created by admin on 13-10-15.
 */
public class VehicleModelParser {
    private String tagName;

    public VehicleModelParser() {}

    public VehicleModel parseVehicleModelXml(InputStream in) {
        VehicleModel vehicleModels = new VehicleModel();

        // 国别
        List<Country> countries = new ArrayList<Country>();
        Country country = new Country();

        // 品牌
        List<Brand> brands = new ArrayList<Brand>();
        Brand brand = new Brand();

        // 厂商
        List<Manufacturer> manufacturers = new ArrayList<Manufacturer>();
        Manufacturer manufacturer = new Manufacturer();

        // 车系
        List<Series> serieses = new ArrayList<Series>();
        Series series = new Series();

        // 型号
        List<Model> models = new ArrayList<Model>();
        Model model = new Model();

        XmlPullParser parser=Xml.newPullParser();
        try {
            parser.setInput(in, "utf-8");
            // 获取事件类型
            int eventType=parser.getEventType();

            while(eventType!=XmlPullParser.END_DOCUMENT){
                switch(eventType){
                    // 文档开始
                    case XmlPullParser.START_DOCUMENT:
                        vehicleModels = new VehicleModel();
                        break;
                    // 解析标签
                    case XmlPullParser.START_TAG:
                        tagName = parser.getName();  // "root"

                        if("root".equals(tagName)) {
                            vehicleModels.version = parser.getAttributeValue(0);
                        } else if("c".equals(tagName)){
                            // 新建一个brand list
                            brands = new ArrayList<Brand>();

                            country = new Country();
                            country.id = parser.getAttributeValue(0);
                            country.name = parser.getAttributeValue(1);
                            countries.add(country);
                        } else if("b".equals(tagName)){
                            // 新建一个production list
                            manufacturers = new ArrayList<Manufacturer>();

                            brand = new Brand();
                            brand.id = parser.getAttributeValue(0);
                            brand.name = parser.getAttributeValue(1);
                            brands.add(brand);
                        } else if("p".equals(tagName)){
                            // 新建一个serial list
                            serieses = new ArrayList<Series>();

                            manufacturer = new Manufacturer();
                            manufacturer.id = parser.getAttributeValue(0);
                            manufacturer.name = parser.getAttributeValue(1);
                            manufacturers.add(manufacturer);
                        } else if("s".equals(tagName)){
                            // 新建一个model list
                            models = new ArrayList<Model>();

                            series = new Series();
                            series.id = parser.getAttributeValue(0);
                            series.name = parser.getAttributeValue(1);
                            serieses.add(series);
                        } else if("m".equals(tagName)){
                            model = new Model();
                            model.id = parser.getAttributeValue(0);
                            model.name = parser.nextText();
                            models.add(model);
                        }

                        break;
                }

                vehicleModels.countries = countries;
                country.brands = brands;
                brand.manufacturers = manufacturers;
                manufacturer.serieses = serieses;
                series.models = models;

                eventType=parser.next();
            }

            in.close();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        return vehicleModels;
    }
}
