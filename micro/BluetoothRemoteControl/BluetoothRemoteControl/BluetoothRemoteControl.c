#define F_CPU 12000000L

#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/wdt.h>
#include <avr/sleep.h>
#include <avr/eeprom.h>
#include <util/delay.h>
//#include <avr/pgmspace.h> 

#define ONTIME 50
#define OFFTIME 100
#define WAITLD  1000
#define LOOPTIME 6000

// Define baud rate
#define USART_BAUDRATE 57600 //57600 //9600
#define BAUD_PRESCALE (((F_CPU / (USART_BAUDRATE * 16L))) - 1)


volatile unsigned char value;

uint8_t data[256];
uint8_t cdata = 0;
volatile uint8_t waiting = 1;
volatile uint8_t numIn = 0;

//poweroff
uint16_t tTimer = 0;
uint16_t sMin = 1440;
uint16_t eMin = 0;
void schFun();
void schReady();
void wnm();

uint16_t cMin = 0;
uint16_t trMin[3] = {1500, 1500, 1500};
uint8_t rpMin[3] = {2,2,2};
uint8_t isSch = 0;

void detectState(void);
void USART_SendByte(uint8_t);
void conInit(void);
void pwrDown(void);
void witg(void);
void BT_SETNAME(void);

#include "IR.h"


#define ONE_MIN 2660 

#define IR_GET_LENGTH 0
#define STALL 0
#define PLAY  1

#define RTYPE 0xa1
#define TTYPE 0x21
#define TTYPS 0x12
#define TYPE0 0x00
#define TYPE1 0x01
#define STARTCONNECT 0x02
#define ENDF 0x66

static unsigned int dataReceived; // for USB_DATA_IN

uchar r_State;
uchar m_State;
uchar s_State;


//ISR(TIMER1_OVF_vect){
ISR(TIMER1_COMPA_vect){
	irparams.sndcounter++;
	irparams.rcvcounter++;
}

ISR(TIMER0_OVF_vect){
	
	tTimer++;
	if(tTimer > ONE_MIN){
		waiting = 1;
		eMin++;
		tTimer = 0;
		if (eMin >= sMin)
		{
			 pwrDown();
		}
		
		schFun(); // sche trans
	}
	
}

ISR(INT1_vect){
	irparams.rintstate = GO;
}

ISR(USART_RXC_vect){
		data[cdata] = UDR;
			if(data[cdata] == ENDF){
				if(cdata > 0 && data[cdata-1] == ENDF){
					dataReceived = cdata;
					cdata = 0;
					//poweroff
					eMin = 0;
					tTimer = 0;
					detectState();
				}
				else {
					cdata++;
				}
			} else {
				cdata++;
			}
			if (cdata > 254)
				cdata = 0;
}

void detectState(){
	uchar bmRequestType = data[0];
	uchar bRequest = data[1];	
	int i;
	if(bmRequestType == RTYPE){
		if(bRequest == TYPE0) {
			if (irparams.rcvok == 1) {
				USART_SendByte(TTYPE);
				for (i=0;i<irparams.lcounter;i++) {
					USART_SendByte(((irparams.ircode[i] >> 8)& 0x00ff));
					USART_SendByte(((irparams.ircode[i]) & 0x00ff));
				}
				USART_SendByte(ENDF);
				USART_SendByte(ENDF);
			} else if(irparams.rel == 1){
				USART_SendByte(TTYPE);
				for (i=0;i<irparams.lcounter;i++) {
					USART_SendByte(((irparams.ircode[i] >> 8)& 0x00ff));
					USART_SendByte(((irparams.ircode[i]) & 0x00ff));
				}
				USART_SendByte(ENDF);
				USART_SendByte(ENDF);
				irparams.rel = 0;
			}
			
		}
		else if(bRequest == STARTCONNECT) {
			conInit();
		}
		
	} else if(bmRequestType == TTYPE){ // device should be ready for code
		if(bRequest == TYPE0) {
			
			if (isSch)
			{
				waiting = 0;
				irparams.lcounter = dataReceived/2 - 2;
				schReady();
				isSch = 0;
				dataReceived = 0;
			} else {
				waiting = 0;
				irparams.lcounter = dataReceived/2 - 2;
				for(i=0;i<irparams.lcounter;i++){
					//irparams.ircode[i] = ((irparams.ircode[2*i+2] << 8) & 0xff00) | (irparams.ircode[2*i+3] & 0x00ff);
					irparams.ircode[i] = ((data[2*i+2] << 8) & 0xff00) | (data[2*i+3] & 0x00ff);
				}
				dataReceived = 0;
				s_State = PLAY;
			}
	
		}else if(bRequest == TYPE1){  // wait for remote code from device
			waiting = 0;
			m_State = PLAY;
		}else{
			//error
			
		}
		
	} else if(bmRequestType == TTYPS){
		if(bRequest == TYPE0) {
			uint16_t tmp = 0;
			tTimer = 0;
			eMin = 0;
			tmp = ((data[2] << 8) & 0xff00) | (data[3] & 0x00ff);
			if(tmp == 0){
				TCCR0 = (0 << CS02)  | (0 << CS00);
			}else {
				TCCR0 = (1 << CS02)  | (1 << CS00);
				sMin = tmp;
			}
			//pwrDown();
		}else if (bRequest == TYPE1)
		{
			cMin = ((data[2] << 8) & 0xff00) | (data[3] & 0x00ff);
			trMin[data[6] - 1] = ((data[4] << 8) & 0xff00) | (data[5] & 0x00ff);
			rpMin[data[6] - 1] = data[7];
			isSch = data[6];
		}
	}else {
		
		//error
	}
	
}

