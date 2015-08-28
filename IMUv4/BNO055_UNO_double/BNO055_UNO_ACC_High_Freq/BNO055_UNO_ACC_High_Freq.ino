#include <Wire.h>
//#include <EEPROM.h>

#include <ZigduinoRadio.h>

#define RF_DEVICE_ID 0x11

//remove comment to enable local serial printing
//#define LOCALDEBUG

int rfChannel = 11;

//int node_id;

#define BNO055_I2C_H_ADDR   0x29
#define BNO055_I2C_ADDR   0x28
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

#define UNIT_SEL       0x3B //RO  //0x3B
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
#define ACC_ID  0x01 // RO; Default = 0xFB
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
#define ACC_CONFIG       0x08   //08

#define ACC_CONFIG_VAL   0x1D  //1D, 19

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

enum Ascale {  // ACC Full Scale
  AFS_2G = 0,
  AFS_4G,
  AFS_8G,
  AFS_18G
};

enum Abw { // ACC Bandwidth
  ABW_7_81Hz = 0,
  ABW_15_63Hz,
  ABW_31_25Hz,
  ABW_62_5Hz,
  ABW_125Hz,    
  ABW_250Hz,
  ABW_500Hz,     
  ABW_1000Hz,    //0x07
};

enum APwrMode { // ACC Pwr Mode
  NormalA = 0,  
  SuspendA,
  LowPower1A,
  StandbyA,        
  LowPower2A,
  DeepSuspendA
};

enum Gscale {  // gyro full scale
  GFS_2000DPS = 0,
  GFS_1000DPS,
  GFS_500DPS,
  GFS_250DPS,
  GFS_125DPS    // 0x04
};

enum GPwrMode { // GYR Pwr Mode
  NormalG = 0,
  FastPowerUpG,
  DeepSuspendedG,
  SuspendG,
  AdvancedPowerSaveG
};

enum Gbw { // gyro bandwidth
  GBW_523Hz = 0,
  GBW_230Hz,
  GBW_116Hz,
  GBW_47Hz,
  GBW_23Hz,
  GBW_12Hz,
  GBW_64Hz,
  GBW_32Hz
};

enum Modr {         // magnetometer output data rate  
  MODR_2Hz = 0,     
  MODR_6Hz,
  MODR_8Hz,
  MODR_10Hz,  
  MODR_15Hz,
  MODR_20Hz,
  MODR_25Hz, 
  MODR_30Hz 
};

enum MOpMode { // MAG Op Mode
  LowPower = 0,
  Regular,
  EnhancedRegular,
  HighAccuracy
};

enum MPwrMode { // MAG power mode
  Normal = 0,   
  Sleep,     
  Suspend,
  ForceMode  
};

uint8_t Ascale = AFS_4G;      // Accel full scale
uint8_t APwrMode = NormalA;    // Accel power mode
uint8_t Abw = ABW_250Hz;    // Accel bandwidth, accel sample rate divided by ABW_divx

uint8_t GPwrMode = NormalG;    // Gyro power mode
uint8_t Gscale = GFS_2000DPS;  // Gyro full scale
uint8_t Gbw = GBW_230Hz;       // Gyro bandwidth

uint8_t MOpMode = Regular;    // Select magnetometer perfomance mode
uint8_t MPwrMode = Normal;    // Select magnetometer power mode
uint8_t Modr = MODR_30Hz;     // Select magnetometer ODR when in BNO055 bypass mode

