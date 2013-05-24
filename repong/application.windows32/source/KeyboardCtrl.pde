class KeyboardCtrl extends BaseCtrl {

  char upKey;
  char downKey;
  
  KeyboardCtrl(char _upKey,char _downKey) {
    upKey = _upKey;
    downKey = _downKey;
  }

  void update(Paddle paddle) {
    if (keyPressed) {
      if (key == upKey) {
        paddle.requestMove(paddle.destY-paddle.inc);
        paddle.setLastUserInput();
      }
      if (key == downKey) {
        paddle.requestMove(paddle.destY+paddle.inc);
        paddle.setLastUserInput();
      }
    }
  }
  
}

