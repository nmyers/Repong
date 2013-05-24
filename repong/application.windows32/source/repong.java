import processing.core.*; 
import processing.xml.*; 

import fullscreen.*; 
import ddf.minim.*; 
import ddf.minim.*; 
import processing.serial.*; 
import hypermedia.net.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class repong extends PApplet {

/**
 * Re-Pong
 * 
 * Nicolas Myers - Dot Samsen
 ----------------  
 v0-3 
 |
     *               
           |
 ----------------
 
 
 */
 

FullScreen fs;
  
// Class reading values from the arduino
SerialReader serial;

// Game area and walls
Rect   playArea;
Rect   topWall, bottomWall, leftWall, rightWall;

// Game State
int gameState = 0;  //0 = idle screen

//Ball and Paddles
Ball   ball;
Paddle paddleL, paddleR;

//Message
ScreenMessage screenMessage;

//Settings that can be changed are stored in a hashmap
//Values are updated from a text file >> see loadSettings function
HashMap settings = new HashMap();

//Score object
Score  score;

//Loop age
int startLoop;


PFont font;

int activeAi = 0; // -1 for left ; 1 for right ; 0 for none


//For the sound controller

Minim minim;
AudioInput microphone;

boolean isButtons = false;

public void setup() {

  
  //smooth();

  /// Initial settings are overwritten by the ones in the text file
  /// Load settings from external file
  //
  settings.put("appWidth", 600);
  settings.put("appHeight", 400);
  settings.put("frameRate", 100); 
  
  settings.put("gameWidth", 600);
  settings.put("gameHeight", 400);
  settings.put("game_x0", 0);
  settings.put("game_y0", 0);

  settings.put("objSize", 10);
  settings.put("paddleSize", 20);
  settings.put("netSize", 3);

  settings.put("ballSpeed", 2);
  settings.put("maxScore", 10);
  settings.put("startGameActivityThreshold", 500);

  settings.put("padControllers", "keyboard,ai");
  settings.put("padDamp",0.5f);  

  settings.put("inactivityThreshold",2000);
  settings.put("aiCycle", 100);
  settings.put("aiPause", 10);
  settings.put("aiPrecision", 5);
  settings.put("aiDamp", 0.3f);
  settings.put("aiBackMiddle", true);

  settings.put("serialBufferLength",10);
  settings.put("serialPort", 0);
  settings.put("serialSpeed", 115200);
  settings.put("serialNbReading", 2);

  loadSettings("settings.ini");

  screenMessage = new ScreenMessage();

  /// Initialize serial reading to get values from arduino
  serial = new SerialReader(this, _i("serialPort"), _i("serialSpeed"), _i("serialNbReading"),_i("serialBufferLength"));

  /// App size and Game area
  size(_i("appWidth"), _i("appHeight"));
  playArea = new Rect(_i("game_x0"), _i("game_y0"), _i("gameWidth"), _i("gameHeight"));

  int paddleSize = _i("paddleSize");
  int objSize = _i("objSize");

  /// Walls
  topWall    = new Rect(playArea.x, playArea.y-objSize, playArea.w, objSize);
  bottomWall = new Rect(playArea.x, playArea.y+playArea.h, playArea.w, objSize);
  rightWall  = new Rect(playArea.x+playArea.w, playArea.y, objSize, playArea.h);
  leftWall   = new Rect(playArea.x-objSize, playArea.y, objSize, playArea.h);

  /// Ball
  ball = new Ball(40, 15, objSize);
  ball.vx = _i("ballSpeed");

  /// Controllers and Paddles
  paddleL = new Paddle(playArea.x, 100, objSize, paddleSize);
  paddleL.damp = _f("padDamp");
  paddleR = new Paddle(playArea.x+playArea.w-objSize, 100, objSize, paddleSize);
  paddleR.damp = _f("padDamp");

  String[] controllers = trim(split(_("padControllers"), ","));
  for (int i=0;i<controllers.length;i++) {
    println("-"+controllers[i]+"-");
    if (controllers[i].equals("keyboard")) {
      println("Adding Keyboard controller");
      paddleL.addController(new KeyboardCtrl('q', 'a'));
      paddleR.addController(new KeyboardCtrl('w', 's'));
    } 
    if (controllers[i].equals("tilt")) {
      println("Adding Tilt controller");
      paddleL.addController(new TiltCtrl(0, 0));
      paddleR.addController(new TiltCtrl(1, 0));
    } 
    if (controllers[i].equals("connected")) {
      println("Adding Tilt controller (connected)");
      paddleL.addController(new TiltCtrl(0, 1));
      paddleR.addController(new TiltCtrl(1, 1));
    } 
    if (controllers[i].equals("scale")) {
      println("Adding Scale controller");
      paddleL.addController(new ScaleCtrl(1, 0));
      paddleR.addController(new ScaleCtrl(0, 0));
    } 
    if (controllers[i].equals("buttons")) {
      println("Adding Button controller");
      isButtons = true;
      paddleL.addController(new ButtonCtrl(0));
      paddleR.addController(new ButtonCtrl(1));
    }
    if (controllers[i].equals("pitch")) {
      println("Adding Pitch controller");
      // Sound input used by the sound controllers
      minim = new Minim(this);
      microphone = minim.getLineIn(Minim.STEREO, 512*4);
      paddleL.addController(new PitchCtrl(0, this));
      paddleR.addController(new PitchCtrl(1, this));
    }    
    if (controllers[i].equals("ai")) {
      println("Adding AI controller");
      paddleL.addController(new AICtrl(ball, _i("aiCycle"), _i("aiPause"), _f("aiPrecision"), _f("aiDamp"), _b("aiBackMiddle")));
      paddleR.addController(new AICtrl(ball, _i("aiCycle"), _i("aiPause"), _f("aiPrecision"), _f("aiDamp"), _b("aiBackMiddle")));
    }
  }

  /// Score
  score = new Score();  

  /// Font
 // font = loadFont("SquareFont-48.vlw"); 
  font = loadFont("SFSquareHead-200.vlw"); 
  
  textFont(font, 200);

  noStroke();
  frameRate(_i("frameRate"));

  updateGameState(0);
  
  noCursor();
  
  
  // Create the fullscreen object
  if (_b("isFullScreen")) {
    fs = new FullScreen(this);
    fs.enter();
  }
  background(0);
  
  setupTimer();
}

// Main loop, switch to the current state
public void draw() {
  noCursor();
  fill(0,0,0,150);
  rect(0,0,width,height);
  
  if (_b("showBoundaries")) {
    fill(150);
    rect(playArea.x,playArea.y,playArea.w,playArea.h);
  }
  
  fill(255);
  
  switch(gameState) {
  case 0: 
    idleScreenLoop();
    break;
  case 1: 
    gameLoop();
    break;
  case 2: 
    gameOverLoop();
    break;
  }
  updateTimerStatus();
}

public void updateGameState(int newGameState) {
  gameState = newGameState;
  startLoop = millis();
  switch(gameState) {
  case 0: 
    initIdleScreenLoop();
    break;
  case 1: 
    initGameLoop();
    break;
  case 2: 
    initGameOverLoop();
    break;
  }
}


/// RESET THE SCREEN AND PREPARES FOR IDLE SCREEN
//
public void initIdleScreenLoop() {
  ball.reset();
  
  paddleL.lastUserInput = -10000;
  paddleR.lastUserInput = -10000;
  
  score.reset();
  screenMessage.start("fadeInOut");
  screenMessage.message = "READY ?";
}

/// IDLE STATE > WAIT FOR USER INPUT TO START THE GAME
//
public void idleScreenLoop() {
  serial.update();

  // Update paddles and ball
  paddleL.update();
  paddleR.update();
  ball.update();

  /// Check for collisions
  topWall.bounce(ball);
  bottomWall.bounce(ball);
  rightWall.bounce(ball);
  leftWall.bounce(ball);

  // If there's activity -> start the game
  if ((paddleL.getLastUserInputAge()<_i("inactivityThreshold")) || (paddleR.getLastUserInputAge()<_i("inactivityThreshold"))) {
    updateGameState(1);
  }

  // Draw objects
  ball.draw();
  score.draw();
  
  drawNet();
  
  screenMessage.draw();
}

/// RESET THE SCORE BEFORE STARTING THE GAME
//
public void initGameLoop() {
  screenMessage.message = "GO!";
  screenMessage.alpha = 255;
  screenMessage.start("fadeOut");
  score.reset();
  ball.reset();
  ball.pause = _i("ballPauseGameStart");
}

/// MAIN GAME LOOP
//
public void gameLoop() {
  serial.update();

  // Update paddles and ball
  paddleL.update();
  paddleR.update();
  ball.update();
  

  /// Check for collisions and update score
  topWall.bounce(ball);
  bottomWall.bounce(ball);
  paddleL.bounce(ball);
  paddleR.bounce(ball);

  if (rightWall.bounce(ball)) {
    ball.reset(1);
    ball.pause = _i("ballPauseGameNewBall");
    score.addScore(0);
  }
  if (leftWall.bounce(ball)) {
    ball.reset(-1);
    ball.pause = _i("ballPauseGameNewBall");
    score.addScore(1);
  }

  if (score.scoreR==_i("maxScore") || score.scoreL==_i("maxScore")) {
    updateGameState(2);
  }

  /// Draw objects
  ball.draw();
  paddleL.draw(); 
  paddleR.draw();    
  score.draw();
  drawNet();
  
  screenMessage.draw();
  
}

public void initGameOverLoop() {
  screenMessage.start("fadeInOut");
  if (score.scoreR<score.scoreL) {
    screenMessage.message = "GAME OVER\nLEFT WINS";
  } else {
    screenMessage.message = "GAME OVER\nRIGHT WINS";
  }
  
}

/// "STATIC SCREEN" 
public void gameOverLoop() {
  
  if (getLoopAge()>6000) {
    updateGameState(0);
  }
  
  /// Draw objects
  paddleL.draw(); 
  paddleR.draw();    
  score.draw();
  drawNet();
  
  screenMessage.draw();
  
}

public int getLoopAge() {
  return millis()-startLoop;
}

// Draws the net in the middle of the screen
//
public void drawNet() {
  int netSize = _i("netSize");
  int dash = netSize * 3;
  int space = netSize * 2;
  int posx = playArea.x+(playArea.w-netSize)/2;
  int steps = ceil((playArea.h)/(dash+space))+1-_i("removeNetBottomDash");
  for (int i=0;i<steps;i++) {
    rect(posx, playArea.y+i*(dash+space), netSize, dash);
  }
}



/// SETTINGS FUNCTIONS
/// ---------------------------------------------------------

/// Simple settings loading
//
public void loadSettings(String filename) {
  String lines[] = loadStrings(filename);
  println("there are " + lines.length + " lines");
  for (int i =0 ; i < lines.length; i++) {
    if (lines[i].length()==0) continue;
    if (lines[i].charAt(0)=='#') continue;
    String[] lineparts = trim(split(lines[i], '='));
    settings.put(lineparts[0], lineparts[1]);
  }
}

public Boolean _b(String key) {
  return PApplet.parseBoolean(_(key));
}

public int _i(String key) {
  return PApplet.parseInt(_(key));
}

public float _f(String key) {
  return PApplet.parseFloat(_(key));
}

public String _(String key) {
  return settings.get(key).toString();
}

class AICtrl extends BaseCtrl {

  int counter;
  
  int cycle = 100;
  int pause = 10;
  float damp = 0.5f;
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

  public void update(Paddle paddle) {
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
      int incr = PApplet.parseInt(paddle.inc);
      if (abs(distToPaddle)<paddle.inc) incr = PApplet.parseInt(abs(distToPaddle));
      
      if (distToPaddle>0) {
        paddle.requestMove(paddle.destY+incr);
      } 
      else {
        paddle.requestMove(paddle.destY-incr);
      }
    }
  }
}

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
  
  public void reset() {
    reset(0);
  }
  
  public void reset(int dir) {
    float ppos = (dir < 0) ? 0.8f : 0.2f;
    if (dir==0) {
      ppos = 0.5f;
      dir = round(random(1))*2-1; 
    }
    x = playArea.x+PApplet.parseInt((playArea.w-h)*ppos);
    y = (playArea.h-h)/2;
    vx = dir*abs(vx);
    vy = 0;
    while (vy==0) {
      vy = (int) random(-3,3);
    }
  }
  
}

