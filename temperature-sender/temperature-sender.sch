EESchema Schematic File Version 2
LIBS:power
LIBS:device
LIBS:transistors
LIBS:conn
LIBS:linear
LIBS:regul
LIBS:74xx
LIBS:cmos4000
LIBS:adc-dac
LIBS:memory
LIBS:xilinx
LIBS:microcontrollers
LIBS:dsp
LIBS:microchip
LIBS:analog_switches
LIBS:motorola
LIBS:texas
LIBS:intel
LIBS:audio
LIBS:interface
LIBS:digital-audio
LIBS:philips
LIBS:display
LIBS:cypress
LIBS:siliconi
LIBS:opto
LIBS:atmel
LIBS:contrib
LIBS:valves
LIBS:kicad-components
LIBS:temperature-sender-cache
EELAYER 25 0
EELAYER END
$Descr A4 11693 8268
encoding utf-8
Sheet 1 1
Title ""
Date ""
Rev ""
Comp ""
Comment1 ""
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
$Comp
L CONN_01X02 P2
U 1 1 5548B5B0
P 750 1950
F 0 "P2" H 750 2100 50  0000 C CNN
F 1 "CONN_01X02" V 850 1950 50  0000 C CNN
F 2 "kicad-footprints:Pin_Header_Angled_SMD_1x02" H 750 1950 60  0001 C CNN
F 3 "" H 750 1950 60  0000 C CNN
	1    750  1950
	-1   0    0    1   
$EndComp
$Comp
L D D1
U 1 1 5548B793
P 1400 1900
F 0 "D1" H 1500 1850 50  0000 C CNN
F 1 "D" H 1400 1800 50  0001 C CNN
F 2 "kicad-footprints:SOD-80" H 1400 1900 60  0001 C CNN
F 3 "" H 1400 1900 60  0000 C CNN
	1    1400 1900
	-1   0    0    1   
$EndComp
$Comp
L GND #PWR8
U 1 1 5548BA01
P 3350 2850
F 0 "#PWR8" H 3350 2600 50  0001 C CNN
F 1 "GND" H 3350 2700 50  0000 C CNN
F 2 "" H 3350 2850 60  0000 C CNN
F 3 "" H 3350 2850 60  0000 C CNN
	1    3350 2850
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR12
U 1 1 5548BB27
P 4550 2300
F 0 "#PWR12" H 4550 2050 50  0001 C CNN
F 1 "GND" H 4550 2150 50  0000 C CNN
F 2 "" H 4550 2300 60  0000 C CNN
F 3 "" H 4550 2300 60  0000 C CNN
	1    4550 2300
	1    0    0    -1  
$EndComp
$Comp
L C C2
U 1 1 5548BB4A
P 5050 2100
F 0 "C2" H 5075 2200 50  0000 L CNN
F 1 "10uF" H 5075 2000 50  0000 L CNN
F 2 "Capacitors_SMD:C_0603_HandSoldering" H 5088 1950 30  0001 C CNN
F 3 "" H 5050 2100 60  0000 C CNN
	1    5050 2100
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR15
U 1 1 5548BC6D
P 5050 2300
F 0 "#PWR15" H 5050 2050 50  0001 C CNN
F 1 "GND" H 5050 2150 50  0000 C CNN
F 2 "" H 5050 2300 60  0000 C CNN
F 3 "" H 5050 2300 60  0000 C CNN
	1    5050 2300
	1    0    0    -1  
$EndComp
Text Label 1700 4050 0    60   ~ 0
SCL
Text Label 1650 4350 0    60   ~ 0
GND
$Comp
L GND #PWR5
U 1 1 5548BF68
P 1950 4450
F 0 "#PWR5" H 1950 4200 50  0001 C CNN
F 1 "GND" H 1950 4300 50  0000 C CNN
F 2 "" H 1950 4450 60  0000 C CNN
F 3 "" H 1950 4450 60  0000 C CNN
	1    1950 4450
	1    0    0    -1  
$EndComp
Text Notes 1350 3950 0    60   ~ 0
Temp sensor
$Comp
L +3.3V #PWR14
U 1 1 5549F94B
P 5050 1650
F 0 "#PWR14" H 5050 1500 50  0001 C CNN
F 1 "+3.3V" H 5050 1790 50  0000 C CNN
F 2 "" H 5050 1650 60  0000 C CNN
F 3 "" H 5050 1650 60  0000 C CNN
	1    5050 1650
	1    0    0    -1  
$EndComp
$Comp
L SW_PUSH SW2
U 1 1 554A020B
P 4500 3800
F 0 "SW2" H 4400 3950 50  0000 C CNN
F 1 "SW_PUSH" H 4500 3720 50  0001 C CNN
F 2 "kicad-footprints:4x4x1.5mm_Tactile_Push_Button_SMD" H 4500 3800 60  0001 C CNN
F 3 "" H 4500 3800 60  0000 C CNN
	1    4500 3800
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR11
U 1 1 554A02D6
P 4050 4000
F 0 "#PWR11" H 4050 3750 50  0001 C CNN
F 1 "GND" H 4050 3850 50  0000 C CNN
F 2 "" H 4050 4000 60  0000 C CNN
F 3 "" H 4050 4000 60  0000 C CNN
	1    4050 4000
	1    0    0    -1  
$EndComp
$Comp
L C C10
U 1 1 554A0362
P 4500 3950
F 0 "C10" V 4600 4050 50  0000 L CNN
F 1 "0.1uF" V 4650 3800 50  0000 L CNN
F 2 "Capacitors_SMD:C_0603_HandSoldering" H 4538 3800 30  0001 C CNN
F 3 "" H 4500 3950 60  0000 C CNN
	1    4500 3950
	0    1    1    0   
