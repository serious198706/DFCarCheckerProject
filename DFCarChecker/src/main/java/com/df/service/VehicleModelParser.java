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
import com.df.entry.Production;
import com.df.entry.Serial;
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
        List<Production> productions = new ArrayList<Production>();
        Production production = new Production();

        // 车系
        List<Serial> serials = new ArrayList<Serial>();
        Serial serial = new Serial();

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
                            productions = new ArrayList<Production>();

                            brand = new Brand();
                            brand.id = parser.getAttributeValue(0);
                            brand.name = parser.getAttributeValue(1);
                            brands.add(brand);
                        } else if("p".equals(tagName)){
                            // 新建一个serial list
                            serials = new ArrayList<Serial>();

                            production = new Production();
                            production.id = parser.getAttributeValue(0);
                            production.name = parser.getAttributeValue(1);
                            productions.add(production);
                        } else if("s".equals(tagName)){
                            // 新建一个model list
                            models = new ArrayList<Model>();

                            serial = new Serial();
                            serial.id = parser.getAttributeValue(0);
                            serial.name = parser.getAttributeValue(1);
                            serials.add(serial);
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
                brand.productions = productions;
                production.serials = serials;
                serial.models = models;

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
