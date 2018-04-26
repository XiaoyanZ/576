import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
import org.opencv.imgcodecs.Imgcodecs;

public class MotionMatch {

	private static final int kernelSize = Constants.LK_KERNEL_SIZE;
	private static final double stdDev = Constants.LK_STD_DEV;
	private static final int winSize = Constants.LK_WINDOW_SIZE;
	
	public static void GetMotionVectors(BufferedImage imageOld, BufferedImage imageNew, String name) {
		int align = (Constants.LK_WINDOW_SIZE - 1) / 2;

		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			//Load the image
			Mat image0 = Functions.img2Mat(imageOld);
			Mat image1 = Functions.img2Mat(imageNew);
			
			if (image0.empty() || image1.empty()) {
				System.out.println("One for the files could not be opened");
			}
			
			
			Imgcodecs.imwrite(name+".jpg",detection(image0,image1));


		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public static Mat detection(Mat lastFrame, Mat currentFrame)
    {
        if (lastFrame == null) {
            return currentFrame;
        }

        int interval = 10;
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

        Mat outputFrame = drawLine(currentFrame, prevPtsNew, nextPtsNew, status);

        return outputFrame;

    }
	
	static Mat drawLine(Mat result, MatOfPoint2f corners, MatOfPoint2f points, MatOfByte status)
    {
        Mat tmp = result.clone();
        List<Point> cornersList  = corners.toList();
        List<Point> pointsList = points.toList();
        List<Byte> statusList = status.toList();


        
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
               
                int change = Math.abs((int)(p1.x - p2.x)) + Math.abs((int)(p1.y - p2.y));
                
                if (change >= 3 && change <= 25) {
                	Imgproc.line(tmp, p1, p2, new Scalar(0, 255, 0), 1, 8, 0);
                    Imgproc.circle(tmp, p2, 2, new Scalar(0, 0, 255));
                }

            }

        }
        return tmp;
    }
	
}
