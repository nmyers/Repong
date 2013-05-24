public class Ball extends Rect {

  int vx = 0;
  int vy = 0;
  int pause = 0;

  Ball(int x, int y, int size) {
    super(x, y, size, size);
  }
  
  public void update() {
    pause = Math.max(0,pause-1);
    if (pause!=0) return;
    x += vx;
    y += vy;
  }
  
  void reset() {
    reset(0);
  }
  
  void reset(int dir) {
    float ppos = (dir < 0) ? 0.8 : 0.2;
    if (dir==0) {
      ppos = 0.5;
      dir = round(random(1))*2-1; 
    }
    x = playArea.x+int((playArea.w-h)*ppos);
    y = (playArea.h-h)/2;
    vx = dir*abs(vx);
    vy = 0;
    while (vy==0) {
      vy = (int) random(-3,3);
    }
  }
  
}

