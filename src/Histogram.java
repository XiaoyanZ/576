import java.util.HashMap;

public class Histogram {
	private HashMap<Integer, Integer> histogram;
	public static int HSV_SUBSAMPLE_RATE = 64;
	public String position;

	public Histogram() {
		this.position = null;
		init();
	}
	
	public Histogram(String pos) {
		this.position = pos;
		init();
	}
	
	private void init() {
		this.histogram = new HashMap<Integer, Integer>();
		for(int i = 0; i < HSV_SUBSAMPLE_RATE; i ++) {
			histogram.put(i, 0);
		}
	}
	
	public void addToBinById(int id) {
		if(!histogram.containsKey(id))
			System.out.println("add to bin error!");
		int count = histogram.get(id);
		histogram.put(id, count + 1);
	}
	
	public int getContByBinId(int id) {
		if(!histogram.containsKey(id))
			System.out.println("get from bin error!");
		return histogram.get(id);
	}
	
	public void overrideBinById(int id, int count) {
		if(!histogram.containsKey(id))
			System.out.println("override bin error!");
		histogram.put(id, count);
	}
	
	public double computeDissimilarity(Histogram h) {
		double sum = 0;
		for(int i = 0; i < HSV_SUBSAMPLE_RATE; i ++) {
			double square = Math.pow((this.getContByBinId(i) - h.getContByBinId(i)), 2);
			sum += square;
		}
		return Math.sqrt(sum);
	}
}
