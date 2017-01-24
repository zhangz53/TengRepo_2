#include <Wire.h>

//*** constants for IMU data
#define BNO055_I2C_H_SLAVE_ADDR   0x29
#define BNO055_I2C_ADDR   0x29
#define BNO055_I2C_HID_SLAVE_ADDR 0x40

// BNO055 Registers
#define MAG_RADIUS_MSB 0x6A // R/W
#define MAG_RADIUS_LSB 0x69 // R/W
#define ACC_RADIUS_MSB 0x68 // R/W
#define ACC_RADIUS_LSB 0x67 // R/W

#define GYR_OFFSET_Z_MSB 0x66 // R/W
#define GYR_OFFSET_Z_LSB 0x65 // R/W
#define GYR_OFFSET_Y_MSB 0x64 // R/W
#define GYR_OFFSET_Y_LSB 0x63 // R/W 
#define GYR_OFFSET_X_MSB 0x62 // R/W
#define GYR_OFFSET_X_LSB 0x61 // R/W

#define MAG_OFFSET_Z_MSB 0x60 // R/W
#define MAG_OFFSET_Z_LSB 0x5F // R/W
#define MAG_OFFSET_Y_MSB 0x5E // R/W
#define MAG_OFFSET_Y_LSB 0x5D // R/W
#define MAG_OFFSET_X_MSB 0x5C // R/W
#define MAG_OFFSET_X_LSB 0x5B // R/W

#define ACC_OFFSET_Z_MSB 0x5A // R/W
#define ACC_OFFSET_Z_LSB 0x59 // R/W
#define ACC_OFFSET_Y_MSB 0x58 // R/W
#define ACC_OFFSET_Y_LSB 0x57 // R/W
#define ACC_OFFSET_X_MSB 0x56 // R/W
#define ACC_OFFSET_X_LSB 0x55 // R/W

// Registers 43-54 reserved for Soft Iron Calibration Matrix

#define AXIS_MAP_SIGN   0x42 // Bits0-2: R/W; Bits3-7: reserved 
#define AXIS_MAP_CONFIG 0x41 // Bits0-5: R/W; Bits6-7: reserved
#define TEMP_SOURCE     0x40 // Bits0-1: R/W; Bits2-7: reserved

#define SYS_TRIGGER 0x3F
#define PWR_MODE    0x3E
#define OPR_MODE    0x3D //default = 0x1C

// Register 3C Reserved

#define UNIT_SEL       0x3B //RO
#define SYS_ERR        0x3A //RO
#define SYS_STATUS     0x39 //RO 
#define SYS_CLK_STATUS 0x38 //RO
#define INT_STATUS     0x37	//RO
#define ST_RESULT      0x36 //RO
#define CALIB_STAT     0x35 //RO
#define TEMP           0x34 //RO

#define GRV_DATA_Z_MSB 0x33 //RO
#define GRV_DATA_Z_LSB 0x32 //RO
#define GRV_DATA_Y_MSB 0x31 //RO
#define GRV_DATA_Y_LSB 0x30 //RO
#define GRV_DATA_X_MSB 0x2F //RO
#define GRV_DATA_X_LSB 0x2E //RO

#define LIA_DATA_Z_MSB 0x2D //RO
#define LIA_DATA_Z_LSB 0x2C //RO
#define LIA_DATA_Y_MSB 0x2B //RO
#define LIA_DATA_Y_LSB 0x2A //RO
#define LIA_DATA_X_MSB 0x29 //RO
#define LIA_DATA_X_LSB 0x28 //RO

#define QUA_DATA_Z_MSB 0x27 //RO
#define QUA_DATA_Z_LSB 0x26 //RO
#define QUA_DATA_Y_MSB 0x25 //RO
#define QUA_DATA_Y_LSB 0x24 //RO
#define QUA_DATA_X_MSB 0x23 //RO
#define QUA_DATA_X_LSB 0x22 //RO
#define QUA_DATA_W_MSB 0x21 //RO
#define QUA_DATA_W_LSB 0x20 //RO

#define EUL_PITCH_MSB   0x1F //RO
#define EUL_PITCH_LSB   0x1E //RO
#define EUL_ROLL_MSB    0x1D //RO
#define EUL_ROLL_LSB    0x1C //RO
#define EUL_HEADING_MSB 0x1B //RO
#define EUL_HEADING_LSB 0x1A //RO

