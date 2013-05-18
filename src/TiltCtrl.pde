class TiltCtrl extends BaseCtrl {

  int serialIndex;
  int mode;
  float calibrateAngle;
  int connectedCalibrateMid,connectedCalibrateMax,connectedCalibrateMin;

  TiltCtrl(int _serialIndex, int _mode) {
    serialIndex = _serialIndex;
    mode = _mode;
    calibrateAngle = serialIndex==0 ? _f("tiltCalibrateLeft") :  _f("tiltCalibrateRight");
    connectedCalibrateMid = serialIndex==0 ? _i("connectCalibrateLeftMid") :  _i("connectCalibrateRightMid");
    connectedCalibrateMin = serialIndex==0 ? _i("connectCalibrateLeftMin") :  _i("connectCalibrateRightMin");
    connectedCalibrateMax = serialIndex==0 ? _i("connectCalibrateLeftMax") :  _i("connectCalibrateRightMax");
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
      //Spirit level
      angle = roundValue(pulseToAngle(serial.data[serialIndex])-calibrateAngle);
      s_angle = roundValue(pulseToAngle(serial.smoothed_data[serialIndex])-calibrateAngle); 
      if (_b("tiltSmoothed")) {
        angle = s_angle;
      }
      println(serialIndex+" - angle "+abs(angle));
      //No movement
      if (abs(s_angle)<_f("tiltAngleThreshold")) return;
      desty = map(angle, -_i("tiltMinAngle"), -_i("tiltMaxAngle"), -_i("tiltAmplitude"), _i("tiltAmplitude"));
      paddle.requestMove(paddle.destY+desty);
      paddle.setLastUserInput();
      break;
    case 1: 
      //Connected
      pulse = serial.data[serialIndex]-connectedCalibrateMid;
      s_pulse = serial.smoothed_data[serialIndex]-connectedCalibrateMid;
      if (_b("connectedSmoothed")) {
        pulse = s_pulse;
      }
      int mapped = int(map(pulse,connectedCalibrateMin,connectedCalibrateMax,paddle.maxY,paddle.minY));
      //println(serialIndex+"connected - "+pulse+" _ "+mapped);
      //movement detecte
      if (abs(s_pulse)>_f("connectedMovementThreshold")) {
        //println("moved "+s_pulse);
        paddle.setLastUserInput(); 
      } 
      //no ai for connected so this is fine
      paddle.requestMove(mapped);          
      break;
    }
  }
  
  /// Decodes the pulse from the arduino and converts it into an angle in degrees
  //
  float pulseToAngle(int _pulse) {
    float accelerationX = ((_pulse / 10) - 500) * 8;
    float tiltX = asin(accelerationX/1000);
    tiltX *= (360/(2*PI));
    tiltX = map(tiltX, -74, 74, -90, 90);
    if (tiltX < -90) {
      tiltX = -90;
    }
    else if (tiltX > 90) {
      tiltX = 90;
    }
    return min(90,max(-90,tiltX));
  }
}

