import java.awt.GridLayout;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JFrame;

public class AudioMatch {
	public static HashMap<String,Integer> GetAudioMap(String query){
		if(query == "" || query == null)
			return null;
		HashMap<String,Integer> resultMap = new HashMap<String,Integer>();
		
		String filename = query;
		
		HashMap<Integer, Integer> fps = new HashMap<>();
		double[][] stanDevs = new double[8][500];
		//HashMap<Integer, Integer> midDiscrete = new HashMap<>();
		for(String dbVideoName:Constants.DB_FILE_NAMES){
			
		}
		for(int i = 0; i < Constants.DB_FILE_NAMES.length; i++){
			setTable(fps, Constants.DB_FILE_NAMES[i], i, stanDevs);
		}
	
		
		       
		int[] count = {0,0,0,0,0,0,0};
		
		WaveFileReader reader = new WaveFileReader(filename,"query");       
		
		long sampleRate = reader.getSampleRate();
		
		if(reader.isSuccess()){  
		    int[] data = reader.getData()[0]; //get first channel
		    
		    LinkedList<Landmark> landmarks = new LinkedList<Landmark>();
		    int countLM = 0;
		    
		    //get landmarks
		    for(int i = 0; i < data.length - Constants.AUDIO_WINDOW; i = i + Constants.AUDIO_WINDOW){
		    	Complex[] x = new Complex[Constants.AUDIO_WINDOW];
		    	for(int j = 0; j < Constants.AUDIO_WINDOW; j++){
		    		x[j] = new Complex(data[i+j], 0);
		    	}
		    	Complex[] y = fft(x);
		    	
		    	double avg = (double)0;
		    	double sum = (double)0;
		    	int count1 = 0;
		    	for(int j = 0; j < Constants.AUDIO_WINDOW; j++){
		    		if(y[j].re() > 0){
		    			sum += y[j].re();
		    			count1++;
		    		}
		    	}
		    	avg = sum/count1;
		    	
		    	//sort frequency
		    	int[] lmNumber = new int[Constants.AUDIO_LMSIZE];
		    	for(int k = 0; k < Constants.AUDIO_LMSIZE; k++){
		    		int maxNUmber = 0;
		    		for(int j = 0; j < Constants.AUDIO_WINDOW; j++){
		    			if(y[j].re() > y[maxNUmber].re()){
		    				maxNUmber = j;
		    			}
		    		}
		    		lmNumber[k] = maxNUmber;
		    		y[maxNUmber] = new Complex(0, 0);
		    	}
		    	for(int k = 0; k < Constants.AUDIO_LMSIZE; k++){
		    		Landmark lm = new Landmark((double)sampleRate / Constants.AUDIO_WINDOW * lmNumber[k], (double)i/sampleRate);//landmark contains time and frequency 
					landmarks.add(lm);
					countLM++;
		    	}
		    	
		        double stanDev = (double)0;
		    	for(int j = 0; j < Constants.AUDIO_WINDOW; j++){
		    		if(y[j].re() > 0){
		    			stanDev += (y[j].re() - avg) * (y[j].re() - avg);   
		    		}
		    	}
		    	stanDev = stanDev/Constants.AUDIO_WINDOW;
		    	stanDev = Math.sqrt(stanDev);
		    	stanDevs[7][i/Constants.AUDIO_WINDOW] = stanDev;
		    	
		    }
		    
		    //System.out.println("Test's countLM: " + countLM);
		
		    //get fingerprint
		    int countFP = 0;
		    java.util.Iterator<Landmark> itOut = landmarks.iterator();
		    
		
		    while(itOut.hasNext()){
		    	java.util.Iterator<Landmark> itIn = landmarks.iterator();
		    	Landmark lm = itOut.next();
		    	
		    	while(itIn.hasNext()){
		    		Landmark lmIn = itIn.next();
		    		double difFreq = lmIn.freq() - lm.freq();
		    		double difTime = lmIn.time() - lm.time();
		    		if(difFreq > (0 - Constants.AUDIO_FRETARGET) && difFreq < Constants.AUDIO_FRETARGET && difTime > Constants.AUDIO_TIMETARGET && difTime < 2 * Constants.AUDIO_TIMETARGET){
		    			int fingerPrint = ((int)difFreq) * 10 + (int)((difTime - 1) * 10);
		    			
		    			
		    			if(fps.containsKey(fingerPrint)){
		    				countFP++;
		        			
		        			int id = fps.get(fingerPrint);
		        			//7 bits represent 7 files in database
		        			for(int i = 0; i < 7; i++){
		        				if((id & (1 << i)) != 0){
		        					count[i]++;
		        				}
		        			}
		    			}
		    			break;
		    		}
		    	}
		    }
		    

		    
		    for(int i = 0; i < Constants.DB_FILE_NAMES.length; i++){
				resultMap.put(Constants.DB_FILE_NAMES[i], count[i]);
			}
		   
		    System.out.println(resultMap);
		   	return resultMap;
		}
		return null;
	}
	
