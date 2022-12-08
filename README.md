# greenwich-experiment
Radio-Controlled Clock based on GPS receivers

<img src="https://github.com/damico/greenwich-experiment/blob/main/design/model.png?raw=true" alt="Working hardware" width="500"/>

## Introduction
This is a hardware and software project, and the main idea behind it is to have a clock with time would be collected by 2 different GPS receivers (Which I call receiver A and receiver B). **This is not a GPS disciplined oscillator.**

If you want to know more about  Radio-Controlled Clocks, there is an excelent text written by Michael A. Lombardi from
 National Institute of Standards and Technology (NIST), which can be found here: https://tf.nist.gov/general/pdf/1877.pdf

### Main board
The hardware is based on a BeagleBone Black (BBB) board, which uses [AM3358 Debian 10.3 2020-04-06 4GB SD IoT] (https://debian.beagleboard.org/images/bone-debian-10.3-iot-armhf-2020-04-06-4gb.img.xz) image. 

For Debian image, with SD Cards bigger than 4GB, expand your file system to be able to use all SD Card space `sudo /opt/scripts/tools/grow_partition.sh`. Also to reduce power consumption is important to decrease the cpu frequency. It can be done by editing file **/etc/init.d/cpufrequtils** and changing governor key to `GOVERNOR="powersave"` and then running the following command `systemctl daemon-reload`. Other thing to decrease power consumption is disable HDMI and AUDIO, and this can be done by updating **/boot/uEnv.txt** and adding the following line: `cape_disable=capemgr.disable_partno=BB-BONELT-HDMI,BB-BONELT-HDMIN` and `disable_uboot_overlay_video=1` and `disable_uboot_overlay_audio=1`.



<img src="https://user-images.githubusercontent.com/692043/205400812-4b35fe3a-920d-423e-b293-6fd904ef0984.png" alt="BBB" width="200"/>

### GPS receiver A
Ublox M8N

<img src="https://user-images.githubusercontent.com/692043/205401292-2b7f3ea4-0b7f-4390-afd9-4a98d243ec27.png" alt="Ublox M8N" width="200"/>

### GPS receiver B
Ublox Neo 6 MV2

<img src="https://user-images.githubusercontent.com/692043/205401515-2606b607-28cd-438b-b872-fd02c1031190.png" alt="Ublox Neo 6 MV2" width="200"/>


### LCD Display 16X2 (Wich I2C Board)

<img src="https://user-images.githubusercontent.com/692043/205401788-60b35172-80ee-4bb6-97ca-ccf6f1b5782d.png" alt="LCD Display 16X2 (Wich I2C Board)" width="200"/>


## GPS receivers communication
The Ublox modules talks with BeagleBone Black (BBB) using UARTs. In this project the UARTs used are: 
 - UART1 (Pins P9_26 RX and	P9_24 TX)	/dev/ttyO1
 - UART4 (Pins P9_11 RX and	P9_13 TX)	/dev/ttyO4 

However these UARTs needs to be accessible. For that, the /boot/uEnv.txt file of BeagleBone Black must be edited with this line `capemgr.enable_partno=BB-UART1,BB-UART4` and with these other 2: `uboot_overlay_addr0=/lib/firmware/BB-UART1-00A0.dtbo`, `uboot_overlay_addr2=/lib/firmware/BB-UART4-00A0.dtbo`


The main software that controls the GPS receiver is the GPSD, which is available on Debian 10. 
The installation of GPSD is done by: `sudo apt-get install gpsd`.
There are 2 main configurations in GPSD:
 - Receivers conf at **/etc/default/gpsd** where devices must be declared as `DEVICES="/dev/ttyO1 /dev/ttyO4"` and options as `GPSD_OPTIONS="-G"`
 - TCP conf at **/lib/systemd/system/gpsd.socket** where ListenStream must be declared as `ListenStream=0.0.0.0:2947`

## Enclosure
The enclusure was projected in FreeCad and 3D printed:

![image](https://user-images.githubusercontent.com/692043/205404929-21c9e014-b176-4da3-8cc0-6f2511cbea2a.png)

The LCD part was based on this work: https://www.thingiverse.com/thing:614241

## Power Supply
All boards work fine with USB power, but I've also added support for 3.7V 18650 battery, using the battery pins (TP5, TP8) of BBB:
![image](https://user-images.githubusercontent.com/692043/205405374-4c6b6b5f-155f-4b55-976a-73207c21117d.png)


## User Interface
The UI is done through the 16X2 LCD Display, using I2C. 
The software which controls de LCD is written in Python 3 and was based on the work of MilesBDyson, which can be found here: https://github.com/MilesBDyson/I2C-LCD/blob/master/lcd_i2c.py . At **python/** folder there is a lcd_ui.py file which handles the messages into the display. This code requires **psutil** module (`pip3 install psutil`).
