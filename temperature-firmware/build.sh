#!/bin/bash

set -e
avr-gcc -g -Os -mmcu=attiny2313 -c main.c USI_TWI_Master.c
avr-gcc -g -mmcu=attiny2313 -o main.elf main.o USI_TWI_Master.o
avr-objcopy -j .text -j .data -O ihex main.elf main.hex

# Internal 8MHz osc
FUSES="-U lfuse:w:0xde:m -U hfuse:w:0xdf:m -U efuse:w:0xff:m"

# External 8MHz crystal
#FUSES="-U lfuse:w:0xe4:m -U hfuse:w:0xdf:m -U efuse:w:0xff:m "

# Fuses default (H:FF, E:DF, L:E4)
avrdude -c buspirate -P /dev/ttyUSB0 -p attiny2313 -v -U flash:w:main.hex $FUSES
#avrdude -c buspirate -P /dev/ttyUSB0 -p attiny2313 -v -U flash:v:main.hex $FUSES