$EndComp
$Comp
L Led_Small D3
U 1 1 554A2817
P 5500 2150
F 0 "D3" H 5450 2275 50  0000 L CNN
F 1 "Led_Small" H 5325 2050 50  0001 L CNN
F 2 "LEDs:LED-0603" V 5500 2150 60  0001 C CNN
F 3 "" V 5500 2150 60  0000 C CNN
	1    5500 2150
	0    -1   -1   0   
$EndComp
$Comp
L GND #PWR17
U 1 1 554A2AAD
P 5500 2300
F 0 "#PWR17" H 5500 2050 50  0001 C CNN
F 1 "GND" H 5500 2150 50  0000 C CNN
F 2 "" H 5500 2300 60  0000 C CNN
F 3 "" H 5500 2300 60  0000 C CNN
	1    5500 2300
	1    0    0    -1  
$EndComp
$Comp
L R R2
U 1 1 554A2B86
P 5500 1850
F 0 "R2" H 5600 1900 50  0000 C CNN
F 1 "330R" V 5500 1850 50  0000 C CNN
F 2 "Resistors_SMD:R_0603_HandSoldering" V 5430 1850 30  0001 C CNN
F 3 "" H 5500 1850 30  0000 C CNN
	1    5500 1850
	1    0    0    -1  
$EndComp
$Comp
L +3.3V #PWR16
U 1 1 554A2EE2
P 5500 1650
F 0 "#PWR16" H 5500 1500 50  0001 C CNN
F 1 "+3.3V" H 5500 1790 50  0000 C CNN
F 2 "" H 5500 1650 60  0000 C CNN
F 3 "" H 5500 1650 60  0000 C CNN
	1    5500 1650
	1    0    0    -1  
$EndComp
$Comp
L SW_PUSH SW1
U 1 1 554A3892
P 7100 1950
F 0 "SW1" H 7000 2100 50  0000 C CNN
F 1 "SW_PUSH" H 7100 1870 50  0001 C CNN
F 2 "kicad-footprints:4x4x1.5mm_Tactile_Push_Button_SMD" H 7100 1950 60  0001 C CNN
F 3 "" H 7100 1950 60  0000 C CNN
	1    7100 1950
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR22
U 1 1 554A3898
P 7500 2200
F 0 "#PWR22" H 7500 1950 50  0001 C CNN
F 1 "GND" H 7500 2050 50  0000 C CNN
F 2 "" H 7500 2200 60  0000 C CNN
F 3 "" H 7500 2200 60  0000 C CNN
	1    7500 2200
	1    0    0    -1  
$EndComp
$Comp
L C C3
U 1 1 554A389E
P 7100 2100
F 0 "C3" V 7200 2200 50  0000 L CNN
F 1 "0.1uF" V 7250 1950 50  0000 L CNN
F 2 "Capacitors_SMD:C_0603_HandSoldering" H 7138 1950 30  0001 C CNN
F 3 "" H 7100 2100 60  0000 C CNN
	1    7100 2100
	0    1    1    0   
$EndComp
$Comp
L R R1
U 1 1 554A3CFE
P 6650 1700
F 0 "R1" H 6750 1550 50  0000 C CNN
F 1 "47k" V 6650 1700 50  0000 C CNN
F 2 "Resistors_SMD:R_0603_HandSoldering" V 6580 1700 30  0001 C CNN
F 3 "" H 6650 1700 30  0000 C CNN
	1    6650 1700
	1    0    0    -1  
$EndComp
$Comp
L +3.3V #PWR18
U 1 1 554A3EBB
P 6650 1500
F 0 "#PWR18" H 6650 1350 50  0001 C CNN
F 1 "+3.3V" H 6650 1640 50  0000 C CNN
F 2 "" H 6650 1500 60  0000 C CNN
F 3 "" H 6650 1500 60  0000 C CNN
	1    6650 1500
	1    0    0    -1  
$EndComp
$Comp
L R R7
U 1 1 554A4116
P 4900 3600
F 0 "R7" H 4750 3500 50  0000 C CNN
F 1 "47k" V 4900 3600 50  0000 C CNN
F 2 "Resistors_SMD:R_0603_HandSoldering" V 4830 3600 30  0001 C CNN
F 3 "" H 4900 3600 30  0000 C CNN
	1    4900 3600
	1    0    0    -1  
$EndComp
$Comp
L +3.3V #PWR13
U 1 1 554A435D
P 4900 3400
F 0 "#PWR13" H 4900 3250 50  0001 C CNN
F 1 "+3.3V" H 4900 3540 50  0000 C CNN
F 2 "" H 4900 3400 60  0000 C CNN
F 3 "" H 4900 3400 60  0000 C CNN
	1    4900 3400
	1    0    0    -1  
$EndComp
Text Label 8250 4300 0    60   ~ 0
SDA/MOSI
$Comp
L R R8
U 1 1 554A4D12
P 9050 4300
F 0 "R8" V 9150 4200 50  0000 C CNN
F 1 "5k" V 9050 4300 50  0000 C CNN
F 2 "Resistors_SMD:R_0603_HandSoldering" V 8980 4300 30  0001 C CNN
F 3 "" H 9050 4300 30  0000 C CNN
	1    9050 4300
	0    1    1    0   
