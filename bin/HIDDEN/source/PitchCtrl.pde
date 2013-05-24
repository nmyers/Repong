/**
 * Reads the current pitch of the microphone (left or right channel)
 * "High" pitch  > moves pad up
 * "Low"  pitch  > moves down
 * no pitch or low volume --> no update
 *
 */



class PitchCtrl extends BaseCtrl {

  
  PitchDetectorYin pitchDetector;
  int bufferSize = 2048;
  
  float highPitch,lowPitch,lastPitch;
  
  float pitchBuffer[];
  
  int channel = 0; // 0 = left, 1 = right

  PitchCtrl(int _channel,PApplet _parent) {
    channel = _channel;
    
    pitchBuffer = new float[10];
    
    pitchDetector = new PitchDetectorYin(microphone.sampleRate(),bufferSize);
    pitchDetector.setChannel(channel);
    //microphone.addListener(pitchDetector);
    highPitch = 0;
    lowPitch  = 10000;
  }

  void update(Paddle paddle) {
    
    if (channel==0) {
      pitchDetector.getPitch(microphone.left.toArray());
    } else {
      pitchDetector.getPitch(microphone.right.toArray());
    }
      
    float pitch = round(pitchDetector.GetFrequency());
    
    if (_b("pitchAdjust")) {
      if (pitch>0 && pitchDetector.probability>0.85) {
        highPitch = min(_i("highPitch"),max(highPitch,pitch));
        lowPitch  = max(_i("lowPitch"),min(lowPitch,pitch));
      }
    } else {
        highPitch = _i("highPitch");
        lowPitch  = _i("lowPitch");
    }
    
   
    //float inc = map(pitch,lowPitch,highPitch/2,5,-5);
    float inc = map(constrain(pitch,_i("lowPitch"),_i("highPitch")),50,400,1,-1);   
    
    lastPitch = pitch;
    int level = 0;
    
    if (channel==0) {
      level = round(microphone.left.level()*1000);
    } else {
      level = round(microphone.right.level()*1000);
    }
    
    if (_i("pitchMethod")==1) {
      if (level<_i("minMicLevel")) return;
      if (pitchDetector.isPitched==false) return;
      
      //drawing the pitch
      //int h = abs(int(inc*5*level))*10;
      //if (inc>0) h = -h;
      //rect(playArea.x,playArea.y+playArea.h/2-1,4,2);
      //rect(playArea.x,playArea.y+playArea.h/2-h,2,h);
      //println("inc = "+inc);
      
      //up or down?
      inc = inc>0 ? 1 : -1;
      paddle.requestMove(paddle.destY+inc*_i("padspeed"));
    }
    if (_i("pitchMethod")==2) {
      println("level = "+level);
      int newpos = int(map(level,_i("sndlevelMin"),_i("sndlevelMax"),paddle.maxY,paddle.minY));
      paddle.requestMove(newpos);
    }
    
    //paddle.requestMove(pos);
    paddle.setLastUserInput();
  }
  

}