void conInit(void){
	
	unsigned long i,j;
	
	USART_SendByte(TTYPE);
	USART_SendByte(STARTCONNECT);
	USART_SendByte(ENDF);
	USART_SendByte(ENDF);
	
	//waiting = 0;
	
	PORTB |= 1;
	for(i=0;i< LOOPTIME;i++)
		for(j=0;j< ONTIME;j++){}
	PORTB &= ~1;
	for(i=0;i< LOOPTIME;i++)
		for(j=0;j< OFFTIME;j++){}
	PORTB |= 1;
	for(i=0;i< LOOPTIME;i++)
		for(j=0;j< ONTIME;j++){}
	PORTB &= ~1;
	for(i=0;i< LOOPTIME;i++)
		for(j=0;j< OFFTIME;j++){}
	PORTB |= 1;
	for(i=0;i< LOOPTIME;i++)
		for(j=0;j< ONTIME;j++){}
	PORTB &= ~1;
	for(i=0;i< LOOPTIME;i++)
		for(j=0;j< OFFTIME;j++){}
	PORTB |= 1;
	for(i=0;i<LOOPTIME;i++)
		for(j=0;j< ONTIME;j++){}
	PORTB &= ~1;
	
}

void USART_Init(void){
   // Set baud rate
   UBRRL = BAUD_PRESCALE;// Load lower 8-bits into the low byte of the UBRR register
   UBRRH = (BAUD_PRESCALE >> 8); 
	 /* Load upper 8-bits into the high byte of the UBRR register
    Default frame format is 8 data bits, no parity, 1 stop bit
  to change use UCSRC, see AVR datasheet*/ 

  // Enable receiver and transmitter and receive complete interrupt 
  UCSRB = ((1<<TXEN)|(1<<RXEN) | (1<<RXCIE));
}

void USART_SendByte(uint8_t u8Data){

	// Wait until last byte has been transmitted
	while((UCSRA &(1<<UDRE)) == 0);

	// Transmit data
	UDR = u8Data;
}

void BT_SETNAME(void){
	
	//char char_array[] = {'A','T','+','B','A','U','D','7','\r','\n',0x00};
	char char_array[] = {'A','T','+','N','A','M','E','S','m','a','r','t','R','e','m','o','t','e','\r','\n',0x00};
	//char char_array[] = {'A','T','+','P','I','N','1','2','3','4','\r','\n',0x00};
	uint8_t i = 0;
	while (char_array[i] != 0x00)
	{
		while((UCSRA &(1<<UDRE)) == 0);
		UDR = char_array[i++];
	}
}

void witg(void){
	unsigned long i,j;
	PORTB |= 1;
	for(i=0;i< LOOPTIME;i++)
	for(j=0;j< ONTIME;j++){}
	PORTB &= ~1;
	i = 0;
	while(i < LOOPTIME){
		i++;
		j = 0;
		if (waiting == 0)
		break;
		while(j < WAITLD){
			j++;
			if (waiting == 0)
			break;
		}
	}
}

// not being used but here for completeness
// Wait until a byte has been received and return received data
uint8_t USART_ReceiveByte(){
	while((UCSRA &(1<<RXC)) == 0);
	return UDR;
}

void pwrDown(){
	cli();
	TCCR0 = (0 << CS02)  | (0 << CS00);
	TCCR1A = (0 << WGM11 ) | (0 << WGM10);
	TCCR1B |= (0 << WGM13 ) | (0 << WGM12) | (0 << CS11);
	GICR |= 0 << INT1;
	TIMSK |= (0 << TOIE0);
	UCSRB = ((0<<TXEN)|(0<<RXEN) | (0<<RXCIE));
	set_sleep_mode(SLEEP_MODE_PWR_DOWN);
	PORTB &= ~0x02;
	sleep_enable();
	sei();
	sleep_cpu();
}

