/*
   Memsic2125
 
 Read the Memsic 2125 two-axis accelerometer.  Converts the
 pulses output by the 2125 into milli-g's (1/1000 of earth's
 gravity) and prints them over the serial connection to the
 computer.
 
 Based on: 
 http://www.arduino.cc/en/Tutorial/Memsic2125
 

 */


const int p1Pin = 8;     // Pin Accelerometer 1
const int p2Pin = 9;     // Pin Accelerometer 2
int avgCount = 10; // frames to average

int min1 = 4232;
int zero1 = 3821;
int max1 = 3427;

int min2 = 3533;
int zero2 = 3989;
int max2 = 4416;

int minOut = -300;
int maxOut = 300;

float med1 = zero1;
float med2 = zero2;
int out1 = 0;
int out2 = 0;
/// initialize communication and pins


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
  
//  med1 = med1*(avgCount-1)/avgCount + p1Pulse/avgCount;
//  med2 = med2*(avgCount-1)/avgCount + p2Pulse/avgCount;
  
  //Output a string to be decoded by processing later
//  Serial.print(p1Pulse);
//  Serial.print(",");
//  Serial.print(p2Pulse);
//  Serial.print("\n");

  if(p1Pulse < zero1){
    out1 = map(p1Pulse,zero1,max1,0,maxOut);
  }else{
    out1 = map(p1Pulse,zero1,min1,0,minOut);
  }
  
  if(p2Pulse > zero2){
    out2 = map(p2Pulse,zero2,max2,0,maxOut);
    
  }else{
    out2 = map(p2Pulse,zero2,min2,0,minOut);
  }
  //out1 /= 10;
  //out2 /= 10;
  Serial.print(out1);
  Serial.print(",");
  Serial.print(out2);
  Serial.print("\n");

  //delay(10); 
}