class BaseCtrl {
  
  BaseCtrl() {
    
  }

  public void update(Paddle paddle) {
    //paddle.requestMove();
  }
  
}

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

  public void update(Paddle paddle) {
      
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

class KeyboardCtrl extends BaseCtrl {

  char upKey;
  char downKey;
  
  KeyboardCtrl(char _upKey,char _downKey) {
    upKey = _upKey;
    downKey = _downKey;
  }

  public void update(Paddle paddle) {
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

public class Paddle extends Rect {

  int minY;
  int maxY;

  float destY, fy;
  float damp = 0.5f;
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

  public void addController(BaseCtrl controller) {
    controllers.add(controller);
  }

  public void setSize(int padsize) {
    h = padsize;
    minY = playArea.y;
    maxY = playArea.y+playArea.h-h;
  }

  public void requestMove(float _y) {
    if (Float.isNaN(_y)) return;
    destY = constrain(_y, minY, maxY);
    isUpdated = true;
  }

  public void update() { 
    // update the conntrollers
    isUpdated = false;
    for (int i = 0; i < controllers.size(); i++) { 
      BaseCtrl controller = (BaseCtrl) controllers.get(i);
      controller.update(this);
    }
    fy = fy+(destY-fy)*damp;
    y = (int) fy;
  }
  
  public void draw() {
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

  public boolean bounce(Ball ball) {
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
  
  public void setLastUserInput() {
    lastUserInput = millis();
  }
  
  public int getLastUserInputAge() {
    return millis()-lastUserInput;
  }
  
  public int getLeftRight() {
    if (x<(playArea.x+playArea.h/2)) return -1;
    return 1;
  }
}

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

  public void update(Paddle paddle) {
    
    if (channel==0) {
      pitchDetector.getPitch(microphone.left.toArray());
    } else {
      pitchDetector.getPitch(microphone.right.toArray());
    }
      
    float pitch = round(pitchDetector.GetFrequency());
    
    if (_b("pitchAdjust")) {
      if (pitch>0 && pitchDetector.probability>0.85f) {
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
      int newpos = PApplet.parseInt(map(level,_i("sndlevelMin"),_i("sndlevelMax"),paddle.maxY,paddle.minY));
      paddle.requestMove(newpos);
    }
    
    //paddle.requestMove(pos);
    paddle.setLastUserInput();
  }
  

}


/**
 * An implementation of the AUBIO_YIN pitch tracking algorithm. See <a href=
 * "http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf"
 * >the YIN paper.</a> Implementation based on <a
 * href="http://aubio.org">aubio</a>
 * 
 * @author Joren Six
 * @author Paul Brossier
 */
 


public class PitchDetectorYin implements AudioListener {
  
        public float pitchInHertz;
        public float probability;
        boolean isPitched = false;
        
        long t;
        
	/**
	 * The default YIN threshold value. Should be around 0.10~0.15. See YIN
	 * paper for more information.
	 */
	private static final double DEFAULT_THRESHOLD = 0.20f;

	/**
	 * The default size of an audio buffer (in samples).
	 */
	public static final int DEFAULT_BUFFER_SIZE = 2048;

	/**
	 * The default overlap of two consecutive audio buffers (in samples).
	 */
	public static final int DEFAULT_OVERLAP = 1536;

	/**
	 * The actual YIN threshold.
	 */
	private final double threshold;

	/**
	 * The audio sample rate. Most audio has a sample rate of 44.1kHz.
	 */
	private final float sampleRate;

	/**
	 * The buffer that stores the calculated values. It is exactly half the size
	 * of the input buffer.
	 */
	private final float[] yinBuffer;
	

        private int channel;

        public synchronized void samples(float[] samp) {
         // getPitch(samp);
          t++;
        }
        
        public synchronized void samples(float[] sampL, float[] sampR) {
         /* if (channel==0) {
            getPitch(sampL);
          } else {
            getPitch(sampR);
          }
          */
          t++;
        }
        
        public synchronized long GetTime() {
          return t;
        }
        
        public synchronized float GetFrequency() {
          return pitchInHertz;
        }
        
        public void setChannel(int _channel) {
          channel=_channel;
        }

	/**
	 * Create a new pitch detector for a stream with the defined sample rate.
	 * Processes the audio in blocks of the defined size.
	 * 
	 * @param audioSampleRate
	 *            The sample rate of the audio stream. E.g. 44.1 kHz.
	 * @param bufferSize
	 *            The size of a buffer. E.g. 1024.
	 */
	public PitchDetectorYin(final float audioSampleRate, final int bufferSize) {
		this(audioSampleRate, bufferSize, DEFAULT_THRESHOLD);
	}

	/**
	 * Create a new pitch detector for a stream with the defined sample rate.
	 * Processes the audio in blocks of the defined size.
	 * 
	 * @param audioSampleRate
	 *            The sample rate of the audio stream. E.g. 44.1 kHz.
	 * @param bufferSize
	 *            The size of a buffer. E.g. 1024.
	 * @param yinThreshold
	 *            The parameter that defines which peaks are kept as possible
	 *            pitch candidates. See the YIN paper for more details.
	 */
	public  PitchDetectorYin(final float audioSampleRate, final int bufferSize, final double yinThreshold) {
		this.sampleRate = audioSampleRate;
		this.threshold = yinThreshold;
		yinBuffer = new float[bufferSize / 2];
	}

	/**
	 * The main flow of the YIN algorithm. Returns a pitch value in Hz or -1 if
	 * no pitch is detected.
	 * 
	 * @return a pitch value in Hz or -1 if no pitch is detected.
	 */
	public float getPitch(final float[] audioBuffer) {

		final int tauEstimate;
		
		// step 2
		difference(audioBuffer);

		// step 3
		cumulativeMeanNormalizedDifference();

		// step 4
		tauEstimate = absoluteThreshold();

		// step 5
		if (tauEstimate != -1) {
			final float betterTau = parabolicInterpolation(tauEstimate);

			// step 6
			// TODO Implement optimization for the AUBIO_YIN algorithm.
			// 0.77% => 0.5% error rate,
			// using the data of the YIN paper
			// bestLocalEstimate()

			// conversion to Hz
			pitchInHertz = sampleRate / betterTau;
		} else {
			// no pitch found
			pitchInHertz = -1;
		}
		
		return pitchInHertz;
	}

	/**
	 * Implements the difference function as described in step 2 of the YIN
	 * paper.
	 */
	private void difference(final float[] audioBuffer) {
		int index, tau;
		float delta;
		for (tau = 0; tau < yinBuffer.length; tau++) {
			yinBuffer[tau] = 0;
		}
		for (tau = 1; tau < yinBuffer.length; tau++) {
			for (index = 0; index < yinBuffer.length; index++) {
				delta = audioBuffer[index] - audioBuffer[index + tau];
				yinBuffer[tau] += delta * delta;
			}
		}
	}

	/**
	 * The cumulative mean normalized difference function as described in step 3
	 * of the YIN paper. <br>
	 * <code>
	 * yinBuffer[0] == yinBuffer[1] = 1
	 * </code>
	 */
	private void cumulativeMeanNormalizedDifference() {
		int tau;
		yinBuffer[0] = 1;
		float runningSum = 0;
		for (tau = 1; tau < yinBuffer.length; tau++) {
			runningSum += yinBuffer[tau];
			yinBuffer[tau] *= tau / runningSum;
		}
	}

	/**
	 * Implements step 4 of the AUBIO_YIN paper.
	 */
	private int absoluteThreshold() {
		// Uses another loop construct
		// than the AUBIO implementation
		int tau;
		// first two positions in yinBuffer are always 1
		// So start at the third (index 2)
		for (tau = 2; tau < yinBuffer.length; tau++) {
			if (yinBuffer[tau] < threshold) {
				while (tau + 1 < yinBuffer.length && yinBuffer[tau + 1] < yinBuffer[tau]) {
					tau++;
				}
				// found tau, exit loop and return
				// store the probability
				// From the YIN paper: The threshold determines the list of
				// candidates admitted to the set, and can be interpreted as the
				// proportion of aperiodic power tolerated
				// within a periodic signal.
				//
				// Since we want the periodicity and and not aperiodicity:
				// periodicity = 1 - aperiodicity
                                probability = 1 - yinBuffer[tau];
				break;
			}
		}

		
		// if no pitch found, tau => -1
		if (tau == yinBuffer.length || yinBuffer[tau] >= threshold) {
			tau = -1;
                        isPitched = false;
                        probability = 0;
		} else {
                        isPitched = true;
		}

		return tau;
	}

	/**
	 * Implements step 5 of the AUBIO_YIN paper. It refines the estimated tau
	 * value using parabolic interpolation. This is needed to detect higher
	 * frequencies more precisely. See http://fizyka.umk.pl/nrbook/c10-2.pdf and
	 * for more background
	 * http://fedc.wiwi.hu-berlin.de/xplore/tutorials/xegbohtmlnode62.html
	 * 
	 * @param tauEstimate
	 *            The estimated tau value.
	 * @return A better, more precise tau value.
	 */
	private float parabolicInterpolation(final int tauEstimate) {
		final float betterTau;
		final int x0;
		final int x2;

		if (tauEstimate < 1) {
			x0 = tauEstimate;
		} else {
			x0 = tauEstimate - 1;
		}
		if (tauEstimate + 1 < yinBuffer.length) {
			x2 = tauEstimate + 1;
		} else {
			x2 = tauEstimate;
		}
		if (x0 == tauEstimate) {
			if (yinBuffer[tauEstimate] <= yinBuffer[x2]) {
				betterTau = tauEstimate;
			} else {
				betterTau = x2;
			}
		} else if (x2 == tauEstimate) {
			if (yinBuffer[tauEstimate] <= yinBuffer[x0]) {
				betterTau = tauEstimate;
			} else {
				betterTau = x0;
			}
		} else {
			float s0, s1, s2;
			s0 = yinBuffer[x0];
			s1 = yinBuffer[tauEstimate];
			s2 = yinBuffer[x2];
			// fixed AUBIO implementation, thanks to Karl Helgason:
			// (2.0f * s1 - s2 - s0) was incorrectly multiplied with -1
			betterTau = tauEstimate + (s2 - s0) / (2 * (2 * s1 - s2 - s0));
		}
		return betterTau;
	}
}
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

  public void draw() {
    rect(x, y, w, h);
  }

  public boolean bounce(Ball ball) {
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

  public float roundValue(float value) {
    value = round(value*10)/10.0f;
    return value;
  }
  
  public void update(Paddle paddle) {
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
      int mapped = PApplet.parseInt(map(pulse,scaleCalibrateMin,scaleCalibrateMax,paddle.maxY,paddle.minY));
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

public class Score {

  int scoreL;
  int scoreR;
  
  int startBallTime = 0;
  float bestTime = 1.0f;
  
  float[][] bestTimes;

  Score() {
    readBestTimes();
    
  }
    
  public void draw() {
    textSize(_i("scoreSize"));
    textAlign(CENTER);
    text(scoreL, playArea.x+playArea.w/4, playArea.y+_i("scoreSize"));    
    text(scoreR, playArea.x+3*playArea.w/4, playArea.y+_i("scoreSize"));
    if (activeAi!=0 && gameState==1 && (millis()%_i("aiLabelBlinkSpeed")>_i("aiLabelBlinkSpeed")/2)) {
       textAlign(LEFT);
       textSize(_i("aiLabelSize"));
       if (activeAi==1) text("AI",playArea.x+3*playArea.w/4+_i("aiLabelDx"), playArea.y+_i("aiLabelDy"));
       if (activeAi==-1) text("AI",playArea.x+playArea.w/4+_i("aiLabelDx"), playArea.y+_i("aiLabelDy"));
       textSize(_i("scoreSize"));
    }
    
    textSize(_i("scoreTimeSize"));
    //println((millis()-startBallTime)/1000.0);
    float lapse = (millis()-startBallTime)/1000.0f;
    
    if (gameState!=1) lapse = 0.0f;
    textAlign(RIGHT);
    text(nf(lapse,1,1)+"  :",playArea.x+playArea.w/2+_i("scoreTimeDx"),playArea.h+playArea.y-_i("scoreTimeSize")+_i("scoreTimeDy"));
    
   
    
    textAlign(LEFT);
    text("  "+nf(bestTime,1,1),playArea.x+playArea.w/2+_i("scoreTimeDx"),playArea.h+playArea.y-_i("scoreTimeSize")+_i("scoreTimeDy"));
    
    textAlign(CENTER);
  }
  
  
  public void readBestTimes() {
    Date d = new Date();
    long current=d.getTime()/1000;
    String lines[] = loadStrings("best_times.txt");
    println("there are " + lines.length + " lines");
    int k=0;
    bestTimes = new float[lines.length][2];
    for (int i =0 ; i < lines.length; i++) {
      if (lines[i].length()==0) continue;
      if (lines[i].charAt(0)=='#') continue;
      String[] lineparts = trim(split(lines[i], ':'));
      if (lineparts.length!=2) continue; 
      println(lineparts[1]);
      bestTimes[k][0] = PApplet.parseFloat(lineparts[0]);
      bestTimes[k][1] = PApplet.parseFloat(lineparts[1]);
      k++;
    }
    for (int j=0;j<bestTimes.length;j++){
      if (daysAgo(PApplet.parseInt(bestTimes[j][0]))<_i("bestTime_since")) {
        bestTime = max(bestTime,bestTimes[j][1]);
      }
    }
  }
  
  public void writeBestTimes() {
    Date d = new Date();
    long current=d.getTime()/1000;
    String[] scores = new String[bestTimes.length+1];
    for (int j=0;j<bestTimes.length;j++){
      scores[j] = PApplet.parseInt(bestTimes[j][0])+":"+bestTimes[j][1];
    }
    scores[bestTimes.length] = PApplet.parseInt(current)+":"+nf(bestTime,1,1);
    saveStrings("best_times.txt", scores);
  }
  
  public int daysAgo(int timestamp) {
    Date d = new Date();
    long current=d.getTime()/1000;
    return PApplet.parseInt((current-timestamp)/(60*60*24));
  }
  
  public void setScore(int score,int player) {
    if (player==0) {
      scoreL = score;
    } else {
      scoreR = score;
    }
    float thisTime = (millis()-startBallTime)/1000.0f;
    if (thisTime>bestTime) {
      //We have a best time!
      screenMessage.start("blink");
      screenMessage.message = "BEST TIME\n"+nf(thisTime,1,1)+"s";
      bestTime = thisTime;
      writeBestTimes();
    }
    startBallTime = millis();
    ball.pause = _i("ballPauseBestTime");
  }
  
  public void addScore(int player) {
    if (player==0) {
      setScore(scoreL+1,0);
      if (isButtons && scoreL<11) {
        serial.myPort.write(score.scoreL);
      }
      //
    } else {
      setScore(scoreR+1,1);
      if (isButtons && scoreR<11) {
        serial.myPort.write(score.scoreR+20);
      }
    }
  }
  
  public void reset() {
    
    scoreL = scoreR = 0;
    if (isButtons) {
        serial.myPort.write(score.scoreL);
        serial.myPort.write(score.scoreR+20);
      }
    startBallTime = millis();
  }
}



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

  public void start(String _animName) {
    //println("start :"+_animName);
    step = 0;
    counter = 0;
    animName = _animName;
  }

  public void setDuration(int _animDuration) {
    animDuration = _animDuration;
    counter = 0;
    step++;
  }
  public boolean stepOver() {
    return counter>animDuration;
  }


  public void updateAnim() {
    if (animName.equals("fadeInOut")) fadeInOut();
    if (animName.equals("blink")) blink();
    if (animName.equals("fadeOut")) fadeOut();
    counter++;
  }
  
  public void fadeOut() {
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

  public void fadeInOut() {
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
      alpha = (counter/PApplet.parseFloat(animDuration))*255;
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
      alpha = 255-(counter/PApplet.parseFloat(animDuration))*255;
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

  public void blink() {
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
  
  public void draw() {
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
      rect(playArea.x+playArea.w/2-tw/2,playArea.y+playArea.h/2-textHeight*0.8f,tw,th);
    
    
    fill(255, 255, 255, alpha);
    text(message, playArea.x+playArea.w/2, playArea.y+playArea.h/2);
    textSize(defaulttextHeight);
    fill(255);
  }
}



public class SerialReader {

  public Serial  myPort;
  public  int[]   data;
  public  int[]   smoothed_data;
  public  int[][] serialBuffer;
  private int serialBufferIndex;
  
  private int count;
  
  private String  rawData;
  private boolean noSerial;
  
  private int nValues;

  SerialReader(PApplet parent, int _portNo, int _portSpeed, int _nValues, int _bufferLength) {
    nValues = _nValues;
    data   = new int[_nValues];    
    smoothed_data = new int[_nValues];
    serialBuffer = new int[_nValues][_bufferLength];
    serialBufferIndex = 0;
    count = 0;
    noSerial = false;
    try {
      myPort = new Serial(parent, Serial.list()[_portNo], _portSpeed);
      println("Added Serial on > "+Serial.list()[_portNo]); 
    } catch (Exception e) {
     println("Can't connect to serial port. Serial value while output 0"); 
     noSerial = true;
    }
    for (int i=0;i<nValues;i++) {
       data[i] = 0;
       smoothed_data[i] = 0;    
    }
    
  }

  public void update() {
    if (noSerial) {
      data[0]=0;
      data[1]=0;
      return;
    }
    while (myPort.available() > 0) {
      char c = myPort.readChar();
      if (c!='\n') {
        rawData += c;
      } else {
        int[] tmpdata;
        try {
          tmpdata = PApplet.parseInt(split(rawData, ','));
        } catch (Exception e) {
          rawData = "";
          return;
        }
        
        //Error in reading > skip
        if (tmpdata.length!=nValues) {
          rawData = "";
          return;
        }
        
        data = tmpdata;
        
        
        //no smoothing
        if (_("serialSmoothing").equals("none")) {
          for (int i=0;i<nValues;i++) {
             smoothed_data[i] = data[i];    
          }
        }
        
        //Smooothing
        if (_("serialSmoothing").equals("median")) {
          for (int i=0;i<nValues;i++) {
            try {
              serialBuffer[i][serialBufferIndex] = data[i];          
              int[] sorted = sort(serialBuffer[i]);
              smoothed_data[i] = sorted[round(sorted.length/2)];
            } catch (Exception e) {
              smoothed_data[i] = 0; // data[i];
            }
          }
          serialBufferIndex++;
          serialBufferIndex = serialBufferIndex % serialBuffer[0].length;
        }
        
        //Smooothing
        if (_("serialSmoothing").equals("runningaverage")) {
          for (int i=0;i<nValues;i++) {
            try {
              serialBuffer[i][serialBufferIndex] = data[i];
              int sum = 0;   
              for (int j=0;j<serialBuffer[i].length;j++) {
                sum += serialBuffer[i][j];
              }
              smoothed_data[i] = sum/serialBuffer[i].length;
            } catch (Exception e) {
              smoothed_data[i] = data[i];
            }
          }
          serialBufferIndex++;
          serialBufferIndex = serialBufferIndex % serialBuffer[0].length;
        }        
        
        println("serial reading raw ="+rawData);
        rawData = "";
      }
    }
  }
}

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

  public float roundValue(float value) {
    value = round(value*10)/10.0f;
    return value;
  }
  
  public void update(Paddle paddle) {
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
      int mapped = PApplet.parseInt(map(pulse,connectedCalibrateMin,connectedCalibrateMax,paddle.maxY,paddle.minY));
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
  public float pulseToAngle(int _pulse) {
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


int childPort = 9999;
int masterPort = 12345;
UDP udp;
int timerInterval = 1000;
long lastTimerMillis = millis();


public void setupTimer(){
  
    udp = new UDP( this, childPort); //listerning port
  udp.listen( true );
  udp.send( "started", "localhost", masterPort);
}

public void receive( byte[] data, String ip, int port ) {  // <-- extended handler
  String message = new String( data );
  //println( "receive: \""+message+"\" from "+ip+" on port "+port );
  decodeTimer(message);
}


public void decodeTimer(String m) {
  if (m.equals("bang")) {
    udp.send( "exiting", "localhost", masterPort);
    println("got kill command, exiting"); 
    exit();
  }
}

public void updateTimerStatus() {
  if (millis() - lastTimerMillis > timerInterval) {
    lastTimerMillis = millis(); 
    udp.send( "running", "localhost", masterPort);
  }
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "repong" });
  }
}
