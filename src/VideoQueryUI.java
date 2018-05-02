import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;  
import org.jfree.chart.ChartPanel;  
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.json.simple.parser.ParseException;
import org.math.plot.*;

public class VideoQueryUI extends Frame implements ActionListener,ChangeListener  {
	private static final long serialVersionUID = 1L; // any unique long number
			
	private String queryFileName = "";
    private String dbFileName = "";
	
    private ArrayList<BufferedImage> images;
    private PlaySound playSound;
    
    private HashMap<String ,ArrayList<BufferedImage>> dbVideoMap = new HashMap<String ,ArrayList<BufferedImage>>();
    private HashMap<String, PlaySound> dbSoundMap = new HashMap<String, PlaySound>();
    
    private HashMap<String ,Double[]> overallScoreArrayMap = new HashMap<String ,Double[]>();
    private HashMap<String ,Double[]> motionScoreArrayMap = new HashMap<String ,Double[]>();
    private HashMap<String ,Double[]> colorScoreArrayMap = new HashMap<String ,Double[]>();
    private HashMap<String ,Double> audioScoreMap = new HashMap<String ,Double>();
    private HashMap<String ,Double[]> multipleScoreArrayMap = new HashMap<String ,Double[]>();
    
    private JLabel imageLabel;
    private JLabel resultImageLabel;
    
    private JLabel errorLabel;
    private TextField queryField;
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private Button resultPlayButton;
    private Button resultPauseButton;
    private Button resultStopButton;
    private Button loadQueryButton;
    private Button loadResultButton;
    private Button searchButton;
    public static JSlider slider;
    
    private List resultListDisplay;
    private Map<String, Double> resultMap;
    private Map<String, Double> sortedResultMap;
    private ArrayList<Double> resultList;
    private ArrayList<String> resultListRankedNames;
    
    private int playStatus = 3;//1 for play, 2 for pause, 3 for stop
    private int resultPlayStatus = 3;
    private Thread playingThread;
    private Thread playingDBThread;
    private Thread audioThread;
    private Thread audioDBThread;
    private int currentFrameNum = 0;
    private int currentDBFrameNum = 0;
    
    private boolean isDragging = false;

    private String fileFolder = System.getProperty("user.dir") + "/query";
    private String dbFileFolder = System.getProperty("user.dir") + "/db";
    static final int WIDTH = Constants.WIDTH;
    static final int HEIGHT = Constants.HEIGHT;
    
    Panel dataPanel;
    ChartPanel chartPanel = new ChartPanel(null);
    
