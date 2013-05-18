void sevenSegWrite(byte digit, int p) {

  if(digit != 10){

    for (byte segCount = 0; segCount < 7; segCount++) {
      if(p == 1){
        digitalWrite(LEDpin1[segCount], seven_seg_digits[digit][segCount]);
      }
      else{
        digitalWrite(LEDpin2[segCount], seven_seg_digits[digit][segCount]);
      }

    }

  }
  else{
    for(int i = 0; i< 7; i++){

      if(i == counter){
        if(p==1){
          digitalWrite(LEDpin1[i], HIGH);
        }
        else{
          digitalWrite(LEDpin2[i], HIGH); 
        }
      }
      else{
        if(p ==1 ){
          digitalWrite(LEDpin1[i], LOW);
        }
        else{
          digitalWrite(LEDpin2[i], LOW);
        }
      }

    }
  }
}




