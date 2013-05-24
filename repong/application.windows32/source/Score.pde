public class Score {

  int scoreL;
  int scoreR;
  
  int startBallTime = 0;
  float bestTime = 1.0;
  
  float[][] bestTimes;

  Score() {
    readBestTimes();
    
  }
    
  void draw() {
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
    float lapse = (millis()-startBallTime)/1000.0;
    
    if (gameState!=1) lapse = 0.0;
    textAlign(RIGHT);
    text(nf(lapse,1,1)+"  :",playArea.x+playArea.w/2+_i("scoreTimeDx"),playArea.h+playArea.y-_i("scoreTimeSize")+_i("scoreTimeDy"));
    
   
    
    textAlign(LEFT);
    text("  "+nf(bestTime,1,1),playArea.x+playArea.w/2+_i("scoreTimeDx"),playArea.h+playArea.y-_i("scoreTimeSize")+_i("scoreTimeDy"));
    
    textAlign(CENTER);
  }
  
  
  void readBestTimes() {
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
      bestTimes[k][0] = float(lineparts[0]);
      bestTimes[k][1] = float(lineparts[1]);
      k++;
    }
    for (int j=0;j<bestTimes.length;j++){
      if (daysAgo(int(bestTimes[j][0]))<_i("bestTime_since")) {
        bestTime = max(bestTime,bestTimes[j][1]);
      }
    }
  }
  
  void writeBestTimes() {
    Date d = new Date();
    long current=d.getTime()/1000;
    String[] scores = new String[bestTimes.length+1];
    for (int j=0;j<bestTimes.length;j++){
      scores[j] = int(bestTimes[j][0])+":"+bestTimes[j][1];
    }
    scores[bestTimes.length] = int(current)+":"+nf(bestTime,1,1);
    saveStrings("best_times.txt", scores);
  }
  
  int daysAgo(int timestamp) {
    Date d = new Date();
    long current=d.getTime()/1000;
    return int((current-timestamp)/(60*60*24));
  }
  
  void setScore(int score,int player) {
    if (player==0) {
      scoreL = score;
    } else {
      scoreR = score;
    }
    float thisTime = (millis()-startBallTime)/1000.0;
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
  
  void addScore(int player) {
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
  
  void reset() {
    
    scoreL = scoreR = 0;
    if (isButtons) {
        serial.myPort.write(score.scoreL);
        serial.myPort.write(score.scoreR+20);
      }
    startBallTime = millis();
  }
}

