// timer app, start child app when launch || at 11 AM, 
// kill child at 9PM
// comminucate with child via UDP

import hypermedia.net.*;
int childPort = 9999;
int masterPort = 12345;
int hh, mm, ss;
boolean childRunning = true;
boolean childGotKillCommand = false;
String childLocation = "/Users/dotmancando/Desktop/childTimer.app";
UDP udp;
int runningTimer = 5*1000; // if doenst get "running" command in 10 sec, consider not running
long lastTimerMillis = millis();
int startHour = 11;
int shutDownHour = 21;
boolean supposedState = true;

void setup() {
  size(200,100);
  udp = new UDP( this, masterPort); //listerning port
  udp.listen( true );
}


void draw() {
  background(255);
  hh = hour();
  mm = minute();
  
  if ( hh >= shutDownHour ) { // kill time
    supposedState = false;
    if (childRunning && !childGotKillCommand) {
      //send kill command 
      println("send kill command");
      udp.send( "bang", "localhost", childPort);
    }
  }

  if ( hh >= startHour && hh < shutDownHour) { // attemp to start app 
  supposedState = true;
    if (!childRunning) {
      println("starting child");
      open(childLocation);//start child app when first start
      delay(2000); // wait a bit
    }
  }
  
  
  if(millis() - lastTimerMillis > runningTimer ){
   childRunning = false; 
  }
  
  long w = millis() - lastTimerMillis;
  float ww = map(w,0,runningTimer,0,width);
  noStroke();
  fill(0);
  rect(0,0,ww,height);
  fill(150);
  int hLeft = shutDownHour - hh;
  int hSince = hh - startHour;
  textAlign(CENTER);
  text("start " + startHour+":00,  "+ hSince + " hrs since", width/2, height*1/4); 
  text("shut " + shutDownHour+":00,  "+ hLeft + " hrs left", width/2, height*2/4); 
  
  if(supposedState){
    text("supposed to be ON",width/2,height*3/4);
  }else{
    text("supposed to be OFF",width/2,height*3/4);
  }
}



void keyPressed() {
  udp.send( "bang", "localhost", childPort);
}


void receive( byte[] data, String ip, int port ) {  // <-- extended handler

  String message = new String( data );
  decode(message);
}


//udp.send( message, "localhost", childPort);

void decode(String m) {
  if (m.equals("started")) {
    println("child app started"); 
    childRunning = true;
    childGotKillCommand = false;
  }
  else if (m.equals("exiting")) {
    println("child app exiting"); 
    childGotKillCommand = true;
    childRunning = false;
  }
  else if (m.equals("running")) {
    println("child is running"); 
    childRunning = true;
    lastTimerMillis = millis();
  }
}

