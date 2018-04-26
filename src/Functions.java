import java.awt.Graphics;
import java.awt.image.BufferedImage;


import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Functions {

	public static BufferedImage convertToGrayScale(BufferedImage image) {
		  BufferedImage result = new BufferedImage(
		            image.getWidth(),
		            image.getHeight(),
		            BufferedImage.TYPE_BYTE_GRAY);
		  Graphics g = result.getGraphics();
		  g.drawImage(image, 0, 0, null);
		  g.dispose();
		  System.out.println("V" + getGrayLevel(result,10,10));
		  return result;
	}
	
	public static int getGrayLevel (BufferedImage img, int x, int y) {
		return img.getRGB(x, y) & 0xFF;
	}
	

	
	public static BufferedImage getBlurImage(BufferedImage image) {
		
		int height = image.getHeight();
	    int width = image.getWidth();
	    // result is transposed, so the width/height are swapped
	    BufferedImage temp =  new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    double[] k = { 0.00598, 0.060626, 0.241843, 0.383103, 0.241843, 0.060626, 0.00598 };
	    // horizontal blur, transpose result
	    for (int y = 0; y < height; y++) {
	        for (int x = 3; x < width - 3; x++) {
	            float r = 0, g = 0, b = 0;
	            for (int i = 0; i < 7; i++) {
	                int pixel = image.getRGB(x + i - 3, y);
	                b += (pixel & 0xFF) * k[i];
	                g += ((pixel >> 8) & 0xFF) * k[i];
	                r += ((pixel >> 16) & 0xFF) * k[i];
	            }
	            int p = (int)b + ((int)g << 8) + ((int)r << 16);
	            // transpose result!
	            temp.setRGB(x, y, p);
	        }
	    }
	    return temp;
	}
	
	public static Mat img2Mat(BufferedImage in)
    {
          Mat out;
          byte[] data;
          int r, g, b;

          if(in.getType() == BufferedImage.TYPE_INT_RGB)
          {
              out = new Mat(Constants.HEIGHT, Constants.WIDTH, CvType.CV_8UC3);
              data = new byte[Constants.WIDTH * Constants.HEIGHT * (int)out.elemSize()];
              int[] dataBuff = in.getRGB(0, 0, Constants.WIDTH, Constants.HEIGHT, null, 0, Constants.WIDTH);
              for(int i = 0; i < dataBuff.length; i++)
              {
                  data[i*3] = (byte) ((dataBuff[i] >> 16) & 0xFF);
                  data[i*3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
                  data[i*3 + 2] = (byte) ((dataBuff[i] >> 0) & 0xFF);
              }
          }
          else
          {
              out = new Mat(Constants.HEIGHT, Constants.WIDTH, CvType.CV_8UC1);
              data = new byte[Constants.WIDTH * Constants.HEIGHT * (int)out.elemSize()];
              int[] dataBuff = in.getRGB(0, 0, Constants.WIDTH, Constants.HEIGHT, null, 0, Constants.WIDTH);
              for(int i = 0; i < dataBuff.length; i++)
              {
                r = (byte) ((dataBuff[i] >> 16) & 0xFF);
                g = (byte) ((dataBuff[i] >> 8) & 0xFF);
                b = (byte) ((dataBuff[i] >> 0) & 0xFF);
                data[i] = (byte)((0.21 * r) + (0.71 * g) + (0.07 * b)); //luminosity
              }
           }
           out.put(0, 0, data);
           return out;
     } 
	
	public static BufferedImage mat2Img(Mat in)
    {
        BufferedImage out;
        byte[] data = new byte[Constants.WIDTH * Constants.HEIGHT * (int)in.elemSize()];
        int type;
        in.get(0, 0, data);

        if(in.channels() == 1)
            type = BufferedImage.TYPE_BYTE_GRAY;
        else
            type = BufferedImage.TYPE_3BYTE_BGR;

        out = new BufferedImage(Constants.WIDTH, Constants.HEIGHT, type);

        out.getRaster().setDataElements(0, 0, Constants.WIDTH, Constants.HEIGHT, data);
        return out;
    } 
}
