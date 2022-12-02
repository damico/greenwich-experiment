# greenwich-experiment
RF Clock based on GPS receivers

## Introduction
This is a hardware and software project, and the main idea behind it is to have a clock with time would be collected by 2 different GPS receivers (Which I call receiver A and receiver B).

### Main board
The hardware is based on a BeagleBone Black board, which uses [AM3358 Debian 10.3 2020-04-06 4GB SD IoT] (https://debian.beagleboard.org/images/bone-debian-10.3-iot-armhf-2020-04-06-4gb.img.xz) image.

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
The Ublox modules talks with BeagleBone Black using UARTs. In this project the UARTs used are: 
 - UART1 (Pins P9_26 RX and	P9_24 TX)	/dev/ttyO1
 - UART4 (Pins P9_11 RX and	P9_13 TX)	/dev/ttyO4 

However these UARTs needs to be accessible. For that, the uEnv.txt file of BeagleBone Black must be edited with this line `capemgr.enable_partno=BB-UART1,BB-UART4`

The main software that controls the GPS receiver is the GPSD, which is available on Debian 10. There are 2 main configurations in GPSD:
 - Receivers conf at: 
 - TCP conf at:

## User Interface
The UI is done through the 16X2 LCD Display, using I2C. 
The software which controls de LCD is written in Python 3 and was based on the work of MilesBDyson, which can be found here: https://github.com/MilesBDyson/I2C-LCD/blob/master/lcd_i2c.py