$EndComp
$Comp
L +3.3V #PWR24
U 1 1 554A4E17
P 9250 4150
F 0 "#PWR24" H 9250 4000 50  0001 C CNN
F 1 "+3.3V" H 9250 4290 50  0000 C CNN
F 2 "" H 9250 4150 60  0000 C CNN
F 3 "" H 9250 4150 60  0000 C CNN
	1    9250 4150
	1    0    0    -1  
$EndComp
$Comp
L R R9
U 1 1 554A57EE
P 9050 4500
F 0 "R9" V 9150 4400 50  0000 C CNN
F 1 "5k" V 9050 4500 50  0000 C CNN
F 2 "Resistors_SMD:R_0603_HandSoldering" V 8980 4500 30  0001 C CNN
F 3 "" H 9050 4500 30  0000 C CNN
	1    9050 4500
	0    1    1    0   
$EndComp
Text Label 8650 4500 0    60   ~ 0
SCL
$Comp
L SERVO K1
U 1 1 554A7C01
P 10400 2750
F 0 "K1" H 10350 2550 50  0000 C CNN
F 1 "SERVO" H 10400 2950 40  0000 C CNN
F 2 "kicad-footprints:Pin_Header_Angled_SMD_1x03" H 10400 2750 60  0001 C CNN
F 3 "" H 10400 2750 60  0000 C CNN
	1    10400 2750
	1    0    0    1   
$EndComp
$Comp
L GND #PWR27
U 1 1 554A7F9C
P 9950 3300
F 0 "#PWR27" H 9950 3050 50  0001 C CNN
F 1 "GND" H 9950 3150 50  0000 C CNN
F 2 "" H 9950 3300 60  0000 C CNN
F 3 "" H 9950 3300 60  0000 C CNN
	1    9950 3300
	1    0    0    -1  
$EndComp
Text Label 9600 2750 0    60   ~ 0
SERVO_PWR
Text Label 8900 2650 0    60   ~ 0
SERVO1
Text Label 10400 3650 0    60   ~ 0
RX
Text Label 10400 3750 0    60   ~ 0
TX
$Comp
L GND #PWR29
U 1 1 554A8DA0
P 10400 3900
F 0 "#PWR29" H 10400 3650 50  0001 C CNN
F 1 "GND" H 10400 3750 50  0000 C CNN
F 2 "" H 10400 3900 60  0000 C CNN
F 3 "" H 10400 3900 60  0000 C CNN
	1    10400 3900
	1    0    0    -1  
$EndComp
Text Label 8350 4800 0    60   ~ 0
TX
Text Label 8350 4700 0    60   ~ 0
RX
$Comp
L ATTINY2313A-S IC1
U 1 1 554CF2B1
P 6800 4600
F 0 "IC1" H 5650 5600 40  0000 C CNN
F 1 "ATTINY2313A-S" H 7750 3700 40  0000 C CNN
F 2 "Housings_SOIC:SOIC-20_7.5x12.8mm_Pitch1.27mm" H 6800 4600 35  0001 C CIN
F 3 "" H 6800 4600 60  0000 C CNN
	1    6800 4600
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR20
U 1 1 5555E709
P 6800 5700
F 0 "#PWR20" H 6800 5450 50  0001 C CNN
F 1 "GND" H 6800 5550 50  0000 C CNN
F 2 "" H 6800 5700 60  0000 C CNN
F 3 "" H 6800 5700 60  0000 C CNN
	1    6800 5700
	1    0    0    -1  
$EndComp
$Comp
L +3.3V #PWR19
U 1 1 5555E869
P 6800 3000
F 0 "#PWR19" H 6800 2850 50  0001 C CNN
F 1 "+3.3V" H 6800 3140 50  0000 C CNN
F 2 "" H 6800 3000 60  0000 C CNN
F 3 "" H 6800 3000 60  0000 C CNN
	1    6800 3000
	1    0    0    -1  
$EndComp
Text Label 8250 4400 0    60   ~ 0
MISO
Text Label 5150 3800 0    60   ~ 0
RESET
$Comp
L CONN_02X03 P1
U 1 1 5556E050
P 9250 1400
F 0 "P1" H 9250 1600 50  0000 C CNN
F 1 "ISP" H 9250 1200 50  0000 C CNN
F 2 "Socket_Strips:Socket_Strip_Straight_2x03" H 9250 200 60  0001 C CNN
F 3 "" H 9250 200 60  0000 C CNN
	1    9250 1400
	1    0    0    -1  
$EndComp
Text Label 8700 1300 0    60   ~ 0
MISO
$Comp
L GND #PWR26
U 1 1 5556E2E2
P 9600 1550
F 0 "#PWR26" H 9600 1300 50  0001 C CNN
F 1 "GND" H 9600 1400 50  0000 C CNN
F 2 "" H 9600 1550 60  0000 C CNN
F 3 "" H 9600 1550 60  0000 C CNN
	1    9600 1550
	1    0    0    -1  
$EndComp
$Comp
L +3.3V #PWR25
U 1 1 5556E31F
P 9600 1250
F 0 "#PWR25" H 9600 1100 50  0001 C CNN
F 1 "+3.3V" H 9600 1390 50  0000 C CNN
F 2 "" H 9600 1250 60  0000 C CNN
F 3 "" H 9600 1250 60  0000 C CNN
	1    9600 1250
	1    0    0    -1  
