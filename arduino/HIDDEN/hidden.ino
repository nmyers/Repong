
int buttonPin[4];
int state[4];
int incomingByte = 0; 
int score[2];
int counter = 0;
int countInterval = 200;
long lastMillis = millis();
int LEDpin1[] = {
  43, 53, 51, 49, 41, 45, 47};
int LEDpin2[] = {
  42, 52, 50, 48, 40, 44, 46};

byte seven_seg_digits[10][7] = { 
  { 
    1,1,1,1,1,1,0             }
  ,  // = 0
  { 
    0,1,1,0,0,0,0             }
  ,  // = 1
  { 
    1,1,0,1,1,0,1             }
  ,  // = 2
  { 
    1,1,1,1,0,0,1             }
  ,  // = 3
  { 
    0,1,1,0,0,1,1             }
  ,  // = 4
  { 
    1,0,1,1,0,1,1             }
  ,  // = 5
  { 
    1,0,1,1,1,1,1             }
  ,  // = 6
  { 
    1,1,1,0,0,0,0             }
  ,  // = 7
  { 
    1,1,1,1,1,1,1             }
  ,  // = 8
  { 
    1,1,1,0,0,1,1             }   // = 9
};





void setup() {

  Serial.begin(9600);

  for(int i =0; i<4 ; i++){
    buttonPin[i] = 10+i; 
    pinMode(buttonPin[i], INPUT);
    state[i] = 10;
    score[i] = 10;
  }

  for(int i =0; i<7; i++){
    pinMode(LEDpin1[i], OUTPUT);
    pinMode(LEDpin2[i], OUTPUT);
  }
}

void loop() {
  for(int i=0; i<4 ; i++){
    state[i] = digitalRead(buttonPin[i]);

    Serial.print(state[i]);
    if(i != 3){
      Serial.print(",");
    }
    else{
      Serial.print("\n");
    }
  }



  //  Serial.print("player1 is ");
  //  Serial.println(score[0]);
  //  Serial.print("player2 is ");
  //  Serial.println(score[1]);

  if (Serial.available() > 0) {
    incomingByte = Serial.read();
    if(incomingByte < 15 ){ // 0 - 10, 20-30
      score[0] = incomingByte;
    }
    else{
      score[1] = incomingByte - 20;
    }
  }

  if(millis() - lastMillis > countInterval){
    lastMillis = millis();
    counter ++; 
    if(counter > 5){
      counter = 0; 
    }
  }

  sevenSegWrite(score[0],0);
  sevenSegWrite(score[1],1);

  //for(int i=0; i<10; i++){
  //  sevenSegWrite(i,0);
  //  sevenSegWrite(i,1);
  //}
  //delay(1000);

  //delay(25); 
}







