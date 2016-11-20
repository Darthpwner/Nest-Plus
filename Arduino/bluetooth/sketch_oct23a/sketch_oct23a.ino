#include <Servo.h>
char val; // variable to receive data from the serial port
int ledpin = 6; // LED connected to pin 48 (on-board LED)
int doorpin = 5; // LED connected to door
Servo door;
Servo window_left;
Servo window_right;

void setup() {
  pinMode(ledpin, OUTPUT);  // pin 48 (on-board LED) as OUTPUT
  pinMode(doorpin, OUTPUT);
  Serial.begin(9600);       // start serial communication at 9600bps
  door.attach(A0); //analog pin 0
  window_left.attach(A1); //analog pin 1
  window_right.attach(A2); //analog pin 2
  door.write(90);
  window_left.write(90);
  window_right.write(90);
}
void loop() {
  if( Serial.available() )       // if data is available to read
  {
    val = Serial.read();         // read it and store it in 'val'
    Serial.print(val);
  }
  switch(val) {
    case 1:
      digitalWrite(doorpin, HIGH);  // turn ON the LED
      door.write(0);
      break;
    case 2:
      digitalWrite(doorpin, LOW);   // otherwise turn it OFF
      door.write(90);
      break;
    case 3:
      digitalWrite(ledpin, HIGH);
      break;
    case 4:
      digitalWrite(ledpin, LOW);
      break;
    case 5:
      window_left.write(0);
      window_right.write(90);
      break;
    case 6:
      window_left.write(90);
      window_right.write(0);
      break;
  }
  delay(100);                    // wait 100ms for next reading
}