$EndComp
Text Label 8700 1400 0    60   ~ 0
SCL
Text Label 1700 4150 0    60   ~ 0
SDA/MOSI
Text Label 9500 1400 0    60   ~ 0
SDA/MOSI
Text Label 8700 1500 0    60   ~ 0
RESET
$Comp
L TEST_POINT TP_XTAL1
U 1 1 55570FD2
P 5350 4100
F 0 "TP_XTAL1" H 5500 4000 60  0000 C CNN
F 1 "TEST_POINT" H 5450 4200 60  0001 C CNN
F 2 "kicad-footprints:TP_1mm" H 5350 4100 60  0001 C CNN
F 3 "" H 5350 4100 60  0000 C CNN
	1    5350 4100
	-1   0    0    1   
$EndComp
$Comp
L TEST_POINT TP_XTAL2
U 1 1 55571059
P 5350 4300
F 0 "TP_XTAL2" H 5500 4400 60  0000 C CNN
F 1 "TEST_POINT" H 5450 4400 60  0001 C CNN
F 2 "kicad-footprints:TP_1mm" H 5350 4300 60  0001 C CNN
F 3 "" H 5350 4300 60  0000 C CNN
	1    5350 4300
	-1   0    0    1   
$EndComp
$Comp
L TMP102 U3
U 1 1 555721A6
P 1750 6400
F 0 "U3" H 1550 6050 60  0000 C CNN
F 1 "TMP102" H 1950 6050 60  0000 C CNN
F 2 "kicad-footprints:SOT563" H 1750 6400 60  0001 C CNN
F 3 "" H 1750 6400 60  0000 C CNN
	1    1750 6400
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR4
U 1 1 5557239D
P 1850 7650
F 0 "#PWR4" H 1850 7400 50  0001 C CNN
F 1 "GND" H 1850 7500 50  0000 C CNN
F 2 "" H 1850 7650 60  0000 C CNN
F 3 "" H 1850 7650 60  0000 C CNN
	1    1850 7650
	1    0    0    -1  
$EndComp
$Comp
L +3.3V #PWR2
U 1 1 55572418
P 1650 5100
F 0 "#PWR2" H 1650 4950 50  0001 C CNN
F 1 "+3.3V" H 1650 5240 50  0000 C CNN
F 2 "" H 1650 5100 60  0000 C CNN
F 3 "" H 1650 5100 60  0000 C CNN
	1    1650 5100
	1    0    0    -1  
$EndComp
Text Notes 2350 5900 0    60   ~ 0
ADDR0 to V+ =>\nDevice address = 1001001
Text Label 2150 6450 0    60   ~ 0
ALERT
$Comp
L R R10
U 1 1 55572BC6
P 2550 6200
F 0 "R10" V 2450 6100 50  0000 C CNN
F 1 "5k" V 2550 6200 50  0000 C CNN
F 2 "Resistors_SMD:R_0603_HandSoldering" V 2480 6200 30  0001 C CNN
F 3 "" H 2550 6200 30  0000 C CNN
	1    2550 6200
	-1   0    0    1   
$EndComp
Text Label 8200 4900 0    60   ~ 0
ALERT
Text Label 900  6350 0    60   ~ 0
SCL
Text Label 900  6450 0    60   ~ 0
SDA/MOSI
$Comp
L CONN_01X03 P3
U 1 1 55573F1A
P 10800 3750
F 0 "P3" H 10800 3950 50  0000 C CNN
F 1 "CONN_01X03" V 10900 3750 50  0000 C CNN
F 2 "kicad-footprints:Pin_Header_Angled_SMD_1x03" H 10800 3750 60  0001 C CNN
F 3 "" H 10800 3750 60  0000 C CNN
	1    10800 3750
	1    0    0    1   
$EndComp
Text Label 8150 4100 0    60   ~ 0
SERVO1
$Comp
L R R5
U 1 1 55570BA3
P 9400 2650
F 0 "R5" V 9300 2600 50  0000 C CNN
F 1 "47R" V 9400 2650 50  0000 C CNN
F 2 "Resistors_SMD:R_0603_HandSoldering" V 9330 2650 30  0001 C CNN
F 3 "" H 9400 2650 30  0000 C CNN
	1    9400 2650
	0    1    1    0   
$EndComp
$Comp
L CONN_01X03 P5
U 1 1 55571B8F
P 10750 4900
F 0 "P5" H 10750 5100 50  0000 C CNN
F 1 "CONN_01X03" V 10850 4900 50  0000 C CNN
F 2 "kicad-footprints:Pin_Header_Angled_SMD_1x03" H 10750 4900 60  0001 C CNN
F 3 "" H 10750 4900 60  0000 C CNN
	1    10750 4900
	1    0    0    1   
$EndComp
Text Notes 10400 3450 0    60   ~ 0
Debug UART
Text Notes 10300 4600 0    60   ~ 0
433MHz Tx module
$Comp
L GND #PWR30
U 1 1 55571DF7
P 10450 5050
F 0 "#PWR30" H 10450 4800 50  0001 C CNN
F 1 "GND" H 10450 4900 50  0000 C CNN
F 2 "" H 10450 5050 60  0000 C CNN
F 3 "" H 10450 5050 60  0000 C CNN
	1    10450 5050
	1    0    0    -1  
$EndComp
Text Label 10200 4800 0    60   ~ 0
RF_DATA
$Comp
L +3.3V #PWR6
U 1 1 55575B52
P 2450 4050
F 0 "#PWR6" H 2450 3900 50  0001 C CNN
F 1 "+3.3V" H 2450 4190 50  0000 C CNN
F 2 "" H 2450 4050 60  0000 C CNN
F 3 "" H 2450 4050 60  0000 C CNN
	1    2450 4050
	1    0    0    -1  