int16_t accelCount[3];  // Stores the 16-bit signed accelerometer sensor output
int16_t gyroCount[3];   // Stores the 16-bit signed gyro sensor output
int16_t magCount[3];    // Stores the 16-bit signed magnetometer sensor output
int16_t quatCount[4];   // Stores the 16-bit signed quaternion output
int16_t EulCount[3];    // Stores the 16-bit signed Euler angle output
int16_t LIACount[3];    // Stores the 16-bit signed linear acceleration output
int16_t GRVCount[3];    // Stores the 16-bit signed gravity vector output
float gyroBias[3] = {0, 0, 0}, accelBias[3] = {0, 0, 0}, magBias[3] = {0, 0, 0};  // Bias corrections for gyro, accelerometer, and magnetometer
int16_t tempGCount, tempMCount;      // temperature raw count output of mag and gyro
float   Gtemperature, Mtemperature;  // Stores the BNO055 gyro and mag internal chip temperatures in degrees Celsius

float GyroMeasError = PI * (40.0f / 180.0f);   // gyroscope measurement error in rads/s (start at 40 deg/s)
float GyroMeasDrift = PI * (0.0f  / 180.0f);   // gyroscope measurement drift in rad/s/s (start at 0.0 deg/s/s)
// There is a tradeoff in the beta parameter between accuracy and response speed.
// In the original Madgwick study, beta of 0.041 (corresponding to GyroMeasError of 2.7 degrees/s) was found to give optimal accuracy.
// However, with this value, the LSM9SD0 response time is about 10 seconds to a stable initial quaternion.
// Subsequent changes also require a longish lag time to a stable output, not fast enough for a quadcopter or robot car!
// By increasing beta (GyroMeasError) by about a factor of fifteen, the response time constant is reduced to ~2 sec
// I haven't noticed any reduction in solution accuracy. This is essentially the I coefficient in a PID control sense; 
// the bigger the feedback coefficient, the faster the solution converges, usually at the expense of accuracy. 
// In any case, this is the free parameter in the Madgwick filtering and fusion scheme.
float beta = sqrt(3.0f / 4.0f) * GyroMeasError;   // compute beta
float zeta = sqrt(3.0f / 4.0f) * GyroMeasDrift;   // compute zeta, the other free parameter in the Madgwick scheme usually set to a small or zero value
#define Kp 2.0f * 5.0f // these are the free parameters in the Mahony filter and fusion scheme, Kp for proportional feedback, Ki for integral
#define Ki 0.0f

float ax, ay, az, gx, gy, gz, mx, my, mz; // variables to hold latest sensor data values 
float q[4] = {1.0f, 0.0f, 0.0f, 0.0f};    // vector to hold quaternion
float quat[4] = {1.0f, 0.0f, 0.0f, 0.0f};    // vector to hold quaternion
float eInt[3] = {0.0f, 0.0f, 0.0f};       // vector to hold integral error for Mahony method

void setup()
{
  // Initialize the Serial Bus for printing data.
  Serial.begin(115200);
  
  //RF.begin(11);
  //RF.setParam(RP_TXPWR(3));
  //RF.setParam(RP_DATARATE(MOD_OQPSK_1000));
//  RF.attachError(errHandle);

  pinMode(35,OUTPUT);
  digitalWrite(35,HIGH);
  pinMode(3,OUTPUT);
  pinMode(5,OUTPUT);
  pinMode(6,OUTPUT);
  analogWrite(3,254);
  analogWrite(5,254);
  analogWrite(6,254);
  

  // Initialize the 'Wire' class for the I2C-bus.
 // Wire.beginOnPins(7,30);
  Wire.begin();  //default 100khz
  //TWBR = 12;
  
//BNO055_read
  Serial.println("BNO055 9-axis motion sensor...");
  byte c = BNO055_read(CHIP_ID);
  Serial.println(c, HEX);

  Serial.println("check acc_id");
  byte d = BNO055_read(ACC_ID);
  Serial.println(d, HEX);  
  delay(1000);

  Serial.println("check page_id");
  byte p = BNO055_read(PAGE_ID);
  Serial.println(p, HEX);  
  delay(1000);

  
//enter config mode
  BNO055_write(OPR_MODE, OPR_MODE_CONFIG_MODE);
  BNO055_write_1(OPR_MODE, OPR_MODE_CONFIG_MODE);
  
  delay(1000);
  
  BNO055_write(PAGE_ID, 0x01);
  BNO055_write_1(PAGE_ID, 0x01);
  delay(1000);
  
  Serial.println("check acc_config before");
  byte e = BNO055_read(ACC_CONFIG);
  Serial.println(e, HEX);
  delay(1000);
  
  BNO055_write(ACC_CONFIG, APwrMode << 5 | Abw << 2 | Ascale);
  BNO055_write_1(ACC_CONFIG, APwrMode << 5 | Abw << 2 | Ascale);
  
  delay(1000);
  
  Serial.println("check acc_config after");
  byte f = BNO055_read(ACC_CONFIG);
  Serial.println(f, HEX);
  delay(1000);
  
  BNO055_write(PAGE_ID, 0x00);
  BNO055_write_1(PAGE_ID, 0x00);

  delay(1000);
  
//enter acc mode
  BNO055_write(OPR_MODE, OPR_MODE_ACCONLY);
  BNO055_write_1(OPR_MODE, OPR_MODE_ACCONLY);  //FASTEST_MODE|
  delay(10000);

 // EEPROM.write(1, 53); //to program node id
  //acc config

  //gyro config
  
  
  //node_id = EEPROM.read(1); // to read the node id

}

