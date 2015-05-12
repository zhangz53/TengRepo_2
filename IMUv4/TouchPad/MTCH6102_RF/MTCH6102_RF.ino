#include <Wire.h>
#include <ZigduinoRadio.h>

#define RF_DEVICE_ID 0x11
int rfChannel = 11;

#define MTCH6102_ADDR       0x25
#define MTCH6102_TOCUHSTATE 0x10
#define MTCH6102_NUMOFXCH   0x20

void setup()
{
  Wire.begin();        // join i2c bus (address optional for master)
  Serial.begin(115200);  // start serial for output
  
  RF.begin(rfChannel);
  RF.setParam(RP_TXPWR(3));
  RF.setParam(RP_DATARATE(MOD_OQPSK_1000));
  MTCH6102_rowcol(5,2);
}

void loop()
{
  uint16_t touchx, touchy;
 
  char str[20];

  if(MTCH6102(&touchx,&touchy))
  {
    Serial.print(touchx);
    Serial.print(", ");
    Serial.println(touchy);
    
    RF.beginTransmission();
    RF.write(dtostrf(touchx,3,0,str));
    RF.write(", ");
    RF.write(dtostrf(touchy,3,0,str));
    RF.write("\n\r");
    RF.endTransmission();
    
  }
  else
  {
    Serial.print(-1);
    Serial.print(", ");
    Serial.println(-1);

    int16_t t = -1;
    RF.beginTransmission();
    RF.write(dtostrf(t,3,0,str));
    RF.write(", ");
    RF.write(dtostrf(t,3,0,str)); 
    RF.write("\n\r");
    RF.endTransmission();
  }

  delay(10);
}

int8_t MTCH6102(uint16_t *touch_x,uint16_t *touch_y)
{
  int8_t touch_state; 
  uint16_t touchx, touchy, touchlsb;

  Wire.beginTransmission(MTCH6102_ADDR);
  Wire.write(MTCH6102_TOCUHSTATE);
  Wire.endTransmission(false);

  Wire.requestFrom(MTCH6102_ADDR, 4,true);
  touch_state = Wire.read();
  touchx = (Wire.read())<<4;
  touchy = (Wire.read())<<4;
  touchlsb = Wire.read();
  
 *touch_x = touchx | (0xF0&touchlsb)>>4;
 *touch_y = touchy | 0x0F&touchlsb; 
  
  return touch_state%2;
}

void MTCH6102_rowcol(uint8_t x_ch, uint8_t y_ch)
{
  Wire.beginTransmission(MTCH6102_ADDR);
  Wire.write(MTCH6102_NUMOFXCH);
  Wire.write(x_ch);
  Wire.write(y_ch);
  Wire.endTransmission();

}
