import hypermedia.net.*;
int childPort = 9999;
int masterPort = 12345;
UDP udp;
int timerInterval = 1000;
long lastTimerMillis = millis();


void setupTimer(){
  
    udp = new UDP( this, childPort); //listerning port
  udp.listen( true );
  udp.send( "started", "localhost", masterPort);
}

void receive( byte[] data, String ip, int port ) {  // <-- extended handler
  String message = new String( data );
  //println( "receive: \""+message+"\" from "+ip+" on port "+port );
  decodeTimer(message);
}


void decodeTimer(String m) {
  if (m.equals("bang")) {
    udp.send( "exiting", "localhost", masterPort);
    println("got kill command, exiting"); 
    exit();
  }
}

void updateTimerStatus() {
  if (millis() - lastTimerMillis > timerInterval) {
    lastTimerMillis = millis(); 
    udp.send( "running", "localhost", masterPort);
  }
}