int cnt_down=80;
void loop()
{
 // freeCube();
 //freeCubeAcc();
 // freeCubeAccRF();
 
 //acc data only
 //getAccRF();
 getAcc();
 
 //acc data from both sensor
 //freeCubeAccDoubleRF();
 //freeCubeAllDoubleRF();
 
 
 //   linearAcc();
 //  gravityVec();
 // freeCubeRF();
  //freeCubeRF_multi(49);
  //freeCube_unity(0);
  //freeCubeRF_multi(node_id+39);
  /*
  if(digitalRead(6))
    cnt_down=80;
  else
    if(cnt_down)
      cnt_down--;
    
  if(!cnt_down)
  {  
     digitalWrite(16,LOW);
     //turn off all LEDs
     pinMode(28,INPUT);
     pinMode(13,INPUT);
     pinMode(23,INPUT);
     pinMode(24,INPUT);
     delay(50);
     while(1){
       digitalWrite(16,LOW);
     }
  }  
  */
  //freeCubeRF_unity(node_id-9);
  //freeCubeRF_multi(50);
  //linearAcc();
  //AccRF();
  //linearAccRF();
  //gravityVec();
  //eulerHead();
  
  //delay(4); //
}

void acc(){
  print_3_vec(ACC_DATA_X_LSB);
}

void gyro(){
  print_3_vec(GYR_DATA_X_LSB);
}

/*
void AccRF(){
  print_3_vec_RF(ACC_DATA_X_LSB);
}
*/
void mag(){
  print_3_vec(MAG_DATA_X_LSB);
}

void eulerHead(){
  print_3_vec(EUL_HEADING_LSB);
}

void gravityVec(){
  print_3_vec(GRV_DATA_X_LSB);
}
/*
void linearAccRF(){
  print_3_vec_RF(LIA_DATA_X_LSB);
}*/

void linearAcc(){
  print_3_vec(LIA_DATA_X_LSB);
}

