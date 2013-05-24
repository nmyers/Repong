import processing.serial.*;

public class SerialReader {

  public Serial  myPort;
  public  int[]   data;
  public  int[]   smoothed_data;
  public  int[][] serialBuffer;
  private int serialBufferIndex;
  
  private int count;
  
  private String  rawData;
  private boolean noSerial;
  
  private int nValues;

  SerialReader(PApplet parent, int _portNo, int _portSpeed, int _nValues, int _bufferLength) {
    nValues = _nValues;
    data   = new int[_nValues];    
    smoothed_data = new int[_nValues];
    serialBuffer = new int[_nValues][_bufferLength];
    serialBufferIndex = 0;
    count = 0;
    noSerial = false;
    try {
      myPort = new Serial(parent, Serial.list()[_portNo], _portSpeed);
      println("Added Serial on > "+Serial.list()[_portNo]); 
    } catch (Exception e) {
     println("Can't connect to serial port. Serial value while output 0"); 
     noSerial = true;
    }
    for (int i=0;i<nValues;i++) {
       data[i] = 0;
       smoothed_data[i] = 0;    
    }
    
  }

  void update() {
    if (noSerial) {
      data[0]=0;
      data[1]=0;
      return;
    }
    while (myPort.available() > 0) {
      char c = myPort.readChar();
      if (c!='\n') {
        rawData += c;
      } else {
        int[] tmpdata;
        try {
          tmpdata = int(split(rawData, ','));
        } catch (Exception e) {
          rawData = "";
          return;
        }
        
        //Error in reading > skip
        if (tmpdata.length!=nValues) {
          rawData = "";
          return;
        }
        
        data = tmpdata;
        
        
        //no smoothing
        if (_("serialSmoothing").equals("none")) {
          for (int i=0;i<nValues;i++) {
             smoothed_data[i] = data[i];    
          }
        }
        
        //Smooothing
        if (_("serialSmoothing").equals("median")) {
          for (int i=0;i<nValues;i++) {
            try {
              serialBuffer[i][serialBufferIndex] = data[i];          
              int[] sorted = sort(serialBuffer[i]);
              smoothed_data[i] = sorted[round(sorted.length/2)];
            } catch (Exception e) {
              smoothed_data[i] = 0; // data[i];
            }
          }
          serialBufferIndex++;
          serialBufferIndex = serialBufferIndex % serialBuffer[0].length;
        }
        
        //Smooothing
        if (_("serialSmoothing").equals("runningaverage")) {
          for (int i=0;i<nValues;i++) {
            try {
              serialBuffer[i][serialBufferIndex] = data[i];
              int sum = 0;   
              for (int j=0;j<serialBuffer[i].length;j++) {
                sum += serialBuffer[i][j];
              }
              smoothed_data[i] = sum/serialBuffer[i].length;
            } catch (Exception e) {
              smoothed_data[i] = data[i];
            }
          }
          serialBufferIndex++;
          serialBufferIndex = serialBufferIndex % serialBuffer[0].length;
        }        
        
        println("serial reading raw ="+rawData);
        rawData = "";
      }
    }
  }
}