$EndComp
$Comp
L TEST_POINT TP_PB2
U 1 1 55576764
P 8450 3900
F 0 "TP_PB2" H 8850 3900 60  0000 C CNN
F 1 "TEST_POINT" H 8550 4000 60  0001 C CNN
F 2 "kicad-footprints:TP_1mm" H 8450 3900 60  0001 C CNN
F 3 "" H 8450 3900 60  0000 C CNN
	1    8450 3900
	1    0    0    -1  
$EndComp
$Comp
L TEST_POINT TP_PD1
U 1 1 55576B82
P 8450 5100
F 0 "TP_PD1" H 8850 5100 60  0000 C CNN
F 1 "TEST_POINT" H 8550 5200 60  0001 C CNN
F 2 "kicad-footprints:TP_1mm" H 8450 5100 60  0001 C CNN
F 3 "" H 8450 5100 60  0000 C CNN
	1    8450 5100
	1    0    0    -1  
$EndComp
$Comp
L TEST_POINT TP_PD2
U 1 1 55576C2C
P 8450 5200
F 0 "TP_PD2" H 8850 5200 60  0000 C CNN
F 1 "TEST_POINT" H 8550 5300 60  0001 C CNN
F 2 "kicad-footprints:TP_1mm" H 8450 5200 60  0001 C CNN
F 3 "" H 8450 5200 60  0000 C CNN
	1    8450 5200
	1    0    0    -1  
$EndComp
$Comp
L TEST_POINT TP_PD3
U 1 1 55576CCE
P 8450 5300
F 0 "TP_PD3" H 8850 5300 60  0000 C CNN
F 1 "TEST_POINT" H 8550 5400 60  0001 C CNN
F 2 "kicad-footprints:TP_1mm" H 8450 5300 60  0001 C CNN
F 3 "" H 8450 5300 60  0000 C CNN
	1    8450 5300
	1    0    0    -1  
$EndComp
$Comp
L TEST_POINT TP_PB3
U 1 1 555771A3
P 8450 4200
F 0 "TP_PB3" H 8850 4200 60  0000 C CNN
F 1 "TEST_POINT" H 8550 4300 60  0001 C CNN
F 2 "kicad-footprints:TP_1mm" H 8450 4200 60  0001 C CNN
F 3 "" H 8450 4200 60  0000 C CNN
	1    8450 4200
	1    0    0    -1  
$EndComp
$Comp
L R R6
U 1 1 5557765D
P 9400 2750
F 0 "R6" V 9500 2700 50  0000 C CNN
F 1 "47R" V 9400 2750 50  0000 C CNN
F 2 "Resistors_SMD:R_0603_HandSoldering" V 9330 2750 30  0001 C CNN
F 3 "" H 9400 2750 30  0000 C CNN
	1    9400 2750
	0    1    1    0   
$EndComp
$Comp
L +3.3V #PWR23
U 1 1 55577805
P 8650 2600
F 0 "#PWR23" H 8650 2450 50  0001 C CNN
F 1 "+3.3V" H 8650 2740 50  0000 C CNN
F 2 "" H 8650 2600 60  0000 C CNN
F 3 "" H 8650 2600 60  0000 C CNN
	1    8650 2600
	1    0    0    -1  
$EndComp
$Comp
L PWR_FLAG #FLG1
U 1 1 55578929
P 9050 3100
F 0 "#FLG1" H 9050 3195 50  0001 C CNN
F 1 "PWR_FLAG" H 9050 3280 50  0000 C CNN
F 2 "" H 9050 3100 60  0000 C CNN
F 3 "" H 9050 3100 60  0000 C CNN
	1    9050 3100
	1    0    0    -1  
$EndComp
$Comp
L C C8
U 1 1 55578A47
P 9700 3050
F 0 "C8" H 9725 3150 50  0000 L CNN
F 1 "10uF" H 9725 2950 50  0000 L CNN
F 2 "Capacitors_SMD:C_0603_HandSoldering" H 9738 2900 30  0001 C CNN
F 3 "" H 9700 3050 60  0000 C CNN
	1    9700 3050
	1    0    0    -1  
$EndComp
Text Label 8150 4000 0    60   ~ 0
RF_DATA
$Comp
L GND #PWR7
U 1 1 55679CC3
P 2950 2850
F 0 "#PWR7" H 2950 2600 50  0001 C CNN
F 1 "GND" H 2950 2700 50  0000 C CNN
F 2 "" H 2950 2850 60  0000 C CNN
F 3 "" H 2950 2850 60  0000 C CNN
	1    2950 2850
	1    0    0    -1  
$EndComp
$Comp
L TEST_POINT TP_PB1
U 1 1 55576573
P 8450 3800
F 0 "TP_PB1" H 8850 3800 60  0000 C CNN
F 1 "TEST_POINT" H 8550 3900 60  0001 C CNN
F 2 "kicad-footprints:TP_1mm" H 8450 3800 60  0001 C CNN
F 3 "" H 8450 3800 60  0000 C CNN
	1    8450 3800
	1    0    0    -1  
$EndComp
$Comp
L D D2
U 1 1 5579DCA7
P 1400 2000
F 0 "D2" H 1500 1950 50  0000 C CNN
F 1 "D" H 1400 1900 50  0001 C CNN
F 2 "kicad-footprints:SOD-80" H 1400 2000 60  0001 C CNN
F 3 "" H 1400 2000 60  0000 C CNN
	1    1400 2000
	-1   0    0    1   
