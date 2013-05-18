public class Paddle extends Rect {

  int minY;
  int maxY;

  float destY, fy;
  float damp = 0.5;
  float inc  = 5;
  
  int maxBounceVYSkew = 12;
  
  private int lastUserInput;
  
  Boolean isUpdated = false;
  Boolean isAi = false;

  ArrayList controllers;

  Paddle(int _x, int _y, int _w, int _h) {
    super(_x, _y, _w, _h);
    lastUserInput = -10000;
    destY=y;
    setSize(_h);
    maxBounceVYSkew = _i("maxBounceVYSkew");
    inc = _i("paddleInc");
    controllers = new ArrayList();
  }

  void addController(BaseCtrl controller) {
    controllers.add(controller);
  }

  void setSize(int padsize) {
    h = padsize;
    minY = playArea.y;
    maxY = playArea.y+playArea.h-h;
  }

  void requestMove(float _y) {
    if (Float.isNaN(_y)) return;
    destY = constrain(_y, minY, maxY);
    isUpdated = true;
  }

  void update() { 
    // update the conntrollers
    isUpdated = false;
    for (int i = 0; i < controllers.size(); i++) { 
      BaseCtrl controller = (BaseCtrl) controllers.get(i);
      controller.update(this);
    }
    fy = fy+(destY-fy)*damp;
    y = (int) fy;
  }
  
  void draw() {
    super.draw();
    if (isAi) {
      //adds a visual feedback for ai controlled pad
      if (getLeftRight()==1) {
        //rect(x-4,y,2,h);
        
      } else {
        //rect(x+w+4,y,2,h);
      }
    }
  }

  boolean bounce(Ball ball) {
    //check if collision
    if (x < (ball.x+ball.w) && (x+w) > ball.x &&
      y < (ball.y+ball.h) && (y+h) > ball.y) {

      // Reverse X direction and bounce horizontally
      ball.vx = -ball.vx;
      ball.x += ball.vx;

      // Calculate the relative position where the ball hit from the paddle's center
      float hitPos = ball.y - (this.y+(this.h/2)-ball.h);

      // Calculate the percentage from the paddle's center that the ball hit  
      float percentFromCenter = hitPos/this.h/2;

      // Compute the amount to perturb the ball's VY
      int vyAdjust = round(percentFromCenter * maxBounceVYSkew);

      // Perturb VY based on the hit
      ball.vy += vyAdjust;

      return true;
    }
    return false;
  }
  
  void setLastUserInput() {
    lastUserInput = millis();
  }
  
  int getLastUserInputAge() {
    return millis()-lastUserInput;
  }
  
  int getLeftRight() {
    if (x<(playArea.x+playArea.h/2)) return -1;
    return 1;
  }
}

