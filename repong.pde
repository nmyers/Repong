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
import fullscreen.*; 

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
import ddf.minim.*;
Minim minim;
AudioInput microphone;

boolean isButtons = false;

void setup() {

  
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
  settings.put("padDamp",0.5);  

  settings.put("inactivityThreshold",2000);
  settings.put("aiCycle", 100);
  settings.put("aiPause", 10);
  settings.put("aiPrecision", 5);
  settings.put("aiDamp", 0.3);
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
}

// Main loop, switch to the current state
void draw() {
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
}

void updateGameState(int newGameState) {
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
void initIdleScreenLoop() {
  ball.reset();
  
  paddleL.lastUserInput = -10000;
  paddleR.lastUserInput = -10000;
  
  score.reset();
  screenMessage.start("fadeInOut");
  screenMessage.message = "READY ?";
}

/// IDLE STATE > WAIT FOR USER INPUT TO START THE GAME
//
void idleScreenLoop() {
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
void initGameLoop() {
  screenMessage.message = "GO!";
  screenMessage.alpha = 255;
  screenMessage.start("fadeOut");
  score.reset();
  ball.reset();
  ball.pause = _i("ballPauseGameStart");
}

/// MAIN GAME LOOP
//
void gameLoop() {
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

void initGameOverLoop() {
  screenMessage.start("fadeInOut");
  if (score.scoreR<score.scoreL) {
    screenMessage.message = "GAME OVER\nLEFT WINS";
  } else {
    screenMessage.message = "GAME OVER\nRIGHT WINS";
  }
  
}

/// "STATIC SCREEN" 
void gameOverLoop() {
  
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

int getLoopAge() {
  return millis()-startLoop;
}

// Draws the net in the middle of the screen
//
void drawNet() {
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
void loadSettings(String filename) {
  String lines[] = loadStrings(filename);
  println("there are " + lines.length + " lines");
  for (int i =0 ; i < lines.length; i++) {
    if (lines[i].length()==0) continue;
    if (lines[i].charAt(0)=='#') continue;
    String[] lineparts = trim(split(lines[i], '='));
    settings.put(lineparts[0], lineparts[1]);
  }
}

Boolean _b(String key) {
  return boolean(_(key));
}

int _i(String key) {
  return int(_(key));
}

float _f(String key) {
  return float(_(key));
}

String _(String key) {
  return settings.get(key).toString();
}

