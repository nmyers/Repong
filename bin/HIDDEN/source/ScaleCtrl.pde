class ScaleCtrl extends BaseCtrl {

  int serialIndex;
  int mode;
  
  int scaleCalibrateMax,scaleCalibrateMin;

  ScaleCtrl(int _serialIndex, int _mode) {
    serialIndex = _serialIndex;
    mode = _mode;
    scaleCalibrateMin = serialIndex==0 ? _i("scaleCalibrateLeftMin") :  _i("scaleCalibrateRightMin");
    scaleCalibrateMax = serialIndex==0 ? _i("scaleCalibrateLeftMax") :  _i("scaleCalibrateRightMax");
  }

  float roundValue(float value) {
    value = round(value*10)/10.0;
    return value;
  }
  
  void update(Paddle paddle) {
    float desty,angle,s_angle;
    int pulse,s_pulse;
    
    switch(mode) {
    case 0: 
      //scale
      pulse = serial.data[serialIndex];
      s_pulse = serial.smoothed_data[serialIndex];
      if (_b("scaleSmoothed")) {
        pulse = s_pulse;
      }
      int mapped = int(map(pulse,scaleCalibrateMin,scaleCalibrateMax,paddle.maxY,paddle.minY));
      println(serialIndex+" - "+s_pulse+" _ "+(s_pulse-scaleCalibrateMin));
      
      //No movement
      if ((s_pulse-scaleCalibrateMin)>_f("scaleThreshold")) {
        paddle.setLastUserInput(); 
        paddle.requestMove(mapped); 
      }
   
       
      break;
    case 1: 
              
      break;
    }
  }
  
}

