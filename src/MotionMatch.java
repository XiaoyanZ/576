import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

public class MotionMatch {

	private static final int kernelSize = Constants.LK_KERNEL_SIZE;
	private static final double stdDev = Constants.LK_STD_DEV;
	private static final int winSize = Constants.LK_WINDOW_SIZE;
	

	
	public static void GetMotionVectors(BufferedImage imageOld, BufferedImage imageNew, List<HashMap<Point, Point>> mapList)
    {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	        Mat lastFrame = Functions.img2Mat(imageOld);
	        Mat currentFrame = Functions.img2Mat(imageNew);
	        
	        if (lastFrame.empty() || currentFrame.empty()) {
				System.out.println("One for the files could not be opened");
			}
	       
	        int interval = Constants.LK_POINTS_INTERVAL;
	        Mat gray = new Mat();
	        Mat preGray = new Mat();
	
	        List<Point> cornersList= new ArrayList<Point>();
	        for(int i = 0; i< Constants.WIDTH ; i+=interval)
	        {
	            for(int j = 0; j < Constants.HEIGHT ; j+=interval)
	            {
	                Point p = new Point(i, j);
	                cornersList.add(p);
	            }
	        }
	        MatOfPoint prevPts = new MatOfPoint();
	        MatOfPoint nextPts = new MatOfPoint();
	        MatOfByte status = new MatOfByte();
	        MatOfFloat err = new MatOfFloat();
	        Size winSize = new Size(15,15);
	        int maxLevel = 0;
	        Imgproc.cvtColor(lastFrame, preGray, Imgproc.COLOR_RGB2GRAY);
	        Imgproc.cvtColor(currentFrame, gray, Imgproc.COLOR_RGB2GRAY);
	        prevPts.fromList(cornersList);
	        //Imgproc.goodFeaturesToTrack(preGray, prevPts, 40, 0.1 ,3);
	        //
	        //Imgproc.goodFeaturesToTrack(gray, nextPts, 40, 0.1 ,3);
	
	        MatOfPoint2f  prevPtsNew = new MatOfPoint2f( prevPts.toArray() );
	        MatOfPoint2f  nextPtsNew = new MatOfPoint2f( nextPts.toArray() );
	
	        Video.calcOpticalFlowPyrLK(preGray, gray, prevPtsNew, nextPtsNew, status, err, winSize, maxLevel);
	
	        HashMap<Point,Point> map = GetVectorMap(currentFrame, prevPtsNew, nextPtsNew, status);
	
	        mapList.add(map);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
    }
	
	
	public static double GetFrameScore(BufferedImage imageOld, BufferedImage imageNew, List<HashMap<Point, Point>> mapList)
    {
		if(mapList.size() == 0) return 0.0;
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	        Mat lastFrame = Functions.img2Mat(imageOld);
	        Mat currentFrame = Functions.img2Mat(imageNew);
	        
	        if (lastFrame.empty() || currentFrame.empty()) {
				System.out.println("One for the files could not be opened");
			}
	       
	        int interval = Constants.LK_POINTS_INTERVAL;
	        Mat gray = new Mat();
	        Mat preGray = new Mat();
	
	        List<Point> cornersList= new ArrayList<Point>();
	        for(int i = 0; i< Constants.WIDTH ; i+=interval)
	        {
	            for(int j = 0; j < Constants.HEIGHT ; j+=interval)
	            {
	                Point p = new Point(i, j);
	                cornersList.add(p);
	            }
	        }
	        MatOfPoint prevPts = new MatOfPoint();
	        MatOfPoint nextPts = new MatOfPoint();
	        MatOfByte status = new MatOfByte();
	        MatOfFloat err = new MatOfFloat();
	        Size winSize = new Size(15,15);
	        int maxLevel = 0;
	        Imgproc.cvtColor(lastFrame, preGray, Imgproc.COLOR_RGB2GRAY);
	        Imgproc.cvtColor(currentFrame, gray, Imgproc.COLOR_RGB2GRAY);
	        prevPts.fromList(cornersList);
	        //Imgproc.goodFeaturesToTrack(preGray, prevPts, 40, 0.1 ,3);
	        //
	        //Imgproc.goodFeaturesToTrack(gray, nextPts, 40, 0.1 ,3);
	
	        MatOfPoint2f  prevPtsNew = new MatOfPoint2f( prevPts.toArray() );
	        MatOfPoint2f  nextPtsNew = new MatOfPoint2f( nextPts.toArray() );
	
	        Video.calcOpticalFlowPyrLK(preGray, gray, prevPtsNew, nextPtsNew, status, err, winSize, maxLevel);
	
	        HashMap<Point,Point> dbmap = GetVectorMap(currentFrame, prevPtsNew, nextPtsNew, status);
	
	        double res = 0;
	        for(HashMap<Point,Point> map : mapList) {
	        	int count = 1;
	        	double cosineSum = 0;
	        	double distanceSum = 0;
	        	for (Point key : dbmap.keySet()) {
	        		count ++;
	        		if(map.containsKey(key)){
	        			cosineSum += cosineSimilarity(new double[]{dbmap.get(key).x - key.x, dbmap.get(key).y - key.y}, new double[]{map.get(key).x - key.x, map.get(key).y - key.y});
	        			double d = (Math.hypot(dbmap.get(key).x - key.x, dbmap.get(key).y - key.y) + Math.hypot(map.get(key).x - key.x, map.get(key).y - key.y));
	        			if (d == 0.0) distanceSum += 1;
	        			else distanceSum += (1 - Math.hypot(dbmap.get(key).x - map.get(key).x, dbmap.get(key).y - map.get(key).y) / d);
	        		} 
	        	}
	        	
	        	res = Math.max(res, (cosineSum + distanceSum) / (2 * count) );
	        }
	        return res;
	        
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			return 0.0;
		}
    }

	static HashMap<Point,Point> GetVectorMap(Mat result, MatOfPoint2f corners, MatOfPoint2f points, MatOfByte status)
    {
        Mat tmp = result.clone();
        List<Point> cornersList  = corners.toList();
        List<Point> pointsList = points.toList();
        List<Byte> statusList = status.toList();
        HashMap<Point,Point> map = new HashMap<Point,Point>();
        
        for (int i = 0; i < pointsList.size(); i++)
        {
            if (statusList.get(i) == 1)
            {
                Point p1 = new Point();
                Point p2 = new Point();
                p1.x = (int) cornersList.get(i).x;
                p1.y = (int) cornersList.get(i).y;
                
                p2.x = (int) pointsList.get(i).x;
                p2.y = (int) pointsList.get(i).y;
               
                int changeX = Math.abs((int)(p1.x - p2.x));
                int changeY = Math.abs((int)(p1.y - p2.y));
                
                if (changeX <= 8 && changeY <= 8/* && (changeX > 1 || changeY > 1)*/) {
                	Imgproc.line(tmp, p1, p2, new Scalar(0, 255, 0), 1, 8, 0);
                    Imgproc.circle(tmp, p2, 2, new Scalar(0, 0, 255));
                    map.put(p2,p1);// key : end point in current frame		value : start point in last frome
                } 

            }
        }
//        System.out.println("map size:" + map.size());
        return map;
    }
	
	public static double cosineSimilarity(double[] docVector1, double[] docVector2) {
        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;
        double cosineSimilarity = 0.0;

        for (int i = 0; i < docVector1.length; i++) //docVector1 and docVector2 must be of same length
        {
            dotProduct += docVector1[i] * docVector2[i];  //a.b
            magnitude1 += Math.pow(docVector1[i], 2);  //(a^2)
            magnitude2 += Math.pow(docVector2[i], 2); //(b^2)
        }

        magnitude1 = Math.sqrt(magnitude1);//sqrt(a^2)
        magnitude2 = Math.sqrt(magnitude2);//sqrt(b^2)

        if (magnitude1 != 0.0 | magnitude2 != 0.0) {
            cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
        } else {
            return 0.0;
        }
        return cosineSimilarity;
    }
	
	
}
