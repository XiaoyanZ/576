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
	private ArrayList<HashMap<String, Histogram>> queryList = new ArrayList<HashMap<String, Histogram>>();
	private String resultBase = "";
	private int QUERY_SIZE = 150;
	private int DB_SIZE = 600;
	private int SUB_SAMPLE_RATE = 10;
	private int LAST_150_SUBSAMPLE_RATE = 5;
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
	
	public void readResult(String resultFolder) {
		this.resultBase = "results/" + resultFolder + "/" + resultFolder;
	}
	
	public void readQuery(String queryFolder) throws IOException {
		String queryPathBase = "query/"+queryFolder+"/" + queryFolder;
		for(int j = 0; j < this.QUERY_SIZE; j++) {
			String queryFileNum = String.format("%03d", j + 1);
			String queryPath = queryPathBase + queryFileNum + ".rgb";
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
					//select histogram by position
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
			queryList.add(hm);
		}
	}
	
	public double computeHistogramDissimilarity(int resultId, int queryId) throws IOException, ParseException {
		//read db preprocessed results
        JSONParser parser = new JSONParser();
		JSONObject sample = (JSONObject) parser.parse(new FileReader(this.resultBase + String.format ("%03d", resultId) + ".json"));
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
			Histogram queryH = this.queryList.get(queryId - 1).get(h.position);
			sum += queryH.computeDissimilarity(h);
		}
		return sum / 5;
	}
	
	public ArrayList<Double> computeDissimilarityByResultFolder(String folder) throws IOException, ParseException {
		ArrayList<Double> result = new ArrayList<Double>();
		this.readResult(folder);
		//spare last 150 frames
		for(int i = 0; i < DB_SIZE - QUERY_SIZE + 1; i ++) {
			double sum = 0;
			int j = 0;
			for(; j * SUB_SAMPLE_RATE < QUERY_SIZE; j++) {
				int index = j * SUB_SAMPLE_RATE;
				System.out.println("db frame " + (i + index +1) + " compared with query frame " + (index + 1)); 
				double score = computeHistogramDissimilarity(i + index + 1, index + 1);
				sum += score;
			}
			result.add(sum / j);
		}
		System.out.println();
		//last 150 frames
		for(int i = DB_SIZE - QUERY_SIZE + 2; i < DB_SIZE -  + 1; i ++) {
			int len = DB_SIZE - i + 1;
			double dummy = 0;
			int startBase = 0;
			for(int start = 1; start + startBase * LAST_150_SUBSAMPLE_RATE + len - 1 <= QUERY_SIZE; startBase++) {
				double sum = 0;
				int j = 0;
				for(int index = start + startBase * LAST_150_SUBSAMPLE_RATE; (i + j * SUB_SAMPLE_RATE <= DB_SIZE) && (index + j * SUB_SAMPLE_RATE <= QUERY_SIZE); j++) {
					double score = computeHistogramDissimilarity(i + j * SUB_SAMPLE_RATE, index + j * SUB_SAMPLE_RATE);
					sum += score;
					System.out.println("[db frame " + i + "] compared with query from " + index + " on stage [db " + (i + j * SUB_SAMPLE_RATE) + 
							"] - [query " + (index + j * SUB_SAMPLE_RATE) + "]"  );
					
				}
				dummy += sum / j;
			}
			dummy = dummy / startBase;
			result.add(dummy);
		}
		return result;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		ColorComparator cc = new ColorComparator();
		
		cc.readQuery("first");
		ArrayList<Double> f;
		String[] resultFolders = new String[] {"musicvideo", "sports", "flowers", "interview", "movie", "starcraft", "traffic"};
		for(String str: resultFolders) {
			f = cc.computeDissimilarityByResultFolder(str);
		}
	
	}
}
