class ButtonCtrl extends BaseCtrl {

  int player = 0;
  int indexUp, indexDown;
  
  ButtonCtrl(int _player) {
    player = _player;
    if (player==0) {
      indexUp = 0;
      indexDown = 1;
    } else {
      indexUp = 2;
      indexDown = 3;
    }
  }

  void update(Paddle paddle) {
      
      if (serial.data[indexUp]==1) {
        paddle.requestMove(paddle.destY-paddle.inc);
        paddle.setLastUserInput();
      }
      
      if (serial.data[indexDown]==1) {  
        paddle.requestMove(paddle.destY+paddle.inc);
        paddle.setLastUserInput();
      }
      
      // TODO : Test Send score
      // Could move the update in the serial reader class
      //
  }
  
}

