#!/bin/bash

avr-gcc -g -Os -mmcu=attiny2313 -c main.c 
avr-gcc -g -mmcu=attiny2313 -o main.elf main.o
avr-objcopy -j .text -j .data -O ihex main.elf main.hex


avrdude -c buspirate -P /dev/ttyUSB0 -p attiny2313 -v -U flash:w:main.hex
avrdude -c buspirate -P /dev/ttyUSB0 -p attiny2313 -v -U flash:v:main.hex