	public static void setTable(HashMap<Integer, Integer> fps, String filename, int id, double[][] stanDevs){
    	
    	WaveFileReader reader = new WaveFileReader(filename, "db");       
        
        long sampleRate = reader.getSampleRate();

        if(reader.isSuccess()){  
            int[] data = reader.getData()[0]; //get first channel
            
            LinkedList<Landmark> landmarks = new LinkedList<Landmark>();
            int countLM = 0;
            
            //get landmarks window by window
            for(int i = 0; i < data.length - Constants.AUDIO_WINDOW; i = i+Constants.AUDIO_WINDOW){
            	Complex[] x = new Complex[Constants.AUDIO_WINDOW];
            	for(int j = 0; j < Constants.AUDIO_WINDOW; j++){
            		x[j] = new Complex(data[i+j], 0);
            	}
            	Complex[] y = fft(x);
            	
            	
            	//sort frequency
            	int[] lmNumber = new int[Constants.AUDIO_LMSIZE];
            	for(int k = 0; k < Constants.AUDIO_LMSIZE; k++){
            		int maxNUmber = 0;
            		for(int j = 0; j < Constants.AUDIO_WINDOW; j++){
            			if(y[j].re() > y[maxNUmber].re()){
            				maxNUmber = j;
            			}
            		}
            		lmNumber[k] = maxNUmber;
            		y[maxNUmber] = new Complex(0, 0);
            	}
            	for(int k = 0; k < Constants.AUDIO_LMSIZE; k++){
            		Landmark lm = new Landmark((double)sampleRate / Constants.AUDIO_WINDOW * lmNumber[k], (double)i/sampleRate);//landmark contains time and frequency 
        			landmarks.add(lm);
        			countLM++;
            	}
            	
            	
            	double avg = (double)0;
            	double sum = (double)0;
            	int count = 0;
            	for(int j = 0; j < Constants.AUDIO_WINDOW; j++){
            		if(y[j].re() > 0){
            			sum += y[j].re();
            			count++;
            		}
            	}
            	avg = sum/count;
            	
            	double stanDev = (double)0;
            	for(int j = 0; j < Constants.AUDIO_WINDOW; j++){
            		if(y[j].re() > 0){
            			stanDev += (y[j].re() - avg) * (y[j].re() - avg);   
            		}
            	}
            	stanDev = stanDev/Constants.AUDIO_WINDOW;
            	stanDev = Math.sqrt(stanDev);
            	stanDevs[id][i/Constants.AUDIO_WINDOW] = stanDev;

            	
            }

            //get fingerprint with landmarks
            java.util.Iterator<Landmark> itOut = landmarks.iterator();
            int countFP = 0;
            
            //iterate landmarks
            
            while(itOut.hasNext()){
            	java.util.Iterator<Landmark> itIn = landmarks.iterator();
            	Landmark lm = itOut.next();
            	
            	//for each landmark, try to find a target zone
            	while(itIn.hasNext()){
            		Landmark lmIn = itIn.next();
            		double difFreq = lmIn.freq() - lm.freq();
            		double difTime = lmIn.time() - lm.time();
            		
            		//frequency and time limitation of target zone
            		if(difFreq > (0 - Constants.AUDIO_FRETARGET) && difFreq < Constants.AUDIO_FRETARGET && difTime > Constants.AUDIO_TIMETARGET && difTime < 2 * Constants.AUDIO_TIMETARGET){           			
            			int fingerPrint = ((int)difFreq) * 10 + (int)((difTime - 1) * 10);//fingerprint composed of frequency and time
            			countFP++;
            			
            			
            			//insert fingerprint into hash table
            			if(fps.containsKey(fingerPrint)){
            				if((fps.get(fingerPrint)&(1 << id)) == 0){
            					fps.put(fingerPrint, fps.get(fingerPrint) + (1 << id));
            				}
            			}else{            				
            				fps.put(fingerPrint, 1 << id);
            			}
            			break;
            		}
            	}
            }  
        }
            
        else{  
            System.err.println(filename + "File Error");  
        }
    }
    
    
 // compute the FFT of x[], assuming its length is a power of 2
    public static Complex[] fft(Complex[] x) {
        int n = x.length;

        // base case
        if (n == 1) return new Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (n % 2 != 0) {
            throw new IllegalArgumentException("n is not a power of 2");
        }

        // fft of even terms
        Complex[] even = new Complex[n/2];
        for (int k = 0; k < n/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < n/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n/2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + n/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }
}
