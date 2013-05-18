/*
   Memsic2125
 
 Read the Memsic 2125 two-axis accelerometer.  Converts the
 pulses output by the 2125 into milli-g's (1/1000 of earth's
 gravity) and prints them over the serial connection to the
 computer.
 
 Based on: 
 http://www.arduino.cc/en/Tutorial/Memsic2125
 

 */


const int p1Pin = 9;     // Pin Accelerometer 1
const int p2Pin = 10;     // Pin Accelerometer 2

/// initialize communication and pins
//
void setup() {
  Serial.begin(115200);
  pinMode(p1Pin, INPUT);
  pinMode(p2Pin, INPUT);
}

void loop() {
  // variables to read the pulse widths:
  int p1Pulse, p2Pulse;

  // read from both accelerometers
  p1Pulse = pulseIn(p1Pin,HIGH);  
  p2Pulse = pulseIn(p2Pin,HIGH); 

  //Output a string to be decoded by processing later
  Serial.print(p1Pulse);
  Serial.print(",");
  Serial.print(p2Pulse);
  Serial.print("\n");

  delay(25); 
}