#define GYR_DATA_Z_MSB 0x19 //RO
#define GYR_DATA_Z_LSB 0x18 //RO
#define GYR_DATA_Y_MSB 0x17 //RO
#define GYR_DATA_Y_LSB 0x16 //RO
#define GYR_DATA_X_MSB 0x15 //RO
#define GYR_DATA_X_LSB 0x14 //RO

#define MAG_DATA_Z_MSB 0x13 //RO
#define MAG_DATA_Z_LSB 0x12 //RO
#define MAG_DATA_Y_MSB 0x11 //RO
#define MAG_DATA_Y_LSB 0x10 //RO
#define MAG_DATA_X_MSB 0x0F //RO
#define MAG_DATA_X_LSB 0x0E //RO

#define ACC_DATA_Z_MSB 0x0D //RO
#define ACC_DATA_Z_LSB 0x0C //RO
#define ACC_DATA_Y_MSB 0x0B //RO
#define ACC_DATA_Y_LSB 0x0A //RO
#define ACC_DATA_X_MSB 0x09 //RO
#define ACC_DATA_X_LSB 0x08 //RO

#define PAGE_ID       0x07 //RO
#define BL_REV_ID     0x06 //RO
#define SW_REV_ID_MSB 0x05 //RO
#define SW_REV_ID_LSB 0x04 //RO

#define GYR_ID  0x03 // RO; Default = 0x0F
#define MAG_ID  0x02 // RO; Default = 0x32
#define ACC_ID  0xFB // RO; Default = 0xFB
#define CHIP_ID 0x00 // RO; Default 0xA0

// PAGE 1 (All unreserved registers in Page 1 are READ ONLY)
// Registers 7F-60 reserved
// Registers 5F-50 are named UNIQUE_ID and are READ ONLY
// Registers 4F-20 reserved

#define GYR_AM_SET     0x1F // Default = 0x0A
#define GYR_AM_THRES   0x1E	// Default = 0x04
#define GYR_DUR_Z      0x1D	// Default = 0x19
#define GYR_HR_Z_SET   0x1C	// Default = 0x01
#define GYR_DUR_Y      0x1B	// Default = 0x19
#define GYR_HR_Y_SET   0x1A	// Default = 0x01
#define GYR_DUR_X      0x19	// Default = 0x19
#define GYR_HR_X_SET   0x18	// Default = 0x01
#define GYR_INT_SETING 0x17 // Default = 0x00

#define ACC_NM_SET       0x16 // Default = 0x0B
#define ACC_NM_THRE      0x15 // Default = 0x0A
#define ACC_HG_THRE      0x14 // Default = 0xC0
#define ACC_HG_DURATION  0x13 // Default = 0x0F
#define ACC_INT_SETTINGS 0x12 // Default = 0x03
#define ACC_AM_THRES     0x11 //Default = 0x14

#define INT_EN   0x10
#define INT_MASK 0x0F

// Register 0x0E reserved

#define GYR_SLEEP_CONFIG 0x0D 
#define ACC_SLEEP_CONFIG 0x0C
#define GYR_CONFIG_1     0x0B
#define GYR_CONFIG_0     0x0A
#define MAG_CONFIG       0x09
#define ACC_CONFIG       0x08

// PAGE_ID still mapped to 0x07
// Registers 6-0 reserved

#define PWR_MODE_SEL_BIT    0
#define PWR_MODE_SEL_LENGTH 2

#define PWR_MODE_NORMAL  0x00
#define PWR_MODE_LOW	 0x01
#define PWR_MODE_SUSPEND 0x02

#define OPR_MODE_SEL_BIT 0
#define OPR_MODE_LENGTH  4

#define OPR_MODE_CONFIG_MODE  0x00
#define OPR_MODE_ACCONLY 	  0x01
#define OPR_MODE_MAGONLY 	  0x02
#define OPR_MODE_GYROONLY 	  0x03
#define OPR_MODE_ACCMAG 	  0x04
#define OPR_MODE_ACCGYRO 	  0x05
#define OPR_MODE_MAGGYRO 	  0x06
#define OPR_MODE_AMG	 	  0x07
#define OPR_MODE_IMU	 	  0x08
#define OPR_MODE_COMPASS 	  0x09
#define OPR_MODE_M4G	 	  0x0A
#define OPR_MODE_NDOF_FMC_OFF 0x0B
#define OPR_MODE_NDOF 		  0x0C

#define DATA_RATE_SEL_BIT    4
#define DATA_RATE_SEL_LENGTH 3

#define FASTEST_MODE     0x10
#define GAME_MODE	 0x20
#define UI_MODE		 0x30
#define NORMAL_MODE      0x40

