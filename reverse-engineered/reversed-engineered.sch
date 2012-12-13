EESchema Schematic File Version 2  date Thu 13 Dec 2012 01:05:19 PM CET
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
LIBS:special
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
LIBS:reversed-engineered-cache
EELAYER 25  0
EELAYER END
$Descr A4 11700 8267
encoding utf-8
Sheet 1 6
Title ""
Date "13 dec 2012"
Rev ""
Comp ""
Comment1 ""
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
Text Label 9500 2200 0    60   ~ 0
Protective Earth
Wire Wire Line
	9250 2200 10250 2200
Wire Wire Line
	9250 1900 10250 1900
Wire Notes Line
	5700 4750 5650 4700
Wire Notes Line
	5700 4750 5700 4650
Wire Notes Line
	5700 4650 5650 4700
Wire Notes Line
	5650 4700 5800 4700
Wire Bus Line
	8150 3950 8150 4100
Wire Notes Line
	3250 5250 3250 5600
Wire Notes Line
	3250 5250 3650 5250
Wire Notes Line
	3650 5250 3650 5600
Wire Notes Line
	3650 5600 3250 5600
Wire Bus Line
	9250 3900 9700 3900
Wire Notes Line
	8450 5250 8800 5250
Wire Notes Line
	8450 5250 8450 5600
Wire Notes Line
	8450 5600 8800 5600
Wire Notes Line
	8800 5600 8800 5250
Wire Notes Line
	9800 5550 9650 5550
Wire Notes Line
	9800 5550 9800 5700
Wire Notes Line
	9800 5700 9650 5700
Wire Bus Line
	9700 3900 9700 5400
Wire Bus Line
	2150 6700 2150 6950
Wire Bus Line
	2150 6950 9700 6950
Wire Bus Line
	9700 6950 9700 6650
Wire Bus Line
	6350 1500 2850 1500
Wire Bus Line
	2850 1500 2850 4100
Wire Notes Line
	6700 3800 6700 3550
Wire Notes Line
	6700 3800 6950 3800
Wire Notes Line
	6950 3800 6950 3550
Wire Notes Line
	6950 3550 6700 3550
Wire Notes Line
	9650 5700 9650 5550
Wire Bus Line
	8850 3950 8850 4100
Wire Bus Line
	6350 1700 3200 1700
Wire Bus Line
	3200 1700 3200 4100
Wire Notes Line
	6200 4700 6350 4700
Wire Notes Line
	6300 4650 6350 4700
Wire Notes Line
	6300 4650 6300 4750
Wire Notes Line
	6300 4750 6350 4700
Wire Wire Line
	9250 1750 10250 1750
Wire Wire Line
	9250 2050 10250 2050
Text Label 9900 1750 0    60   ~ 0
L2 230v
Text Label 9900 1900 0    60   ~ 0
L1 230v
Text Label 9900 2050 0    60   ~ 0
Neutral
Text Notes 5800 4750 0    60   ~ 0
Identical
Text Notes 7800 4100 0    60   ~ 0
2 wires for power
Text Notes 4250 1700 0    60   ~ 0
2 wires for power
Text Notes 9500 5850 0    60   ~ 0
NEC D78F0034AGK
Text Notes 3150 800  0    200  ~ 40
Electrolux EHD7660P block schematics
$Sheet
S 2750 4100 2900 2600
U 50C9B907
F0 "Left driver" 60
F1 "driver.sch" 60
$EndSheet
Text Notes 3100 5750 0    60   ~ 0
MC56F8322VFAE
Text Notes 8850 4100 0    60   ~ 0
4 wires control
Text Notes 8250 5750 0    60   ~ 0
MC56F8322VFAE
Text Notes 9450 3900 0    60   ~ 0
3 wires
Text Notes 4250 1500 0    60   ~ 0
4 wires control
Text Notes 6550 3450 0    60   ~ 0
ATMEGA32L-8AU
Text Notes 5300 6950 0    60   ~ 0
10 wires
$Sheet
S 1950 5500 500  1200
U 50C8D45D
F0 "Left display" 60
F1 "display.sch" 60
$EndSheet
$Sheet
S 9450 5400 500  1250
U 50C8D438
F0 "controller" 60
F1 "controller.sch" 60
$EndSheet
$Sheet
S 6350 4100 2900 2600
U 50C8D416
F0 "Righ driver" 60
F1 "driver.sch" 60
$EndSheet
$Sheet
S 6350 1250 2900 2700
U 50C8D398
F0 "Power card" 60
F1 "power-card.sch" 60
$EndSheet
$EndSCHEMATC
