#include <Servo.h>
#include <dht.h>
char val; // variable to receive data from the serial port
int ledpin = 6; // LED connected to pin 48 (on-board LED)
int doorpin = 5; // LED connected to door
Servo door;
Servo window_left;
Servo window_right;
dht DHT;
#define DHT11_PIN 7

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
  int chk = DHT.read11(DHT11_PIN);
  Serial.print("Temperature = ");
  Serial.println(DHT.temperature);
  Serial.print("Humidity = ");
  Serial.println(DHT.humidity);
  delay(1000);
  if( Serial.available() )       // if data is available to read
  {
    val = Serial.read();         // read it and store it in 'val'
    Serial.print(val);
  }
  switch(val) {
    case '1':
      digitalWrite(doorpin, HIGH);  // turn ON the LED
      door.write(0);
      break;
    case '0':
      digitalWrite(doorpin, LOW);   // otherwise turn it OFF
      door.write(90);
      break;
    case '3':
      digitalWrite(ledpin, HIGH);
      break;
    case '2':
      digitalWrite(ledpin, LOW);
      break;
    case '5':
      door.write(0);
      window_left.write(0);
      window_right.write(90);
      break;
    case '4':
      door.write(90);
      window_left.write(90);
      window_right.write(0);
      break;
  }
  delay(100);                    // wait 100ms for next reading
}
