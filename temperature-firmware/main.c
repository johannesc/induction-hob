#include<avr/io.h>

#define F_CPU 8000000 // 8 MHz

#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/sleep.h>
#include <string.h>
#include <math.h>
#include <util/delay.h>
#include "USI_TWI_Master.h"

#define MESSAGEBUF_SIZE       4
#define TWI_CMD_MASTER_READ  0x0

#define TMP102_TEMPERATURE_REGISTER  0x00
#define TMP102_CONFIGURATION_REGISTER 0x01
#define TMP102_TLOW_REGISTER 0x02
#define TMP102_THIGH_REGISTER 0x03

#define TMP102_CONFIG_BITMASK_EM (1<<4)

#define SW1_BIT (1 << PORTD3)
#define SW1_BIT_PORT (PORTD)
#define SW1_BIT_PIN (PIND)
#define SW1_PRESSED (!(SW1_BIT_PIN & SW1_BIT))

#define LED_D6_BIT (1 << PORTB1)
#define LED_D6_PORT (PORTB)

#define LED_D6_ON (LED_D6_PORT |= LED_D6_BIT)
#define LED_D6_OFF (LED_D6_PORT &= ~LED_D6_BIT)

#define DELAY(time_ms) { long loop=time_ms * 500L; while(loop--);}

#define BAUD_RATE 2400
// From datasheet Table 14-1, page 123
#define UBBR_VALUE ((F_CPU / (BAUD_RATE * 16L)) - 1)

#define I2C_ADDRESS 0b1001000

void setupUART(void)
{
  UBRRL = UBBR_VALUE;
  UCSRB |= _BV(TXEN); // Tx Enable
  sei(); // Turn on interrupts
}

// Transmit USART data
void transmitData(const unsigned char data)
{
  while ( !( UCSRA & _BV(UDRE)) ); // Wait for empty transmit buffer
  UDR = data;
}

void transmitString(const char * pString)
{
	int index = 0;
	while(pString[index])
	{
		transmitData(pString[index++]);
	}
}

void transmitDigit(unsigned char val)
{
    val &= 0xF;
    val += (val < 10) ? '0' : 'A' - 10;
    transmitData(val);
}

void transmitHex(unsigned char val)
{
    transmitDigit(val >> 4);
    transmitDigit(val);
}

const char * testText = "\r\nThis is a test\r\n";
int main(void)
{
    DDRB |= _BV(PORTB1);
    PORTB |= _BV(PORTB1);
    setupUART();

    unsigned char messageBuf[MESSAGEBUF_SIZE];
    unsigned char res;

    USI_TWI_Master_Initialise();

    int bol = 0;
    transmitString("Booted\r\n");

    while(1)
    {
        messageBuf[0] = (I2C_ADDRESS<<TWI_ADR_BITS) | (FALSE<<TWI_READ_BIT); // The first byte must always consit of General Call code or the TWI slave address.
        messageBuf[1] = TWI_CMD_MASTER_READ;             // The first byte is used for commands.
        res = USI_TWI_Start_Transceiver_With_Data( messageBuf, 2 );
        if (!res)
        {
            transmitString("Read 1st fail\r\n");
        }

        messageBuf[0] = (I2C_ADDRESS<<TWI_ADR_BITS) | (TRUE<<TWI_READ_BIT); // The first byte must always consit of General Call code or the TWI slave address.
        do
        {
            res = USI_TWI_Start_Transceiver_With_Data( messageBuf, 3 );
            if (!res)
            {
                transmitString("fail\r\n");
            }
        }
        while (USI_TWI_Get_State_Info() == USI_TWI_NO_ACK_ON_ADDRESS);

        unsigned char err = USI_TWI_Get_State_Info();

        //TODO set 13 bit mode?
        transmitString("Read done");
        transmitHex(messageBuf[1]);
        transmitHex(messageBuf[2]);
        transmitString("=");
        //Page 8, 15
        unsigned int temp = ((unsigned int)(messageBuf[1]<<8) | (unsigned int)(messageBuf[2])) >> 4;
        transmitHex(temp >> 8);
        transmitHex(temp & 0XFF);

        transmitString("\r\n");

        bol++;
        if (bol%2)
        {
            LED_D6_ON;
        }
        else
        {
            LED_D6_OFF;
        }
            DELAY(10000);
        }
}

ISR(WDT_OVERFLOW_vect) { }

ISR(USART_UDRE_vect) {}

ISR(USART_RX_vect) {}