void print_3_vec(int8_t addr){
  float temp[3];
  BNO055_3_vec(addr ,&temp[0]);
  Serial.print(temp[0]);
  Serial.print(", ");
  Serial.print(temp[1]);
  Serial.print(", "); 
  Serial.print(temp[2]);
  Serial.println();
}
/*
void print_3_vec_RF(int8_t addr){
  float temp[3];
  char str[20];
  BNO055_3_vec(addr ,&temp[0]);
  RF.beginTransmission();
  RF.write(dtostrf(temp[0], 2, 2, str));
  RF.write(", ");
  RF.write(dtostrf(temp[1], 2, 2, str));
  RF.write(", "); 
  RF.write(dtostrf(temp[2], 2, 2, str));
  RF.write(" \n\r");
  RF.endTransmission();
}

void freeCubeRF_multi(int node){
  float q[4];
  BNO055_4_vec(QUA_DATA_W_LSB, &q[0]);
  freeIMUOutRF_multi(&q[0], node);
}

void freeCubeRF_unity(int node){
  float q[4];
  BNO055_4_vec(QUA_DATA_W_LSB, &q[0]);
  freeIMUOutRF_unity(&q[0], node);
}
*/
void freeCube_unity(int node){
  float q[4];
  BNO055_4_vec(QUA_DATA_W_LSB, &q[0]);
  freeIMUOut_unity(&q[0], node);
}
/*
void freeCubeRF(){
  float q[4];
  BNO055_4_vec(QUA_DATA_W_LSB, &q[0]);
  freeIMUOutRF(&q[0]);
}
*/
void freeCube(){
  float q[4];
  BNO055_4_vec(QUA_DATA_W_LSB, &q[0]);
  freeIMUOut(&q[0]);
}

unsigned long ltime=0,ctime=0;
void freeCubeAcc(){
  float q[4];
  float a[4];
  BNO055_4_vec(QUA_DATA_W_LSB, &q[0]);
  BNO055_3_vec(LIA_DATA_X_LSB, &a[0]);
  ctime=millis();
  a[3]=ctime-ltime;
  ltime=ctime;
  freeIMUOut_s(&q[0]);
  Serial.print(",");
  freeIMUOut(&a[0]);
}

void freeCubeAccRF(){
  float q[4];
  float a[4];
  BNO055_4_vec(QUA_DATA_W_LSB, &q[0]);
  BNO055_3_vec(LIA_DATA_X_LSB, &a[0]);
  ctime=millis();
  a[3]=ctime-ltime;
  ltime=ctime;
  freeIMUOut_accRF(&q[0],&a[0]);
}

void getAccRF()
{
  float a1[3];
  float a2[3];
  float t[1];
  
  BNO055_3_vec(ACC_DATA_X_LSB, &a1[0]);
  BNO055_3_vec_1(ACC_DATA_X_LSB, &a2[0]);
  
  ctime=millis();
  t[0]=ctime-ltime;
  ltime=ctime;
  
  AccOut_RF(&a1[0], &a2[0], &t[0]);
}

void getAcc()
{
  float a1[3];
  float a2[3];
  float t[1];
  
  BNO055_3_vec(ACC_DATA_X_LSB, &a1[0]);
  //BNO055_3_vec_1(ACC_DATA_X_LSB, &a2[0]);
  
  ctime=millis();
  t[0]=ctime-ltime;
  ltime=ctime;
  
  AccOut(&a1[0], &a2[0], &t[0]);
}

void freeCubeAccDoubleRF()
{
  float a1[3];  //x, y, z
  float a2[3];
  BNO055_3_vec(LIA_DATA_X_LSB, &a1[0]);
  BNO055_3_vec_1(LIA_DATA_X_LSB, &a2[0]);
  
  freeIMUOut_accDoubleRF(&a1[0], &a2[0]);
}

