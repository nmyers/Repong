

public class ScreenMessage {

  String message = "";
  int textHeight = _i("messageSize");
  float alpha = 0;
  int defaulttextHeight = textHeight;

  int animDuration = 100;
  int start = 0;
  int counter = 0;

  int step = 0;

  String animName;


  ScreenMessage() {
  }

  void start(String _animName) {
    //println("start :"+_animName);
    step = 0;
    counter = 0;
    animName = _animName;
  }

  void setDuration(int _animDuration) {
    animDuration = _animDuration;
    counter = 0;
    step++;
  }
  boolean stepOver() {
    return counter>animDuration;
  }


  void updateAnim() {
    if (animName.equals("fadeInOut")) fadeInOut();
    if (animName.equals("blink")) blink();
    if (animName.equals("fadeOut")) fadeOut();
    counter++;
  }
  
  void fadeOut() {
    int cstep = 0;
    int fadeSpeed = 10;
    //Init
    if (step==cstep++) {
      setDuration(fadeSpeed);
    }      
    //Fade out
    if (step==cstep++) {
      alpha = Math.max(0,alpha-fadeSpeed); // = 255-(counter/float(animDuration))*255;
      if (stepOver()) {
        //step++;
      }
    }
  }

  void fadeInOut() {
    int cstep = 0;
    int fadeSpeed = 10;
    int pause = 60;
    
    //Init
    if (step==cstep++) {
      setDuration(fadeSpeed);
      alpha = 0;
    }      
    //Fade in
    if (step==cstep++) {
      alpha = (counter/float(animDuration))*255;
      if (stepOver()) {
        setDuration(pause);
      }
    }
    //Wait
    if (step==cstep++) {
      if (stepOver()) {
        setDuration(fadeSpeed);
      }
    }
    //Fade out
    if (step==cstep++) {
      alpha = 255-(counter/float(animDuration))*255;
      if (stepOver()) {
        setDuration(pause);
      }
    }
    //Wait
    if (step==cstep++) {
      if (stepOver()) {
        step=0;
      }
    }
    //println("step = "+step);
    //println("animDuration = "+animDuration);
    //println("counter = "+counter);
  }

  void blink() {
    int cstep = 0;
    int blinkSpeed = 5;

    for (int i=0;i<6;i++) { 
      //Init
      if (step==cstep++) {
        setDuration(blinkSpeed);
        alpha = 255;
      }   
      //hide
      if (step==cstep++) {
        alpha = 255;
        if (stepOver()) {
          setDuration(blinkSpeed);
        }
      }
      //show
      if (step==cstep++) {
        alpha = 10;
        if (stepOver()) {
          setDuration(blinkSpeed);
        }
      }
    }
    
    if (step==cstep++) {
        alpha = 0;
        if (stepOver()) {
          setDuration(blinkSpeed);
        }
      }
      
  }
  
  void draw() {
    updateAnim();
    textAlign(CENTER);
    textSize(textHeight);
    
    /// MASK BEHIND THE TEXT

      fill(0,0,0, Math.min(50,alpha));
      float tw = textWidth(message);
      int th = textHeight;
      if (message.indexOf("\n")!=-1) {
        th = th*2;
      }
      rect(playArea.x+playArea.w/2-tw/2,playArea.y+playArea.h/2-textHeight*0.8,tw,th);
    
    
    fill(255, 255, 255, alpha);
    text(message, playArea.x+playArea.w/2, playArea.y+playArea.h/2);
    textSize(defaulttextHeight);
    fill(255);
  }
}