$EndComp
$Comp
L D D4
U 1 1 5579E1D7
P 1050 2200
F 0 "D4" H 1150 2150 50  0000 C CNN
F 1 "D" H 1050 2100 50  0001 C CNN
F 2 "kicad-footprints:SOD-80" H 1050 2200 60  0001 C CNN
F 3 "" H 1050 2200 60  0000 C CNN
	1    1050 2200
	0    1    1    0   
$EndComp
$Comp
L D D5
U 1 1 5579E282
P 1150 2200
F 0 "D5" H 1250 2150 50  0000 C CNN
F 1 "D" H 1150 2100 50  0001 C CNN
F 2 "kicad-footprints:SOD-80" H 1150 2200 60  0001 C CNN
F 3 "" H 1150 2200 60  0000 C CNN
	1    1150 2200
	0    1    1    0   
$EndComp
$Comp
L GND #PWR1
U 1 1 5579E36A
P 1100 2850
F 0 "#PWR1" H 1100 2600 50  0001 C CNN
F 1 "GND" H 1100 2700 50  0000 C CNN
F 2 "" H 1100 2850 60  0000 C CNN
F 3 "" H 1100 2850 60  0000 C CNN
	1    1100 2850
	1    0    0    -1  
$EndComp
$Comp
L C C1
U 1 1 557A0613
P 1850 2100
F 0 "C1" H 1875 2200 50  0000 L CNN
F 1 "1uF" H 1875 2000 50  0000 L CNN
F 2 "Capacitors_SMD:C_0603_HandSoldering" H 1888 1950 30  0001 C CNN
F 3 "" H 1850 2100 60  0000 C CNN
	1    1850 2100
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR3
U 1 1 557A10D2
P 1850 2850
F 0 "#PWR3" H 1850 2600 50  0001 C CNN
F 1 "GND" H 1850 2700 50  0000 C CNN
F 2 "" H 1850 2850 60  0000 C CNN
F 3 "" H 1850 2850 60  0000 C CNN
	1    1850 2850
	1    0    0    -1  
$EndComp
$Comp
L +3.3V #PWR28
U 1 1 5589CC6D
P 10000 4850
F 0 "#PWR28" H 10000 4700 50  0001 C CNN
F 1 "+3.3V" H 10000 4990 50  0000 C CNN
F 2 "" H 10000 4850 60  0000 C CNN
F 3 "" H 10000 4850 60  0000 C CNN
	1    10000 4850
	1    0    0    -1  
$EndComp
$Comp
L AMS1117 U1
U 1 1 5589B92D
P 4550 1900
F 0 "U1" H 4750 1650 60  0000 C CNN
F 1 "AMS1117" H 4550 2000 60  0000 C CNN
F 2 "kicad-footprints:SOT-223" H 4550 1900 60  0001 C CNN
F 3 "" H 4550 1900 60  0000 C CNN
	1    4550 1900
	1    0    0    -1  
$EndComp
$Comp
L R R3
U 1 1 5589D563
P 2950 2200
F 0 "R3" V 2850 2100 50  0000 C CNN
F 1 "240R" V 2950 2200 50  0000 C CNN
F 2 "Resistors_SMD:R_0603_HandSoldering" V 2880 2200 30  0001 C CNN
F 3 "" H 2950 2200 30  0000 C CNN
	1    2950 2200
	-1   0    0    1   
$EndComp
$Comp
L R R4
U 1 1 5589D95B
P 2950 2600
F 0 "R4" V 2850 2500 50  0000 C CNN
F 1 "1k5" V 2950 2600 50  0000 C CNN
F 2 "Resistors_SMD:R_0603_HandSoldering" V 2880 2600 30  0001 C CNN
F 3 "" H 2950 2600 30  0000 C CNN
	1    2950 2600
	-1   0    0    1   
$EndComp
Text Notes 1850 1650 0    60   ~ 0
Vout = 1.25*(1+R2/R1)\nVout = 1.25*(1+1500/240) \nVout = 9v
$Comp
L C C4
U 1 1 5589E3B2
P 3350 2400
F 0 "C4" H 3375 2500 50  0000 L CNN
F 1 "10uF" H 3375 2300 50  0000 L CNN
F 2 "Capacitors_SMD:C_0603_HandSoldering" H 3388 2250 30  0001 C CNN
F 3 "" H 3350 2400 60  0000 C CNN
	1    3350 2400
	1    0    0    -1  
$EndComp
$Comp
L C C5
U 1 1 5589E5F6
P 3600 2400
F 0 "C5" H 3625 2500 50  0000 L CNN
F 1 "10uF" H 3625 2300 50  0000 L CNN
F 2 "Capacitors_SMD:C_0603_HandSoldering" H 3638 2250 30  0001 C CNN
F 3 "" H 3600 2400 60  0000 C CNN
	1    3600 2400
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR9
U 1 1 5589EE42
P 3600 2850
F 0 "#PWR9" H 3600 2600 50  0001 C CNN
F 1 "GND" H 3600 2700 50  0000 C CNN
F 2 "" H 3600 2850 60  0000 C CNN
F 3 "" H 3600 2850 60  0000 C CNN
	1    3600 2850
	1    0    0    -1  
$EndComp
$Comp
L C C6
U 1 1 5589F4E8
P 3850 2400
F 0 "C6" H 3875 2500 50  0000 L CNN
F 1 "10uF" H 3875 2300 50  0000 L CNN
F 2 "Capacitors_SMD:C_0603_HandSoldering" H 3888 2250 30  0001 C CNN
F 3 "" H 3850 2400 60  0000 C CNN
	1    3850 2400
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR10
U 1 1 5589F829
P 3850 2850
F 0 "#PWR10" H 3850 2600 50  0001 C CNN
F 1 "GND" H 3850 2700 50  0000 C CNN
F 2 "" H 3850 2850 60  0000 C CNN
F 3 "" H 3850 2850 60  0000 C CNN
	1    3850 2850
	1    0    0    -1  
