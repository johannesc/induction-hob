#include<avr/io.h>

// TODO use these macros instead
#define SW1_BIT (1 << PORTD3)
#define SW1_BIT_PORT (PORTD)

#define LED_D6_BIT (1 << PORTB1)
#define LED_D6_PORT (PORTB)

int main(void)
{
  DDRB |= 1 << PORTB1;
  PORTB |= 1 << PORTB1;
  while(1)
  {
    if (PIND & (1 << PORTD3))
    {
      PORTB &= ~(1 << PORTB1);
    }
    else
    {
      PORTB |= 1 << PORTB1;
    }
  }
}
