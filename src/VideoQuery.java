public class VideoQuery {
  static final int WIDTH = 352;
  static final int HEIGHT = 288;

  public static void main(String[] args) {
    //get video name
	String filename = args[0];
	
	VideoQueryUI ui = new VideoQueryUI(filename);
	ui.showUI();

  }

}