int main(void)
{
    //uchar i;
    irparams.isrec = 1;
    s_State = STALL;
    DDRB = 0x0f; // PB0 as output
    OCR1B = 20;
    OCR1A = 40;
	
	cli();
	//powerdown counter
	TCNT0 = 0x00;
	TCCR0 = (1 << CS02)  | (1 << CS00);
	TIMSK |= (1 << TOIE0);
	PORTB |= 0x02;
	
    USART_Init();
    sei(); // Enable interrupts after re-enumeration
    
	
	
    while(1) {
	    //       wdt_reset(); // keep the watchdog happy
		while(waiting == 1)
		{
			witg();
			//BT_SETNAME();
		}
	    if(m_State == PLAY){
		    PORTB |= 1;
		    while(!ir_rec());
			//USART ENABLE
			UCSRB = ((1<<TXEN)|(1<<RXEN) | (1<<RXCIE));
		    PORTB &= ~1;
		    m_State = STALL;
			if (irparams.lcounter > 0)
			{
				USART_SendByte(TTYPE);
				USART_SendByte(STARTCONNECT);
				USART_SendByte(ENDF);
				USART_SendByte(ENDF);
			}
			waiting = 1;
	    }
	    
	    if (s_State == PLAY){
		    irparams.issnd = 1;
		    while(!ir_send());
		    s_State = STALL;
			if(numIn > 0)
				_delay_ms(300);
			wnm();
		//	waiting = 1;
	    }
	    
    }
    
    return 0;
}

void schFun(){
	int i,j = 0;
	
	uint8_t rep = 0;
	
	if(cMin > 1440)
		cMin = 1;
	if(cMin > 0)
		cMin++;
	
	for (i=0;i<1;i++)
	{
		if (cMin == trMin[i])
		{
			
			//PORTB |= 1;
			rep++;
			j = 0;
			waiting = 0;
			while (irparams.schIrcode[i][j] != (ENDF | ENDF))
			{
				j++;
				irparams.ircode[j] = irparams.schIrcode[i][j];
			}
			irparams.lcounter = j;
			dataReceived = 0;
			s_State = PLAY;
			if (rpMin[i] == 0)
			{
				trMin[i] = 1500;
			}
		}
	}
	for (i=1;i<3;i++)
	{
		if (cMin == trMin[i])
		{
			rep++;
			j = 0;
			waiting = 0;
			while (eeprom_read_word(&EschIrcode[i-1][j]) != (ENDF | ENDF))
			{
				j++;
				irparams.ircode[j] = eeprom_read_word(&EschIrcode[i-1][j]);
			}
			irparams.lcounter = j;
			dataReceived = 0;
			s_State = PLAY;
			if (rpMin[i] == 0)
			{
				trMin[i] = 1500;
			}
			
		}
	}
	numIn = rep;
}


void wnm(){
	int j = 0;
	if (numIn == 2)
	{
		while (eeprom_read_word(&EschIrcode[0][j]) != (ENDF | ENDF))
		{
			j++;
			irparams.ircode[j] = eeprom_read_word(&EschIrcode[0][j]);
		}
		irparams.lcounter = j;
		dataReceived = 0;
		s_State = PLAY;
		if (rpMin[1] == 0)
		{
			trMin[1] = 1500;
		}
		numIn--;
	} else if (numIn == 3)
	{
		while (eeprom_read_word(&EschIrcode[1][j]) != (ENDF | ENDF))
		{
			j++;
			irparams.ircode[j] = eeprom_read_word(&EschIrcode[1][j]);
		}
		irparams.lcounter = j;
		dataReceived = 0;
		s_State = PLAY;
		if (rpMin[2] == 0)
		{
			trMin[2] = 1500;
		}
		numIn--;
	} else {
		numIn = 0;
	}
}


void schReady(){
	
	unsigned int i;
	
	switch (isSch)
	{
		case 1:
			
			for(i=0;i<irparams.lcounter;i++){
				irparams.schIrcode[0][i] = ((data[2*i+2] << 8) & 0xff00) | (data[2*i+3] & 0x00ff);
				
				//USART_SendByte(((irparams.schIrcode[1][i] >> 8)& 0x00ff));
				//USART_SendByte(((irparams.schIrcode[i]) & 0x00ff));
				
			}
			irparams.schIrcode[0][irparams.lcounter] = (ENDF) | (ENDF);
			break;
		case 2:
			for(i=0;i<irparams.lcounter;i++){
				eeprom_write_word(&EschIrcode[0][i], (((data[2*i+2] << 8) & 0xff00) | (data[2*i+3] & 0x00ff)));
			}
			eeprom_write_word(&EschIrcode[0][irparams.lcounter], ((ENDF) | (ENDF)));
		break;
		case 3:
			for(i=0;i<irparams.lcounter;i++){
				eeprom_write_word(&EschIrcode[1][i], (((data[2*i+2] << 8) & 0xff00) | (data[2*i+3] & 0x00ff)));
			}
			eeprom_write_word(&EschIrcode[1][irparams.lcounter], ((ENDF) | (ENDF)));
		break;
		default:
			isSch = 0;
		break;
	}
}