	public VideoQueryUI(String query) {
		
		Panel topPanel = new Panel();
		topPanel.setLayout(new BorderLayout());
		
		//Query Panel
	    Panel queryPanel = new Panel();
	    queryPanel.setLayout(new BorderLayout());
	    
	    queryField = new TextField(13);
	    JLabel queryLabel = new JLabel("Query: ");
	    loadQueryButton = new Button("Load Query Video");
	    loadQueryButton.addActionListener(this);
	    searchButton = new Button("Search");
	    searchButton.setFont(new Font("monspaced", Font.BOLD, 60));
	    searchButton.addActionListener(this);
	    
	    queryPanel.add(queryLabel, BorderLayout.WEST);
	    queryPanel.add(queryField, BorderLayout.CENTER);
	    queryPanel.add(loadQueryButton, BorderLayout.EAST);
	    queryPanel.add(searchButton, BorderLayout.SOUTH);
	    
	    //Result Panel
	    Panel resultPanel = new Panel();
	    resultPanel.setLayout(new BorderLayout());
	    
	    resultListDisplay = new List(7);
	    resultListDisplay.add("Matched Videos:    ");
	    loadResultButton = new Button("Load Selected Video");
	    loadResultButton.addActionListener(this);
	    
	    resultPanel.add(resultListDisplay, BorderLayout.WEST);
	    resultPanel.add(loadResultButton, BorderLayout.EAST);
	    
	    //Error Message
	    errorLabel = new JLabel("");
	    errorLabel.setForeground(Color.RED);
	    
	    topPanel.add(errorLabel, BorderLayout.NORTH);
	    topPanel.add(queryPanel, BorderLayout.WEST);
	    topPanel.add(resultPanel, BorderLayout.EAST);
//	    controlQueryPanel.setLayout(new GridLayout(2, 0));
	    
	    add(topPanel, BorderLayout.NORTH);
	    
	  
	    //Query video panel
	    Panel queryVideoPanel = new Panel();
	    queryVideoPanel.setLayout(new BorderLayout());
	    
	    this.imageLabel = new JLabel();
	    
	    Panel controlPanel = new Panel();
	    controlPanel.setLayout(new BorderLayout());
	    playButton = new Button("PLAY");
	    playButton.addActionListener(this);
	    pauseButton = new Button("PAUSE");
	    pauseButton.addActionListener(this);
	    stopButton = new Button("STOP");
	    stopButton.addActionListener(this);
	    controlPanel.add(playButton, BorderLayout.WEST);
	    controlPanel.add(pauseButton, BorderLayout.CENTER);
	    controlPanel.add(stopButton, BorderLayout.EAST);
	    
	    queryVideoPanel.add(this.imageLabel, BorderLayout.CENTER);
	    queryVideoPanel.add(controlPanel, BorderLayout.SOUTH);
	    
	    add(queryVideoPanel, BorderLayout.WEST);
	    
	    
	    //Result video panel
	    Panel resultVideoPanel = new Panel();
	    resultVideoPanel.setLayout(new BorderLayout());
	    
	    
	    Panel resultSliderPanel = new Panel();
	    resultSliderPanel.setLayout(new BorderLayout());
	    dataPanel = resultSliderPanel;
	    
	    slider = new JSlider();
//		slider.addChangeListener(this);
		
		
		MouseListener wrapper = new MouseListener() {
		    @Override
		    public void mousePressed(MouseEvent e) {
		    	isDragging = true;
		    }

		    @Override
		    public void mouseReleased(MouseEvent e) {
		    	isDragging = false;
		    }

		    @Override
		    public void mouseEntered(MouseEvent e) {
		  
		    }

		    @Override
		    public void mouseExited(MouseEvent e) {
		    }

			@Override
			public void mouseClicked(MouseEvent e) {

			}
		};
		
		slider.addMouseListener(wrapper);
		slider.removeMouseListener(wrapper);
		
		slider.setPaintTicks(true);
		slider.setBorder(null);
		slider.setBounds(604, 273, 352, 31);
		slider.setValue(0);
		resultSliderPanel.add(slider, BorderLayout.NORTH);
		
		this.resultImageLabel = new JLabel();
		
		Panel resultControlPanel = new Panel();
		resultControlPanel.setLayout(new BorderLayout());
		resultPlayButton = new Button("PLAY");
	    resultPlayButton.addActionListener(this);
	    resultPauseButton = new Button("PAUSE");
	    resultPauseButton.addActionListener(this);
	    resultStopButton = new Button("STOP");
	    resultStopButton.addActionListener(this);
	    resultControlPanel.add(resultPlayButton, BorderLayout.WEST);
	    resultControlPanel.add(resultPauseButton, BorderLayout.CENTER);
	    resultControlPanel.add(resultStopButton, BorderLayout.EAST);

	    resultVideoPanel.add(resultSliderPanel, BorderLayout.NORTH);
	    resultVideoPanel.add(this.resultImageLabel, BorderLayout.CENTER);
	    resultVideoPanel.add(resultControlPanel, BorderLayout.SOUTH);
	    
	    add(resultVideoPanel, BorderLayout.EAST);
	    
	    this.setQuery(query);
		this.loadDBVideos();
	}
	
	public void setQuery(String query){
		queryFileName = query;
		this.loadVideo(queryFileName);
	}
	