$EndComp
Text Label 2950 1900 0    60   ~ 0
9v
$Comp
L CONN_01X04 P4
U 1 1 558A0E36
P 1450 4200
F 0 "P4" H 1450 4450 50  0000 C CNN
F 1 "CONN_01X04" V 1550 4200 50  0000 C CNN
F 2 "kicad-footprints:Pin_Header_Angled_SMD_1x04" H 1450 4200 60  0001 C CNN
F 3 "" H 1450 4200 60  0000 C CNN
	1    1450 4200
	-1   0    0    1   
$EndComp
$Comp
L C C9
U 1 1 558A22D9
P 7150 3250
F 0 "C9" H 7175 3350 50  0000 L CNN
F 1 "10uF" H 7175 3150 50  0000 L CNN
F 2 "Capacitors_SMD:C_0603_HandSoldering" H 7188 3100 30  0001 C CNN
F 3 "" H 7150 3250 60  0000 C CNN
	1    7150 3250
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR21
U 1 1 558A276C
P 7150 3450
F 0 "#PWR21" H 7150 3200 50  0001 C CNN
F 1 "GND" H 7150 3300 50  0000 C CNN
F 2 "" H 7150 3450 60  0000 C CNN
F 3 "" H 7150 3450 60  0000 C CNN
	1    7150 3450
	1    0    0    -1  
$EndComp
Text Label 6100 1950 0    60   ~ 0
SW
Text Label 8350 5000 0    60   ~ 0
SW
$Comp
L LM317L U2
U 1 1 5589CC09
P 2400 1950
F 0 "U2" H 2200 2150 40  0000 C CNN
F 1 "LM317L" H 2400 2150 40  0000 L CNN
F 2 "Housings_TO-92:TO-92_Inline_Narrow_Oval" H 2400 2050 30  0001 C CIN
F 3 "" H 2400 1950 60  0000 C CNN
	1    2400 1950
	1    0    0    -1  
$EndComp
Text Label 1600 1900 0    60   ~ 0
Vin_rec
Text Label 2450 2400 0    60   ~ 0
Vadj
Text Label 1000 1900 0    60   ~ 0
Vin1
Text Label 1000 2000 0    60   ~ 0
Vin2
Text Label 9600 2650 0    60   ~ 0
SERVO_SIG
Wire Wire Line
	1100 2850 1100 2400
Wire Wire Line
	7150 3400 7150 3450
Connection ~ 6800 3050
Wire Wire Line
	7150 3050 6800 3050
Wire Wire Line
	7150 3100 7150 3050
Wire Wire Line
	3850 2550 3850 2850
Connection ~ 3850 1900
Wire Wire Line
	3850 2250 3850 1900
Wire Wire Line
	3600 2850 3600 2550
Wire Wire Line
	3350 2550 3350 2850
Connection ~ 3600 1900
Wire Wire Line
	3600 1900 3600 2250
Connection ~ 3350 1900
Wire Wire Line
	3350 1900 3350 2250
Connection ~ 2950 1900
Wire Wire Line
	2950 2750 2950 2850
Wire Wire Line
	2950 1900 2950 2050
Wire Wire Line
	2800 1900 4200 1900
Connection ~ 2950 2400
Wire Wire Line
	2950 2350 2950 2450
Wire Wire Line
	2400 2400 2950 2400
Wire Wire Line
	2400 2200 2400 2400
Wire Wire Line
	4550 2200 4550 2300
Connection ~ 5000 1900
Wire Wire Line
	4900 1900 5050 1900
Wire Wire Line
	5000 2000 5000 1900
Wire Wire Line
	4900 2000 5000 2000
Wire Wire Line
	1850 2250 1850 2850
Connection ~ 1850 1900
Wire Wire Line
	1850 1950 1850 1900
Wire Wire Line
	1550 1900 2000 1900
Wire Wire Line
	1250 2000 950  2000
Wire Wire Line
	950  1900 1250 1900
Connection ~ 1150 1900
Wire Wire Line
	1150 2050 1150 1900
Connection ~ 1050 2000
Wire Wire Line
	1050 2050 1050 2000
Connection ~ 1100 2400
Wire Wire Line
	1150 2400 1150 2350
Wire Wire Line
	1050 2400 1150 2400
Wire Wire Line
	1050 2350 1050 2400
Connection ~ 1650 1900
Wire Wire Line
	1650 2000 1650 1900
Wire Wire Line
	1550 2000 1650 2000
Connection ~ 9950 3250
Wire Wire Line
	9700 3250 9950 3250
Wire Wire Line
	9700 3200 9700 3250
Connection ~ 9700 2850
Wire Wire Line
	9550 2850 9700 2850
Wire Wire Line
	9550 3100 9550 2850
Connection ~ 9700 2750
Wire Wire Line
	9700 2900 9700 2750
Wire Wire Line
	9050 3100 9550 3100
Wire Wire Line
	10050 2750 9550 2750
Wire Wire Line
	8650 2750 9250 2750
Wire Wire Line
	8650 2600 8650 2750
Wire Wire Line
	2450 4250 2450 4050
Wire Wire Line
	10000 4900 10550 4900
Wire Wire Line
	10000 4850 10000 4900
