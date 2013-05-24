public class Rect {

  public int x;
  public int y;
  public int w;
  public int h;

  Rect(int _x, int _y, int _w, int _h) {
    x=_x;
    y=_y;
    w=_w;
    h=_h;
  }

  void draw() {
    rect(x, y, w, h);
  }

  boolean bounce(Ball ball) {
    //check if collision
    if (x < (ball.x+ball.w) && (x+w) > ball.x &&
      y < (ball.y+ball.h) && (y+h) > ball.y) {
      //we assume 'wall' like collision, w >> h or h >> w
      if (w<h) {
        ball.vx = - ball.vx;
        ball.x += ball.vx;
      } 
      else {
        ball.vy = - ball.vy;
        ball.y += ball.vy;
      } 
      return true;
    }
    return false;
  }
}