#define X_AXIS_REMAP_BIT  0
#define Y_AXIS_REMAP_BIT  2
#define Z_AXIS_REMAP_BIT  4
#define AXIS_REMAP_LENGTH 2

#define AXIS_REP_X 0x00
#define AXIS_REP_Y 0x01
#define AXIS_REP_Z 0x02

#define AXIS_SIGN_REMAP_BIT	   0
#define AXIS_SIGN_REMAP_LENGTH 3

#define ACC_RANGE_SEL_BIT	  0
#define ACC_RANGE_SEL_LENGTH  2
#define ACC_BW_SEL_BIT        2 //Auto controlled in fusion mode
#define ACC_BW_SEL_LENGTH     3 //Auto controlled in fusion mode
#define ACC_OPMODE_SEL_BIT    5 //Auto controlled in fusion mode
#define ACC_OPMODE_SEL_LENGTH 3 //Auto controlled in fusion mode

#define ACC_RANGE_2G  0x00
#define ACC_RANGE_4G  0x01 // Default
#define ACC_RANGE_8G  0x02
#define ACC_RANGE_16G 0x03

#define GYR_RANGE_SEL_BIT	  0 //Auto controlled in fusion mode
#define GYR_RANGE_SEL_LENGTH  3 //Auto controlled in fusion mode
#define GYR_BW_SEL_BIT        3 //Auto controlled in fusion mode
#define GYR_BW_SEL_LENGTH     3 //Auto controlled in fusion mode
#define GYR_OPMODE_SEL_BIT    6 //Auto controlled in fusion mode
#define GYR_OPMODE_SEL_LENGTH 2 //Auto controlled in fusion mode

#define MAG_RANGE_SEL_BIT	  0 //Auto controlled in fusion mode
#define MAG_RANGE_SEL_LENGTH  3 //Auto controlled in fusion mode
#define MAG_BW_SEL_BIT        3 //Auto controlled in fusion mode
#define MAG_BW_SEL_LENGTH     2 //Auto controlled in fusion mode
#define MAG_OPMODE_SEL_BIT    5 //Auto controlled in fusion mode
#define MAG_OPMODE_SEL_LENGTH 2 //Auto controlled in fusion mode

#define ACC_UNIT_SEL_BIT          0
#define ANGULAR_RATE_UNIT_SEL_BIT 1
#define EULER_ANGLE_UNIT_SEL_BIT  2
#define TEMP_UNIT_SEL_BIT         4
#define UNIT_SEL_LENGTH           1

#define ACC_UNIT_M_SSQ        0
#define ACC_UNIT_MG           1
#define ANGULAR_RATE_UNIT_DPS 0
#define ANGULAR_RATE_UNIT_RPS 1
#define EULER_ANGLE_UNIT_DEG  0
#define EULER_ANGLE_UNIT_RAD  1
#define TEMP_UNIT_C           0
#define TEMP_UNIT_F           1

// *** constants for touch data
#define MTCH6102_ADDR       0x25
#define MTCH6102_TOCUHSTATE 0x10
#define MTCH6102_NUMOFXCH   0x20


String message; //string that stores the incoming message
int ledPin = 13;
int cnt_down=80;
int delayTime = 20;
String BT200 = "8833145F1775"; // Moverio MAC address
String BT200SK = "847E40A2E8A7"; //SoftKinetic uint
  
void setup()
{ 
  //set up led pin
  pinMode(ledPin, OUTPUT);
  
  //Open serial port
  Serial.begin(57600); //start bluetooth serial
  
  Serial.print("$$$");
  delay(100); 
  Serial.println("SM,0");
  delay(100);
  Serial.println("SA,0");
  delay(100);
  Serial.println("SO,Z");
  delay(100);
  Serial.println("R,1");
  delay(100);
  
  
  Serial.begin(57600); //start bluetooth serial
  //delay(1000); //wait to allow remote configuration
  
  Serial.print("$$$");
  delay(100);  
  Serial.println("SR," + BT200SK);
  delay(100);
  Serial.println("C");
  delay(100);
  Serial.println("U,9600,N");
  delay(100);
  
  Serial.begin(9600); //start bluetooth serial
  
  
  // Initialize the 'Wire' class for the I2C-bus.
  Wire.begin();
  
  BNO055_write(OPR_MODE, FASTEST_MODE|OPR_MODE_NDOF);
  
  MTCH6102_rowcol(4,3); // size of touch array
  
  delay(1000);
}
 
