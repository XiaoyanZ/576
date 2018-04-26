import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Preprocess {
    public static int QUERY_SIZE = 150;
    public static int DB_SIZE = 600;
    public static int HEIGHT = 288;
    public static int WIDTH = 352;
    public static int RGB_SUBSAMPLE_RATE = 16;
    
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		
		//import RGB->HSV look-up table
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader("./scripts/map.json"));
        JSONArray list = (JSONArray) obj;
        Iterator<JSONObject> iterator = list.iterator();
        HashMap<String, Integer> colorMap = new HashMap<String, Integer>();
        while (iterator.hasNext()) {
        	JSONObject jsonObject = iterator.next();
            String rgb = (String) jsonObject.get("rgb_id").toString();
            int hsv = ((Long) jsonObject.get("hsv_id")).intValue();
            colorMap.put(rgb, hsv);
        }
        
        //generate color histogram for each frame
        String rootPath = "query";
        String subPath = "/second";
        String fileName = subPath;
        for(int i = 1; i <= QUERY_SIZE; i ++) {
        	Histogram topleftH = new Histogram("topleft");
        	Histogram toprightH = new Histogram("topright");
        	Histogram centerH = new Histogram("center");
        	Histogram bottomleftH = new Histogram("bottomleft");
        	Histogram bottomrightH = new Histogram("bottomright");
        	Histogram[] hList = new Histogram[] {topleftH, toprightH, centerH, bottomleftH, bottomrightH};
        	
        	String fileNum = String.format ("%03d", i);
        	File file = new File( rootPath + subPath + fileName + fileNum + ".rgb");
    		InputStream is = new FileInputStream(file);
    		long len = file.length();
    		byte[] bytes = new byte[(int)len];
    		int offset = 0;
    		int numRead = 0;
    		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
    			offset += numRead;
    		}
    		int ind = 0;
    		for(int y = 0; y < HEIGHT; y++){

    			for(int x = 0; x < WIDTH; x++){
    				byte a = 0;
    				byte r = bytes[ind] ;
    				byte g = bytes[ind+HEIGHT*WIDTH];
    				byte b = bytes[ind+HEIGHT*WIDTH*2]; 
    				int R = r & 0x000000FF;
    				int G = g & 0x000000FF;
    				int B = b & 0x000000FF;
    				
    				//select Bin Counts by position
    				Histogram h;
    				if(y <= HEIGHT / 3) {
    					if(x <= WIDTH / 2)
    						h = topleftH;
    					else 
    						h = toprightH;
    				}else if(y <= HEIGHT * 2 / 3) {
    						h = centerH;
    				}else {
    					if(x <= WIDTH / 2)
    						h = bottomleftH;
    					else 
    						h = bottomrightH;
    				}
    				String key = "[" + R / RGB_SUBSAMPLE_RATE + "," + G / RGB_SUBSAMPLE_RATE + "," + B / RGB_SUBSAMPLE_RATE + "]";
    				int bin_id = colorMap.getOrDefault(key, -1);
    				if(bin_id == -1) {
    					System.out.println("get color bin id error!");
    				}
    				h.addToBinById(bin_id);
    				ind++;
    			}
    		}
    		//write to json CURRENT_FILE_NAME.json
    		JSONObject result= new JSONObject();
    		for(Histogram h: hList) {
    			JSONArray l = new JSONArray();
    			for(int id = 0; id < new Histogram().HSV_SUBSAMPLE_RATE; id++) {
    				JSONObject item = new JSONObject();
    				item.put("bin_id", id);
    				item.put("count", h.getContByBinId(id));
    				l.add(item);
    			}
    			result.put(h.position, l);
    		}
    		File f = new File("results" + subPath + fileName + fileNum + ".json");
    		f.getParentFile().mkdirs();
    		FileWriter fw = new FileWriter(f);
    		fw.write(result.toJSONString());
            fw.flush();
    		System.out.println("results" + subPath + fileName + fileNum + ".json has been created!");
        }

	}

}
