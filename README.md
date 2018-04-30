# 576
## reference paper/websites in folder '/references'
## source code in folder '/src'
## compiled files in folder '/bin'

##To run player
1. Import query folder and db (name changed from databse_videos) folder  
2. Run command in folder or import project into eclipse
  
```
javac VideoQuery.java
```
```
java VideoQuery first :: first is the file name in query folder
```

##Color
###Preprocess
Please add `json-simple-1.1.1.jar` in folder `jar` into project libraries.
For Eclipse:
```sh
right click Project Name -> Properties -> Java Build Path -> Libraries -> Add External JARs
```  
Then run class `Preprocess` and make sure all the result JSON files are generated successfully.
###Generate dissimilarity score array
Run `ColorComparator` and see the results in the ArrayList<Double>
To be more accurate, you can decrease two subsampling parameter:
`private int SUB_SAMPLE_RATE = 10;`
`private int LAST_150_SUBSAMPLE_RATE = 5;`


###OpenCV
Install OpenCV following by this tutorial  
[http://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html](http://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html)