void loop()
{
  message = "NuiRing: ";
  
  //get touch data
  message += getTouch();
  
  //get IMU data
  message += freeCube();
  
  message += "[X]";
  Serial.println(message);
  
  //Serial.println("1,2,3,4,[X]");
  //delay(100);
  blink1();
  
}

void blink1() 
{
  digitalWrite(ledPin, HIGH);
  delay(delayTime);
  digitalWrite(ledPin, LOW);  
}

String getTouch()
{
  String message = "";
  uint16_t touchX;
  uint16_t touchY;
  int8_t gesture;
  bool isGesture;
  bool isTouch;
  
  isTouch = MTCH6102(&touchX, &touchY, &isGesture, &gesture);
  
  if (isTouch)
  {
    message += touchX;
    message += ";";
    message += touchY;
    message += ";";
  }
  else
  {
    message += "-1;-1;";    
  }
  
  if (isGesture)
  {
    message += gesture; 
    message += ";";
  }
  else
  {
     message += "-1;"; 
  }
  
  return message;
}

String freeCube()
{
  String values;
  
  float q[4];
  BNO055_4_vec(QUA_DATA_W_LSB, &q[0]);
  values += freeIMUOut(&q[0]);
  
  return values;
}

String freeIMUOut(float *quat)
{
  String values = "";
  values += serialFloat(quat[0]);
  values += (";"); 
  values += serialFloat(quat[1]);
  values += (";"); 
  values += serialFloat(quat[2]);
  values += (";"); 
  values += serialFloat(quat[3]);
  values += (";"); 
 
  return values; 
}

String serialFloat(float f) 
{
  String value = "";
  byte * b = (byte *) &f;
  for(int i=0; i<4; i++) {
    
    byte b1 = (b[i] >> 4) & 0x0f;
    byte b2 = (b[i] & 0x0f);
 
    char c1 = (b1 < 10) ? ('0' + b1) : 'A' + b1 - 10;
    char c2 = (b2 < 10) ? ('0' + b2) : 'A' + b2 - 10;
    
    value += c1;
    value += c2;
  }
  
  return value;
}

void BNO055_4_vec(int8_t addr, float *vec){
  float norm;
  Wire.beginTransmission(BNO055_I2C_ADDR);
  Wire.write(addr);
  Wire.endTransmission(false);

  Wire.requestFrom(BNO055_I2C_ADDR, 8, false);
  int16_t b_data[8];
  for(int i=0;i<8;i++)
    b_data[i]=Wire.read();
  
  vec[0] = b_data[1]<<8|b_data[0];
  vec[1] = b_data[3]<<8|b_data[2];
  vec[2] = b_data[5]<<8|b_data[4];
  vec[3] = b_data[7]<<8|b_data[6];
 
 /* //If you need normalization
  norm = sqrt(vec[0] * vec[0] + vec[1] * vec[1] + vec[2] * vec[2] + vec[3] * vec[3]);    
  norm = 1.0f/norm;
  vec[0] *= norm;
  vec[1] *= norm;
  vec[2] *= norm;
  vec[3] *= norm;
*/
}

int BNO055_write(int addr,int data){
  Wire.beginTransmission(BNO055_I2C_ADDR);
  Wire.write(addr);
  Wire.write(data);
  Wire.endTransmission(true);

  return 1;
}

void MTCH6102_rowcol(uint8_t x, uint8_t y)
{
  Wire.beginTransmission(MTCH6102_ADDR);
  Wire.write(MTCH6102_NUMOFXCH);
  Wire.write(x);
  Wire.write(y);
  Wire.endTransmission(false);
}

bool MTCH6102(uint16_t *touch_x, uint16_t *touch_y, bool *isGesture, int8_t *gesture)
{
  int8_t touch_state; 
  uint16_t touchx, touchy, touchlsb;
  bool isTouch;

  Wire.beginTransmission(MTCH6102_ADDR);
  Wire.write(MTCH6102_TOCUHSTATE);
  Wire.endTransmission(false);

  Wire.requestFrom(MTCH6102_ADDR, 5,true);
  touch_state = Wire.read();
  touchx = (Wire.read())<<4;
  touchy = (Wire.read())<<4;
  touchlsb = Wire.read();
  *gesture = Wire.read();
  
  isTouch = touch_state % 2;
  *isGesture = (touch_state >> 1) % 2;
  
 *touch_x = touchx | (0xF0&touchlsb)>>4;
 *touch_y = touchy | 0x0F&touchlsb; 
  
  return isTouch;
}

