import java.awt.image.*;
import java.io.*;
import java.util.*;

public class VideoQuery {
  static final int WIDTH = 352;
  static final int HEIGHT = 288;
  static private PlaySound playSound;
  static private String fileFolder = System.getProperty("user.dir") + "/query";
  
  public static void main(String[] args) {
    ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
    
    //get video name
	String filename = args[0];

    try {
//    	Constants.QUERY_VECTOR_MAP_LIST = new ArrayList<HashMap<Point, Point>>();
    	for(int i=1; i<=150; i++) {
	    	  String fileNum = "00";
	    	  if(i < 100 && i > 9) {
	    		  fileNum = "0";
	    	  } else if(i > 99) {
	    		  fileNum = "";
	    	  }
	    	  String fullName = fileFolder + "/" + filename + "/" + filename + fileNum + new Integer(i).toString() + ".rgb";
	    	  String audioFilename = fileFolder + "/" + filename + "/" + filename + ".wav";
	    	  
	    	  File file = new File(fullName);
	    	  InputStream is = new FileInputStream(file);

	   	      long len = file.length();
		      byte[] bytes = new byte[(int)len];
		      int offset = 0;
	          int numRead = 0;
	          while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	              offset += numRead;
	          }
	          System.out.println("Start loading frame: " + fullName);
	    	  int index = 0;
	          BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	          for (int y = 0; y < HEIGHT; y++) {
	            for (int x = 0; x < WIDTH; x++) {
	   				byte r = bytes[index];
	   				byte g = bytes[index+HEIGHT*WIDTH];
	   				byte b = bytes[index+HEIGHT*WIDTH*2]; 
	   				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	    			image.setRGB(x,y,pix);
	    			index++;
	    		}
	    	  }
	          if(images.size() > 0)
	        	  MotionMatch.GetMotionVectors(images.get(images.size() - 1), image, Constants.QUERY_VECTOR_MAP_LIST);
	          images.add(image);
	          is.close();
	          playSound = new PlaySound(audioFilename);
	          System.out.println("End loading query frame: " + fullName);
	      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

	
	VideoQueryUI ui = new VideoQueryUI(images, playSound);
	ui.showUI();

  }

}