void freeCubeAllDoubleRF()
{
  float a1[3];  //x, y, z
  float a2[3];
  //float q[4];
  float q1[4];
  float q2[4];
  
  //read acc
  BNO055_3_vec(LIA_DATA_X_LSB, &a1[0]);
  BNO055_3_vec_1(LIA_DATA_X_LSB, &a2[0]);
  
  //read quat
  //BNO055_4_vec(QUA_DATA_W_LSB, &q1[0]);
  BNO055_4_vec_1(QUA_DATA_W_LSB, &q2[0]);
  
  /*
  //get q
  //normalize the quaterions
  long m_q1=sqrt((q1[0]*q1[0])+(q1[1]*q1[1])+(q1[2]*q1[2])+(q1[3]*q1[3]));
  if(m_q1){ q1[0]/=m_q1; q1[1]/=m_q1; q1[2]/=m_q1; q1[3]/=m_q1;}
  long m_q2=sqrt((q2[0]*q2[0])+(q2[1]*q2[1])+(q2[2]*q2[2])+(q2[3]*q2[3]));
  if(m_q2){ q2[0]/=m_q2; q2[1]/=m_q2; q2[2]/=m_q2; q2[3]/=m_q2;}
  
  //conjugate q2;
  q1[1]=-q1[1]; q1[2]=-q1[2]; q1[3]=-q1[3];
  
  //multiply the quaternions
  q[0] = -q1[1] * q2[1] - q1[2] * q2[2] - q1[3] * q2[3] + q1[0] * q2[0];
  q[1] =  q1[1] * q2[0] + q1[2] * q2[3] - q1[3] * q2[2] + q1[0] * q2[1];
  q[2] = -q1[1] * q2[3] + q1[2] * q2[0] + q1[3] * q2[1] + q1[0] * q2[2];
  q[3] =  q1[1] * q2[2] - q1[2] * q2[1] + q1[3] * q2[0] + q1[0] * q2[3];
  */
  
  //freeIMUOut_allDoubleRF(&a1[0], &a2[0], &q1[0], &q2[0]);
  freeIMUOut_threeDoubleRF(&a1[0], &a2[0], &q2[0]);
  
}

void BNO055_3_vec(int8_t addr, float *vec){
  Wire.beginTransmission(BNO055_I2C_ADDR);
  Wire.write(addr);
  Wire.endTransmission(false);

  Wire.requestFrom(BNO055_I2C_ADDR, 6, false);
  int16_t b_data[6];
  for(int i=0;i<6;i++)
    b_data[i]=Wire.read();
  
  vec[0] = b_data[1]<<8|b_data[0];
  vec[1] = b_data[3]<<8|b_data[2];
  vec[2] = b_data[5]<<8|b_data[4];
}

