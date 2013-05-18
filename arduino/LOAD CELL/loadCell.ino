int r0 = 0;      //value of select pin at the 4051 (s0)
int r1 = 0;      //value of select pin at the 4051 (s1)
int r2 = 0;      //value of select pin at the 4051 (s2)
int input = 0;
int count = 0;   //which y pin we are selecting
int sensorPin = A0;
int mulPin1 = 7;
int mulPin2 = 6;
int mulPin3 = 5;
int p1 = 0;
int p2 = 0;

void setup(){


  pinMode(mulPin1, OUTPUT);    // s0
  pinMode(mulPin2, OUTPUT);    // s1
  pinMode(mulPin3, OUTPUT);    // s2
  Serial.begin(115200);

}



void loop () {
  p1 = 0;
  p2 = 0;
  
  for (count=0; count<8; count++) {
    
    // select the bit  
    r0 = bitRead(count,0);    // use this with arduino 0013 (and newer versions)     
    r1 = bitRead(count,1);    // use this with arduino 0013 (and newer versions)     
    r2 = bitRead(count,2);    // use this with arduino 0013 (and newer versions)     



    digitalWrite(mulPin1, r0);
    digitalWrite(mulPin2, r1);
    digitalWrite(mulPin3, r2);
    
    input = analogRead(sensorPin);
//    Serial.print(" ");
//    Serial.print(count);
//    Serial.print(": ");
//    Serial.print(input);
    if(count < 4){
      p1 += input;  
    }else{
      p2 += input; 
    }
    delay(10);
    
    //Either read or write the multiplexed pin here
    //analogWrite(9,(count+1)*80);
    

  }
  //Serial.println();
  Serial.print(p1);
  Serial.print(",");
  Serial.print(p2);
  Serial.print("\n");  
  //delay(20);
}


