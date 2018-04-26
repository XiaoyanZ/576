import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ColorComparator {
	
	private static HashMap<String, Integer> colorMap = new HashMap<String, Integer>();
    public static int HEIGHT = 288;
    public static int WIDTH = 352;
    public static int RGB_SUBSAMPLE_RATE = 16;
	
	public ColorComparator() throws FileNotFoundException, IOException, ParseException {
		//import RGB->HSV look-up table
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader("./scripts/map.json"));
        JSONArray list = (JSONArray) obj;
        Iterator<JSONObject> iterator = list.iterator();
        while (iterator.hasNext()) {
        	JSONObject jsonObject = iterator.next();
            String rgb = (String) jsonObject.get("rgb_id").toString();
            int hsv = ((Long) jsonObject.get("hsv_id")).intValue();
            this.colorMap.put(rgb, hsv);
        }
		
	}
	
	public static double computeHistogramDissimilarity(String resultPath, String queryPath) throws IOException, ParseException {
    	
    	HashMap<String, Histogram> hm = new HashMap<String, Histogram>();
    	Histogram topleftH = new Histogram("topleft");
    	hm.put(topleftH.position, topleftH);
    	Histogram toprightH = new Histogram("topright");
    	hm.put(toprightH.position, toprightH);
    	Histogram centerH = new Histogram("center");
    	hm.put(centerH.position, centerH);
    	Histogram bottomleftH = new Histogram("bottomleft");
    	hm.put(bottomleftH.position, bottomleftH);
    	Histogram bottomrightH = new Histogram("bottomright");
    	hm.put(bottomrightH.position, bottomrightH);
    	    	
    	//read qeury file
    	File file = new File(queryPath);
		InputStream is = new FileInputStream(file);
		long len = file.length();
		byte[] bytes = new byte[(int)len];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}
		int ind = 0;
		for(int y = 0; y < HEIGHT; y++) {
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
		//read db preprocessed results
        JSONParser parser = new JSONParser();
		JSONObject sample = (JSONObject) parser.parse(new FileReader(resultPath));
		String[] posKeys = new String[] {"topleft", "topright", "center", "bottomleft", "bottomright"};
		ArrayList<Histogram> inputH = new ArrayList<Histogram>();
		for(String key: posKeys) {
			Histogram h = new Histogram(key);
			JSONArray arr = (JSONArray) sample.get(key);
			Iterator<JSONObject> ite = arr.iterator();
			while (ite.hasNext()) {
				JSONObject dummy = ite.next();
				int id = ((Long) dummy.get("bin_id")).intValue();
				int count = ((Long) dummy.get("count")).intValue();
				h.overrideBinById(id, count);
            }
			inputH.add(h);
		}
		
		//compute avg similarity over 5 positions
		double sum = 0;
		for(Histogram h: inputH) {
			Histogram queryH = hm.get(h.position);
			sum += queryH.computeDissimilarity(h);
		}
		return sum / 5;
	}
}
