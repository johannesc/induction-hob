#include<avr/io.h>

#define F_CPU 8000000 // 8 MHz

#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/sleep.h>
#include <string.h>
#include <math.h>
#include <util/delay.h>

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

void setupUART(void)
{
  UBRRL = UBBR_VALUE;
  UCSRB |= _BV(TXEN); // Tx Enable
  sei(); // Turn on interrupts
}

// Transmit USART data
void transmitData(unsigned char data)
{
  while ( !( UCSRA & _BV(UDRE)) ); // Wait for empty transmit buffer
  UDR = data;
}

void transmitString(char * pString)
{
	int index;
	while(pString[index])
	{
		transmitData(pString[index++]);
	}
}

const char * testText = "\r\nThis is a test\r\n";
int main(void)
{
  long i;
  DDRB |= _BV(PORTB1);
  PORTB |= _BV(PORTB1);
  setupUART();
  while(1)
  {
    transmitString(testText);
    DELAY(1000);
    if (SW1_PRESSED)
    {
      LED_D6_ON;
      transmitData('A');
	}
	else
	{
      LED_D6_OFF;
      transmitData('B');
	}
  }
}

ISR(WDT_OVERFLOW_vect) { }

ISR(USART_UDRE_vect) {}

ISR(USART_RX_vect) {}
