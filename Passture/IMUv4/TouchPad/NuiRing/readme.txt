String BT200 = "8833145F1775"; (Teng's)
String BT200SK = "847e40a2e8a7"  (softkinetic moverio)
String HT08 = "000272d6a244";


-connect to bt module through arduino serial monitor - use lowest com port (e.g. COM4 Outgoing 'RNI-SPP' COM5 Incoming -> choose COM4)
-To enter command mode type '$$$' with No line ending- enter remaining commands with line endings

-Enter the commands below before uploading the NuiRing script

-If loading code over BT, baud rate must be 57600



* Before assembly, need to configue the BT module as following:
 *      Command Response                Comment
 *      $$$                             Enter command mode for BT module
 *              CMD
 *		SN,<friendly name>				Set the name of the accessory to something you want
 *				AOK						For example SO,MyPi will set the BT device name to MyPi. This is how your phone will see it
 *      SR,<phone BT MAC address>       Set the default device to connect to**
 *              AOK
 
 *      SO,Z                            We want to get CONNECT and DISCONNECT messages from BT module
 *              AOK
 *		SJ,0200			Increase page scan time for compatability with Android devices
 *				AOK
 *		SI,0200			Increase inquiry scan time for compatability with Android devices
 *				AOK
 *		SU,576K			Set baud rate to 9600. With SoftwareSerial, you can't use 115200, it is totally not relaible
 *				AOK

 *              SA,0			Turns off authentication

 *		R,1			Reboot the unit. After reboot all parameters will take affect, including the new baud rate
 *				Reboot!



//This is what readout should show (command 'd')
BTA=000666726A9E
BTName=NUIRING1
Baudrt=57.6
Mode  =Slav
Authen=0
PinCod=1234
Bonded=0
Rem=847E40A2E8A7