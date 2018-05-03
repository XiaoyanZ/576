import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opencv.core.Point;

public class Constants {

	static final String VIDEO_FILE_EXTENSION = ".rgb";
	static final String AUDIO_FILE_EXTENSION = ".wav";
	static final String[] DB_FILE_NAMES = { "flowers", "interview", "movie", "musicvideo",
											"sports", "starcraft", "traffic"};

	static final int WIDTH = 352;
	static final int HEIGHT = 288;
	static final int MB_SIZE = 10;
	static final int P = 16;
	static final boolean motion = true;
	static final int NO_OF_FILES = 12;
	
	public static int NO_QUERY_FRAMES = 150;
	public static int NO_DB_FRAMES = 600;
	public static final int FRAME_RATE = 30;
	public static final int MAX_INT = Integer.MAX_VALUE;
	public static final int NO_OF_MOTION_VECTORS = 9;

	public static final int LK_WINDOW_SIZE = 9;
	public static final double LK_STD_DEV = 1.5;
	public static final int LK_KERNEL_SIZE = 5;
	public static final int LK_POINTS_INTERVAL = 40;
	
	static final int AUDIO_WINDOW = 1024 * 8;	//window size for STFT
	//static final int PRATE = 30;		//rate to select the peak point in coordinate of time and frequency
	static final int AUDIO_FRETARGET = 100000;	//frequency of target zone
	static final double AUDIO_TIMETARGET = 0.3;	//time of target zone
	static final int AUDIO_LMSIZE = 2;		//size of landmarks per window
	
	public static List<HashMap<Point, Point>> QUERY_VECTOR_MAP_LIST = new ArrayList<HashMap<Point, Point>>();
	
	
	static final int COLOR_WEIGHT = 3;	
	static final int MOTION_WEIGHT = 3;	
	static final int AUDIO_WEIGHT = 1;	
	
}