void BNO055_3_vec_1(int8_t addr, float *vec){
  Wire.beginTransmission(BNO055_I2C_H_ADDR);
  Wire.write(addr);
  Wire.endTransmission(false);

  Wire.requestFrom(BNO055_I2C_H_ADDR, 6, false);
  int16_t b_data[6];
  for(int i=0;i<6;i++)
    b_data[i]=Wire.read();
  
  vec[0] = b_data[1]<<8|b_data[0];
  vec[1] = b_data[3]<<8|b_data[2];
  vec[2] = b_data[5]<<8|b_data[4];
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

void BNO055_4_vec_1(int8_t addr, float *vec){
  float norm;
  Wire.beginTransmission(BNO055_I2C_H_ADDR);
  Wire.write(addr);
  Wire.endTransmission(false);

  Wire.requestFrom(BNO055_I2C_H_ADDR, 8, false);
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

byte BNO055_read(int addr){
  Wire.beginTransmission(BNO055_I2C_ADDR);
  Wire.write(addr);
  Wire.endTransmission(false);

  Wire.requestFrom(BNO055_I2C_ADDR, 1, true);
  return Wire.read();
}

int BNO055_write(int addr,int data){
  Wire.beginTransmission(BNO055_I2C_ADDR);
  Wire.write(addr);
  Wire.write(data);
  Wire.endTransmission(true);

  return 1;
}

int BNO055_write_1(int addr,int data){
  Wire.beginTransmission(BNO055_I2C_H_ADDR);
  Wire.write(addr);
  Wire.write(data);
  Wire.endTransmission(true);

  return 1;
}


void serialFloatPrint(float f) {
  byte * b = (byte *) &f;
  for(int i=0; i<4; i++) {
    
    byte b1 = (b[i] >> 4) & 0x0f;
    byte b2 = (b[i] & 0x0f);
 
    char c1 = (b1 < 10) ? ('0' + b1) : 'A' + b1 - 10;
    char c2 = (b2 < 10) ? ('0' + b2) : 'A' + b2 - 10;
    
    Serial.print(c1);
    Serial.print(c2);
  }
}

void freeIMUOut(float *quat){
  
  serialFloatPrint(quat[0]);
  Serial.print(",");
  serialFloatPrint(quat[1]);
  Serial.print(",");
  serialFloatPrint(quat[2]);
  Serial.print(",");
  serialFloatPrint(quat[3]);
  Serial.print(",\n");  
}

void freeIMUOut_3(float *quat){
  
  serialFloatPrint(quat[0]);
  Serial.print(",");
  serialFloatPrint(quat[1]);
  Serial.print(",");
  serialFloatPrint(quat[2]);
  Serial.print(",\n");  
}

void freeIMUOut_s(float *quat){
  
  serialFloatPrint(quat[0]);
  Serial.print(",");
  serialFloatPrint(quat[1]);
  Serial.print(",");
  serialFloatPrint(quat[2]);
  Serial.print(",");
  serialFloatPrint(quat[3]);
 
}

void freeIMUOutRF_multi(float *quat, int node){
  RF.beginTransmission();
  serialFloatPrintRF(quat[0]);
  RF.write(",");
  serialFloatPrintRF(quat[1]);
  RF.write(",");
  serialFloatPrintRF(quat[2]);
  RF.write(",");
  serialFloatPrintRF(quat[3]);
  RF.write(",");
  RF.write(node);
  RF.write(",\n");  
  RF.endTransmission();
}

void freeIMUOut_unity(float *quat, int node){
  Serial.print("0,Q,");  //zero indicates version
  Serial.print(node);
  Serial.print(",");    
  serialFloatPrint(quat[0]);
  Serial.print(",");
  serialFloatPrint(quat[1]);
  Serial.print(",");
  serialFloatPrint(quat[2]);
  Serial.print(",");
  serialFloatPrint(quat[3]);
  Serial.print(",\n");  
}

void freeIMUOutRF_unity(float *quat, int node){
  RF.beginTransmission();
  RF.write("0,Q,");  //zero indicates version
  RF.write(((node/10)%10)+48);
  RF.write((node%10)+48);
  RF.write(",");    
  serialFloatPrintRF(quat[0]);
  RF.write(",");
  serialFloatPrintRF(quat[1]);
  RF.write(",");
  serialFloatPrintRF(quat[2]);
  RF.write(",");
  serialFloatPrintRF(quat[3]);
  RF.write(",\n");  
  RF.endTransmission();
}

void freeIMUOutRF(float *quat){
  RF.beginTransmission();
  serialFloatPrintRF(quat[0]);
  RF.write(",");
  serialFloatPrintRF(quat[1]);
  RF.write(",");
  serialFloatPrintRF(quat[2]);
  RF.write(",");
  serialFloatPrintRF(quat[3]);
  RF.write(",\n");  
  RF.endTransmission();
}

void freeIMUOut_accRF(float *quat, float *acc){
  RF.beginTransmission();
  serialFloatPrintRF(quat[0]);
  RF.write(",");
  serialFloatPrintRF(quat[1]);
  RF.write(",");
  serialFloatPrintRF(quat[2]);
  RF.write(",");
  serialFloatPrintRF(quat[3]);
  RF.write(",");  
  serialFloatPrintRF(acc[0]);
  RF.write(",");
  serialFloatPrintRF(acc[1]);
  RF.write(",");
  serialFloatPrintRF(acc[2]);
  RF.write(",");
  serialFloatPrintRF(acc[3]);
  RF.write(",\n");  
  RF.endTransmission();
}

void freeIMUOut_accDoubleRF(float *a1, float *a2)
{
  RF.beginTransmission();
  serialFloatPrintRF(a1[0]);
  RF.write(",");
  serialFloatPrintRF(a1[1]);
  RF.write(",");
  serialFloatPrintRF(a1[2]);
  RF.write(",");
  serialFloatPrintRF(a2[0]);
  RF.write(",");
  serialFloatPrintRF(a2[1]);
  RF.write(",");
  serialFloatPrintRF(a2[2]);
  RF.write(",\n");  
  RF.endTransmission();
}

void freeIMUOut_allDoubleRF(float *a1, float *a2, float *q1, float *q2)
{
  RF.beginTransmission();
  serialFloatPrintRF(a1[0]);
  RF.write(",");
  serialFloatPrintRF(a1[1]);
  RF.write(",");
  serialFloatPrintRF(a1[2]);
  RF.write(",");
  serialFloatPrintRF(a2[0]);
  RF.write(",");
  serialFloatPrintRF(a2[1]);
  RF.write(",");
  serialFloatPrintRF(a2[2]);
  RF.write(",");
  serialFloatPrintRF(q1[0]);
  RF.write(",");
  serialFloatPrintRF(q1[1]);
  RF.write(",");
  serialFloatPrintRF(q1[2]);
  RF.write(",");
  serialFloatPrintRF(q1[3]);
  RF.write(",");
  serialFloatPrintRF(q2[0]);
  RF.write(",");
  serialFloatPrintRF(q2[1]);
  RF.write(",");
  serialFloatPrintRF(q2[2]);
  RF.write(",");
  serialFloatPrintRF(q2[3]);
  RF.write(",\n");  
  RF.endTransmission();
}

void freeIMUOut_threeDoubleRF(float *a1, float *a2, float *q)
{
  RF.beginTransmission();
  serialFloatPrintRF(a1[0]);
  RF.write(",");
  serialFloatPrintRF(a1[1]);
  RF.write(",");
  serialFloatPrintRF(a1[2]);
  RF.write(",");
  serialFloatPrintRF(a2[0]);
  RF.write(",");
  serialFloatPrintRF(a2[1]);
  RF.write(",");
  serialFloatPrintRF(a2[2]);
  RF.write(",");
  serialFloatPrintRF(q[0]);
  RF.write(",");
  serialFloatPrintRF(q[1]);
  RF.write(",");
  serialFloatPrintRF(q[2]);
  RF.write(",");
  serialFloatPrintRF(q[3]);
  RF.write(",\n");  
  RF.endTransmission();
}

void AccOut_RF(float *a1, float *a2, float *t)
{
  RF.beginTransmission();
  serialFloatPrintRF(a1[0]);
  RF.write(",");
  serialFloatPrintRF(a1[1]);
  RF.write(",");
  serialFloatPrintRF(a1[2]);
  RF.write(",");
  serialFloatPrintRF(a2[0]);
  RF.write(",");
  serialFloatPrintRF(a2[1]);
  RF.write(",");
  serialFloatPrintRF(a2[2]);
  RF.write(",");
  serialFloatPrintRF(t[0]);
  RF.write(",\n");  
  RF.endTransmission();
}

void AccOut(float *a1, float *a2, float *t)
{
  serialFloatPrint(a1[0]);
  Serial.print(",");
  serialFloatPrint(a1[1]);
  Serial.print(",");
  serialFloatPrint(a1[2]);
  Serial.print(",");
  serialFloatPrint(a2[0]);
  Serial.print(",");
  serialFloatPrint(a2[1]);
  Serial.print(",");
  serialFloatPrint(a2[2]);
  Serial.print(",");
  serialFloatPrint(t[0]);
  Serial.print(",\n");  
}


void serialFloatPrintRF(float f) {
  byte * b = (byte *) &f;
  for(int i=0; i<4; i++) {
    
    byte b1 = (b[i] >> 4) & 0x0f;
    byte b2 = (b[i] & 0x0f);
    
    char c1 = (b1 < 10) ? ('0' + b1) : 'A' + b1 - 10;
    char c2 = (b2 < 10) ? ('0' + b2) : 'A' + b2 - 10;
    
    RF.write(c1);
    RF.write(c2);
  }
}