	private HashMap<String, Double> StandardizedAudioMap(HashMap<String, Double> map){
		HashMap<String, Double> resultMap = new HashMap<String, Double>();
//		Double max = 0.0;
//		for (Map.Entry<String, Integer> entry : map.entrySet()) {  
//		    max = Math.max(max, entry.getValue());
//		}  
//		for (Map.Entry<String, Integer> entry : map.entrySet()) {  
//		    resultMap.put(entry.getKey(), entry.getValue() / max);
//		}  
		resultMap = map;
		return resultMap;
	}
	
	private void loadVideo(String userInput) {
		System.out.println("Start loading query video contents.");
		
		//Match audio
		audioScoreMap = StandardizedAudioMap(AudioMatch.GetAudioMap(userInput));
		
		
	    try {
	      if(userInput == null || userInput.isEmpty()){
	    	  return;
	      }
	      //every query video in has 150 frames
	      images = new ArrayList<BufferedImage>();
	      Constants.QUERY_VECTOR_MAP_LIST.clear();
	      String audioFilename = fileFolder + "/" + userInput + "/" + userInput + ".wav";
    	  
	      for(int i=1; i<=150; i++) {
	    	  String fileNum = "00";
	    	  if(i < 100 && i > 9) {
	    		  fileNum = "0";
	    	  } else if(i > 99) {
	    		  fileNum = "";
	    	  }
	    	  String fullName = fileFolder + "/" + userInput + "/" + userInput + "_" + fileNum + new Integer(i).toString() + ".rgb";
	    	  
	    	  File file = new File(fullName);
	    	  InputStream is = new FileInputStream(file);

	   	      long len = file.length();
		      byte[] bytes = new byte[(int)len];
		      int offset = 0;
	          int numRead = 0;
	          while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	              offset += numRead;
	          }
//	          System.out.println("Start loading frame: " + fullName);
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
//	          System.out.println("End loading query frame: " + fullName);
	      }
	      playSound = new PlaySound(audioFilename);
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	      errorLabel.setText(e.getMessage());
	    } catch (IOException e) {
	      e.printStackTrace();
	      errorLabel.setText(e.getMessage());
	    }
	    displayScreenShot();
	}

	
	private void loadDBVideos() {
		System.out.println("Start loading db video contents.");
	    try {
		  //every query video in has 600 frames
	    	for(String dbVideoName:Constants.DB_FILE_NAMES){
	    		ArrayList<BufferedImage> dbImages = new ArrayList<BufferedImage>();
	    		String audioFilename = dbFileFolder + "/" + dbVideoName + "/" + dbVideoName + ".wav";
				for(int i=1; i<=600; i++) {
				  String fileNum = "00";
				  if(i < 100 && i > 9) {
					  fileNum = "0";
				  } else if(i > 99) {
					  fileNum = "";
				  }
				  String fullName = dbFileFolder + "/" + dbVideoName + "/" + dbVideoName + fileNum + new Integer(i).toString() + ".rgb";
				  
				  
				  File file = new File(fullName);
				  InputStream is = new FileInputStream(file);
				
				  long len = file.length();
				  byte[] bytes = new byte[(int)len];
				  int offset = 0;
				  int numRead = 0;
				  while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				      offset += numRead;
				  }
//				  System.out.println("Start loading db frame: " + fullName);
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

				  dbImages.add(image);
				  is.close();

//				  System.out.println("End loading db frame: " + fullName);
				}//end for
				dbVideoMap.put(dbVideoName,dbImages);
				dbSoundMap.put(dbVideoName,new PlaySound(audioFilename));
	    	}
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	      errorLabel.setText(e.getMessage());
	    } catch (IOException e) {
	      e.printStackTrace();
	      errorLabel.setText(e.getMessage());
	    }
	    displayDBScreenShot();
	    
	}
	
	public void showUI() {
	    pack();
	    setVisible(true);
	}
	
	private void playVideo() {
		playingThread = new Thread() {
            public void run() {
	            System.out.println("Start playing video: " + queryFileName);
	          	for (int i = currentFrameNum; i < Constants.NO_QUERY_FRAMES; i++) {
	          		long st = System.currentTimeMillis();
	          	  	imageLabel.setIcon(new ImageIcon(images.get(i)));
	          	  	long et = System.currentTimeMillis();
	          	    try {
	                  	sleep(1000/Constants.FRAME_RATE - (et - st));
	          	    } catch (InterruptedException e) {
	          	    	if(playStatus == 3) {
	          	    		currentFrameNum = 0;
	          	    	} else {
	          	    		currentFrameNum = i;
	          	    	}
	          	    	imageLabel.setIcon(new ImageIcon(images.get(currentFrameNum)));
	                  	currentThread().interrupt();
	                  	break;
	                }
	          	}
	          	if(playStatus < 2) {
	          		playStatus = 3;
		            currentFrameNum = 0;
	          	}
	            System.out.println("End playing video: " + queryFileName);
	        }
	    };
	    audioThread = new Thread() {
            public void run() {
                try {
        	        playSound.play();
        	    } catch (PlayWaveException e) {
        	        e.printStackTrace();
        	        errorLabel.setText(e.getMessage());
        	        return;
        	    }
	        }
	    };
	    audioThread.start();
	    playingThread.start();
	}
	
	private void playDBVideo() {
		playingDBThread = new Thread() {
            public void run() {
	            System.out.println("Start playing result video: " + dbFileName);
	          	for (int i = currentDBFrameNum; i < Constants.NO_DB_FRAMES; i++) {
	          		long st = System.currentTimeMillis();
	          		resultImageLabel.setIcon(new ImageIcon(dbVideoMap.get(dbFileName).get(i)));
	          	  	slider.setValue(i * 100 / Constants.NO_DB_FRAMES);
	          	  	long et = System.currentTimeMillis();
	          	    try {
	                  	sleep(Math.max(0, 1000/Constants.FRAME_RATE - (et - st)));
//	                  	System
	          	    } catch (InterruptedException e) {
	          	    	if(resultPlayStatus == 3) {
	          	    		currentDBFrameNum = 0;
	          	    	} else {
	          	    		currentDBFrameNum = i;
	          	    	}
	          	    	resultImageLabel.setIcon(new ImageIcon(dbVideoMap.get(dbFileName).get(currentDBFrameNum)));
	                  	currentThread().interrupt();
	                  	break;
	                }
	          	}

	          	if(resultPlayStatus < 2) {
	          		resultPlayStatus = 3;
			        currentDBFrameNum = 0;
	          	}
	          	System.out.println("End playing result video: " + dbFileName);
	        }
	    };
	    audioDBThread = new Thread() {
            public void run() {
                try {
                	dbSoundMap.get(dbFileName).play();
        	    } catch (PlayWaveException e) {
        	        e.printStackTrace();
        	        errorLabel.setText(e.getMessage());
        	        return;
        	    }
	        }
	    };
	    audioDBThread.start();
	    playingDBThread.start();
	}
	
	private void pauseVideo() throws InterruptedException {
		if(playingThread != null) {
			playingThread.interrupt();
			audioThread.interrupt();
			playSound.pause();
			playingThread = null;
			audioThread = null;
		}
	}
	
	private void pauseDBVideo() throws InterruptedException {
		if(playingDBThread != null){
			playingDBThread.interrupt();
			audioDBThread.interrupt();
			dbSoundMap.get(dbFileName).pause();
			playingDBThread = null;
			audioDBThread = null;
		}
	}
	
	private void stopVideo() {
		if(playingThread != null) {
			playingThread.interrupt();
			audioThread.interrupt();
			playSound.stop();
			playingThread = null;
			audioThread = null;
		} else {
			currentFrameNum = 0;
			displayScreenShot();
		}
	}
	
	private void stopDBVideo() {
		if(playingDBThread != null) {
			playingDBThread.interrupt();
			audioDBThread.interrupt();
			dbSoundMap.get(dbFileName).stop();
			playingDBThread = null;
			audioDBThread = null;
		} else {
			currentDBFrameNum = 0;
			displayDBScreenShot();
		}
	}
	
	private void displayScreenShot() {
		this.playStatus = 3;
	    currentFrameNum = 0;
	    System.out.println("End loading query video contents.");
		Thread initThread = new Thread() {
            public void run() {
	          	imageLabel.setIcon(new ImageIcon(images.get(currentFrameNum)));  	   
	        }
	    };
	    initThread.start();
	}
	
	private void displayDBScreenShot() {
		this.resultPlayStatus = 3;
	    currentDBFrameNum = 0;
	    System.out.println("End loading db video contents.");
		Thread initThread = new Thread() {
            public void run() {
            	if(dbFileName == "")
            		resultImageLabel.setIcon(new ImageIcon(dbVideoMap.get("musicvideo").get(currentDBFrameNum)));  
            	else
            		resultImageLabel.setIcon(new ImageIcon(dbVideoMap.get(dbFileName).get(currentDBFrameNum)));  	   
	        }
	    };
	    initThread.start();
	}
	
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == this.playButton) {
			System.out.println("play button clicked");
			if(this.playStatus > 1) {
				this.playStatus = 1;
				this.playVideo();
			}
		} else if(e.getSource() == this.resultPlayButton) {
			System.out.println("result play button clicked");
			if(dbFileName == "") return;
			if(this.resultPlayStatus > 1) {
				this.resultPlayStatus = 1;
				this.playDBVideo();
			}
		} else if(e.getSource() == this.pauseButton) {
			System.out.println("pause button clicked");
			if(this.playStatus == 1) {
				this.playStatus = 2;
				try {
					this.pauseVideo();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					errorLabel.setText(e1.getMessage());
				}
			}
		} else if(e.getSource() == this.resultPauseButton) {
			System.out.println("result pause button clicked");
			if(dbFileName == "") return;
			if(this.resultPlayStatus == 1) {
				this.resultPlayStatus = 2;
				try {
					this.pauseDBVideo();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					errorLabel.setText(e1.getMessage());
					e1.printStackTrace();
				}
			}
		} else if(e.getSource() == this.stopButton) {
			System.out.println("stop button clicked");
			if(this.playStatus < 3) {
				this.playStatus = 3;
				this.stopVideo();
			}
		} else if(e.getSource() == this.resultStopButton) {
			System.out.println("result stop button clicked");
			if(dbFileName == "") return;
			if(this.resultPlayStatus < 3) {
				this.resultPlayStatus = 3;
				this.stopDBVideo();
			}
		}
		else if(e.getSource() == this.loadQueryButton) {
			String userInput = queryField.getText();
			if(userInput != null && !userInput.isEmpty()) {
				this.playingThread = null;
				this.audioThread = null;
				queryFileName = userInput.trim();
				this.loadVideo(queryFileName);
			}
		} else if(e.getSource() == this.searchButton){
			// Video query algorithm here
			System.out.println("Start search!");
			
			resultMap = new HashMap<String, Double>();
			for(String dbVideo:Constants.DB_FILE_NAMES) {
				//Motion Match
				
				Double[] scores = new Double[Constants.NO_DB_FRAMES];
				for(int i = 0; i < scores.length; i++){
					if(i == 0) {
						scores[i] = 0.0;
					} else {
						Double score =MotionMatch.GetFrameScore(dbVideoMap.get(dbVideo).get(i - 1), dbVideoMap.get(dbVideo).get(i), Constants.QUERY_VECTOR_MAP_LIST);
						scores[i] = score;
					}

				}
				motionScoreArrayMap.put(dbVideo, scores);
				
				
				//Color Match
				ColorComparator cc;
				try {
					cc = new ColorComparator();
					cc.readQuery(queryFileName);
					ArrayList<Double> f = cc.computeDissimilarityByResultFolder(dbVideo);

					colorScoreArrayMap.put(dbVideo, convertDissimilarityToScore(f));
				
				} catch (IOException | ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				//Overall Match
				Double[] overallScore = new Double[Constants.NO_DB_FRAMES];
				
				for(int i = 0; i < Constants.NO_DB_FRAMES; i++){
					overallScore[i] = (colorScoreArrayMap.get(dbVideo)[i] * Constants.COLOR_WEIGHT + motionScoreArrayMap.get(dbVideo)[i] * Constants.MOTION_WEIGHT + audioScoreMap.get(dbVideo) * Constants.AUDIO_WEIGHT) / (Constants.COLOR_WEIGHT + Constants.MOTION_WEIGHT + Constants.AUDIO_WEIGHT);
				}
				
				overallScoreArrayMap.put(dbVideo, overallScore);
				
				
				//Multiple Match
				Double[] multipleScore = new Double[Constants.NO_DB_FRAMES];
				
				for(int i = 0; i < Constants.NO_DB_FRAMES; i++){
					multipleScore[i] = colorScoreArrayMap.get(dbVideo)[i] * motionScoreArrayMap.get(dbVideo)[i] * audioScoreMap.get(dbVideo) ;
				}
				
				multipleScoreArrayMap.put(dbVideo, multipleScore);
				
				
				//Caculate Score
				double score = 0.0;
				for(int i = 0; i < Constants.NO_QUERY_FRAMES; i ++){
					score += overallScoreArrayMap.get(dbVideo)[i];
				}
				double maxScore = score; 
				
				for(int i = 0, j = Constants.NO_QUERY_FRAMES - 1; j < Constants.NO_DB_FRAMES; i ++, j ++) {
					score = score + overallScoreArrayMap.get(dbVideo)[j] - overallScoreArrayMap.get(dbVideo)[i];
					maxScore = Math.max(maxScore, score);
				}
				
				resultMap.put(dbVideo, maxScore / Constants.NO_QUERY_FRAMES);
				
			}
			System.out.println("Finish search!");
	
			
			//Sort
			resultListDisplay.removeAll();
		    resultListDisplay.add("Matched Videos:    ");
		    resultList = new ArrayList<Double>(7);
		    resultListRankedNames = new ArrayList<String>(7);
			sortedResultMap = new HashMap<String, Double>();
		    
		    Iterator<Entry<String, Double>> it = resultMap.entrySet().iterator();
		    while (it.hasNext()) {
		        Entry<String, Double> pair = (Entry<String, Double>)it.next();
		        String videoName = (String)pair.getKey();
		        Double videoRank = new BigDecimal((Double)pair.getValue()).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
		        resultList.add(videoRank);
		        sortedResultMap.put(videoName, videoRank);
		    }
		    Collections.sort(resultList);
		    Collections.reverse(resultList);
		    for(int i=0; i<resultList.size(); i++) {
		    	Double tmpRank = resultList.get(i);
		    	it = sortedResultMap.entrySet().iterator();
			    while (it.hasNext()) {
			    	Entry<String, Double> pair = (Entry<String, Double>)it.next();
			    	Double videoRank = (Double)pair.getValue();
			    	if(videoRank == tmpRank) {
			    		String displayScore = String.format("%.2f", videoRank * 100);
			    		resultListDisplay.add(pair.getKey() + "   " + displayScore + "%");
			    		resultListRankedNames.add((String)pair.getKey());
			    		break;
			    	}
			    }
		    }
		} else if(e.getSource() == this.loadResultButton) {
			// Load selected video in results
			int userSelect = resultListDisplay.getSelectedIndex() - 1;
			if(userSelect > -1) {
				this.playingDBThread = null;
				this.audioDBThread = null;
				dbFileName = resultListRankedNames.get(userSelect);
				DisplayChart();
				displayDBScreenShot();
			}
		}
	}
	
	private Double[] convertDissimilarityToScore (ArrayList<Double> originalList) {
		Double[] originalArray = originalList.toArray(new Double[originalList.size()]);
		Double[] resArray = new Double[Constants.NO_DB_FRAMES];
		Double max = 0.0;
		for(int i = 0; i < Constants.NO_DB_FRAMES; i++){
			resArray[i] = 0.0;
		}
		for(int i = 0; i < originalArray.length; i++){
			max = Math.max(max, originalArray[i]);
		}
		
		for(int i = 0; i < originalArray.length; i++){
			Double currentScore = 1 - originalArray[i]/max;
			for (int j = 0; j < Constants.NO_QUERY_FRAMES && i + j < Constants.NO_DB_FRAMES; j++){
				resArray[i + j] = Math.max(resArray[i + j], currentScore);
			}
		}
		return resArray;
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if(isDragging == false) return;
		int usedStatus = resultPlayStatus;
		if(slider.getValueIsAdjusting()){ 

				this.resultPlayStatus = 2;
				try {
					this.pauseDBVideo();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					errorLabel.setText(e1.getMessage());
					e1.printStackTrace();
				}
			
		} else {
//			if(slider.getValue() != 0) { // 改为判断是否真的db video
				currentDBFrameNum = slider.getValue() * Constants.NO_DB_FRAMES / 100;
				System.out.println("cf:"+currentDBFrameNum);
				this.resultPlayStatus = usedStatus;
				if(usedStatus == 1)
					this.playDBVideo();
				if(usedStatus == 1)
					this.playDBVideo();
//			}   
		}
    }
	
	private CategoryDataset GetCurrentDataset() {
	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    String series0 = "Overall";
	    String series1 = "Motion";
	    String series2 = "Color";
	    String series3 = "Audio";
	    String series4 = "Multiple";
	    for(Integer i = 0; i < Constants.NO_DB_FRAMES; i ++){
	    	dataset.addValue(overallScoreArrayMap.get(dbFileName)[i], series0, i);
	    	dataset.addValue(motionScoreArrayMap.get(dbFileName)[i], series1, i);
	    	dataset.addValue(colorScoreArrayMap.get(dbFileName)[i], series2, i);
	    	dataset.addValue(audioScoreMap.get(dbFileName), series3, i);
	    	dataset.addValue(multipleScoreArrayMap.get(dbFileName)[i], series4, i);
	    	
	    }
	 
	    return dataset;
	}

	private void DisplayChart () {
		
//		version1 chart
		dataPanel.remove(chartPanel);
		JFreeChart chart=ChartFactory.createLineChart(
    			null,  //图表标题
    			null,  //X轴lable
    			null,  //Y轴lable
    			GetCurrentDataset(), //数据集
    			PlotOrientation.VERTICAL,
    			//图表放置模式水平/垂直 
    			true, //显示lable
    			false,  //显示提示
    			false //显示urls
    			);
		chartPanel = new ChartPanel(chart,false);
		dataPanel.add(chartPanel,BorderLayout.SOUTH);
		invalidate();
		validate();
		repaint();
		
		/*version2 chart
		double[] y = new double[motionScoreArrayMap.get(dbFileName).length];
		
		for(int i = 0; i < motionScoreArrayMap.get(dbFileName).length; i++){
			y[i] = motionScoreArrayMap.get(dbFileName)[i];
		}
		
		// create your PlotPanel (you can use it as a JPanel)
		Plot2DPanel plot = new Plot2DPanel();
		
		// add a line plot to the PlotPanel
		plot.addLinePlot("Motion", y);
		 
		dataPanel.setPreferredSize(new Dimension(Constants.WIDTH, 100));
		
		dataPanel.add(plot,BorderLayout.SOUTH);
		*/


	}
	
}