Wire Wire Line
	10550 4800 10200 4800
Wire Wire Line
	10450 5000 10450 5050
Wire Wire Line
	10550 5000 10450 5000
Wire Wire Line
	9250 2650 8900 2650
Wire Wire Line
	1750 6850 1750 6900
Wire Wire Line
	1350 6450 900  6450
Wire Wire Line
	1350 6350 900  6350
Connection ~ 2250 5950
Wire Wire Line
	2550 5950 2550 6050
Wire Wire Line
	2550 6450 2550 6350
Wire Wire Line
	2150 6450 2550 6450
Connection ~ 1750 5950
Wire Wire Line
	1750 5950 2550 5950
Wire Wire Line
	1750 5900 1750 6000
Wire Wire Line
	2250 5950 2250 6350
Wire Wire Line
	2250 6350 2150 6350
Wire Wire Line
	5350 4300 5450 4300
Wire Wire Line
	5450 4100 5350 4100
Wire Wire Line
	9000 1500 8700 1500
Wire Wire Line
	9000 1400 8700 1400
Wire Wire Line
	9000 1300 8700 1300
Wire Wire Line
	9600 1300 9600 1250
Wire Wire Line
	9500 1300 9600 1300
Wire Wire Line
	9600 1500 9600 1550
Wire Wire Line
	9500 1500 9600 1500
Wire Wire Line
	9500 1400 9950 1400
Wire Wire Line
	4800 3800 5450 3800
Wire Wire Line
	6800 3000 6800 3500
Wire Wire Line
	6800 5600 6800 5700
Connection ~ 9250 4300
Wire Wire Line
	9250 4500 9200 4500
Wire Wire Line
	4050 3800 4050 4000
Wire Wire Line
	4900 3400 4900 3450
Wire Wire Line
	8150 5300 8450 5300
Wire Wire Line
	8150 5200 8450 5200
Wire Wire Line
	8150 5100 8450 5100
Wire Wire Line
	8150 5000 8450 5000
Wire Wire Line
	8150 4900 8450 4900
Wire Wire Line
	8150 4800 8450 4800
Wire Wire Line
	8150 4700 8450 4700
Wire Wire Line
	8150 4200 8450 4200
Wire Wire Line
	8150 4400 8450 4400
Wire Wire Line
	8150 3900 8450 3900
Wire Wire Line
	8150 3800 8450 3800
Wire Wire Line
	8150 4100 8450 4100
Wire Wire Line
	8150 4000 8450 4000
Wire Wire Line
	10400 3850 10400 3900
Wire Wire Line
	10600 3850 10400 3850
Wire Wire Line
	10400 3650 10600 3650
Wire Wire Line
	10600 3750 10400 3750
Wire Wire Line
	10050 2650 9550 2650
Wire Wire Line
	9950 2850 9950 3300
Wire Wire Line
	10050 2850 9950 2850
Wire Wire Line
	9250 4150 9250 4500
Wire Wire Line
	9200 4300 9250 4300
Wire Wire Line
	8900 4300 8150 4300
Wire Wire Line
	8150 4500 8900 4500
Wire Wire Line
	6650 1500 6650 1550
Connection ~ 6650 1950
Wire Wire Line
	6650 1850 6650 2100
Connection ~ 7500 2100
Wire Wire Line
	7500 1950 7400 1950
Wire Wire Line
	7500 1950 7500 2200
Wire Wire Line
	7250 2100 7500 2100
Wire Wire Line
	6650 2100 6950 2100
Wire Wire Line
	6100 1950 6800 1950
Connection ~ 5050 1900
Wire Wire Line
	5500 1650 5500 1700
Wire Wire Line
	5500 2000 5500 2050
Wire Wire Line
	5500 2250 5500 2300
Wire Wire Line
	4900 3750 4900 3950
Wire Wire Line
	4900 3950 4650 3950
Connection ~ 4050 3950
Wire Wire Line
	4350 3950 4050 3950
Connection ~ 4900 3800
Wire Wire Line
	4200 3800 4050 3800
Wire Wire Line
	1650 4050 1950 4050
Wire Wire Line
	1650 4150 1950 4150
Wire Wire Line
	1650 4250 2450 4250
Wire Wire Line
	1950 4350 1950 4450
Wire Wire Line
	1650 4350 1950 4350
Wire Wire Line
	5050 1650 5050 1950
Wire Wire Line
	5050 2250 5050 2300
$Comp
L 03mmTo05mm P6
U 1 1 55945A3F
P 1750 5550
F 0 "P6" V 1600 5400 60  0000 C CNN
F 1 "03mmTo05mm" V 2000 5300 60  0000 C CNN
F 2 "kicad-footprints:03mmTo05mm" H 1750 5550 60  0001 C CNN
F 3 "" H 1750 5550 60  0000 C CNN
	1    1750 5550
	0    -1   -1   0   
$EndComp
Wire Wire Line
	1650 5100 1650 5200
Text Label 1800 5950 0    60   ~ 0
03mm3.3V
$Comp
L 03mmTo05mm P7
U 1 1 559475F7
P 1750 7250
F 0 "P7" V 1600 7100 60  0000 C CNN
F 1 "03mmTo05mm" V 2000 7000 60  0000 C CNN
F 2 "kicad-footprints:03mmTo05mm" H 1750 7250 60  0001 C CNN
F 3 "" H 1750 7250 60  0000 C CNN
	1    1750 7250
	0    1    1    0   
$EndComp
Wire Wire Line
	1850 7600 1850 7650
$EndSCHEMATC
