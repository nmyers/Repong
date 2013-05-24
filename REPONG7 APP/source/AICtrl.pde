class AICtrl extends BaseCtrl {

  int counter;
  
  int cycle = 100;
  int pause = 10;
  float damp = 0.5;
  float precision    = 20; //in pixels
  float distToPaddle = 0;
  boolean backMiddle = true;
  
  boolean isActive = false;
  
  int inactivityThreshold;
  
  Ball ball;

  AICtrl(Ball _ball) {
    ball = _ball;
    counter = 0;
  }

  AICtrl(Ball _ball, int _cycle, int _pause, float _precision, float _damp,boolean _backMiddle) {
    cycle = _cycle;
    pause = _pause;
    damp  = _damp; 
    ball  = _ball;
    counter = 0;
    precision = _precision;
    backMiddle = _backMiddle;
    inactivityThreshold = _i("inactivityThreshold");
  }

  void update(Paddle paddle) {
    counter = (counter+1) % cycle;

    /// Starts AI if no activity
    // 
    if (paddle.getLastUserInputAge()>inactivityThreshold) {
      //Starts ai if none already in use
      if (activeAi==0) {
        activeAi = paddle.getLeftRight();
        paddle.isAi = true;
        paddle.damp = _f("padDamp");
      }
      //Cancel movement if not current ai
      if (activeAi != paddle.getLeftRight()) {
        return;
      }
    } else {
      //User moved this paddle -> remove ai if was in use
      if (activeAi == paddle.getLeftRight()) {
        activeAi = 0;
        paddle.isAi = false;
      }
      return;
    }
    

    /// The AI is "working"
    if (counter>pause) {

      /// Vertical distance between the ball and the paddle
      distToPaddle = (ball.y+ball.h/2)-(paddle.y+paddle.h/2);

      // Goes back to the center when its the other's move
      if (backMiddle) {
        if ((paddle.x > playArea.x+playArea.h/2) && (ball.vx<0)) {
          distToPaddle = (playArea.h/2)-(paddle.y+paddle.h/2);
        }
        if ((paddle.x < playArea.x+playArea.h/2) && (ball.vx>0)) {
          distToPaddle = (playArea.h/2)-(paddle.y+paddle.h/2);
        }
      }
            // Goes back to the center when its the other's move
      if (_b("aiBackBottom")) {
        if ((paddle.x > playArea.x+playArea.h/2) && (ball.vx<0)) {
          distToPaddle = (playArea.h)-(paddle.y+paddle.h/2);
        }
        if ((paddle.x < playArea.x+playArea.h/2) && (ball.vx>0)) {
          distToPaddle = (playArea.h)-(paddle.y+paddle.h/2);
        }
      }
    }

    if ( abs(distToPaddle) > precision ) {
      
      // Smaller movement if needed
      int incr = int(paddle.inc);
      if (abs(distToPaddle)<paddle.inc) incr = int(abs(distToPaddle));
      
      if (distToPaddle>0) {
        paddle.requestMove(paddle.destY+incr);
      } 
      else {
        paddle.requestMove(paddle.destY-incr);
      }
    }
  }
}

