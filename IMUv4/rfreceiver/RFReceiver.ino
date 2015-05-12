#include <ZigduinoRadio.h>
int rfChannel = 11;


void setup()
{
  Serial.begin(115200);
  
 // Serial.println("ArduIMU V4 Receiver...");
  
  RF.begin(rfChannel);
  RF.setParam(RP_TXPWR(3));
  RF.setParam(RP_DATARATE(MOD_OQPSK_1000));
  
 // Serial.println("ArduIMU V4 :: init done.");
}

void loop()
{
  if (RF.available())
  {
    while(RF.available())
      Serial.write(RF.read());
  }
}

