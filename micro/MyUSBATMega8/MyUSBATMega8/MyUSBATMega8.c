#define F_CPU 12000000L

#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/wdt.h>
#include "usbdrv.h"
#include "IR.h"


#include <util/delay.h>

#define USB_LED_OFF 0
#define USB_LED_ON  1
#define USB_DATA_OUT 2
#define USB_DATA_WRITE 3
#define USB_DATA_IN 4

#define IR_GET_LENGTH 0
#define STALL 0
#define PLAY  1


static int dataReceived; // for USB_DATA_IN

void conInit();


const uchar RTYPE = 0xa1;
const uchar TTYPE = 0x21;


uchar r_State;
uchar m_State;
uchar s_State;

//ISR(TIMER1_OVF_vect){
ISR(TIMER1_COMPA_vect){
	irparams.sndcounter++;
	irparams.rcvcounter++;
}

ISR(INT1_vect){
	irparams.rintstate = GO;
}


// this gets called when custom control message is received
USB_PUBLIC uchar usbFunctionSetup(uchar data[8]) {
	usbRequest_t *rq = (void *)data; // cast data to correct type
	
	
	if(rq->bmRequestType == RTYPE){
		
		if(rq->bRequest == 0x00) {
			r_State = IR_GET_LENGTH;
		}
		else {
			r_State = rq->bRequest;
		}
		
		return USB_NO_MSG;
		}	 else if(rq->bmRequestType == TTYPE)	{
		if(rq->bRequest == 0x00) {
			irparams.lcounter = 0;
			dataReceived = 0;
			return 0;
			} else if(rq->bRequest == 0x01){
			m_State = PLAY;
			return 0;
		}
		else if(rq->bRequest == 0x02){
			irparams.lcounter = dataReceived;
			s_State = PLAY;
			return 0;
		}
		else if(rq->bRequest == 0x03){
			
			return USB_NO_MSG; // usbFunctionWrite will be called now
		}
		
	}

	return 0; // should not get here
}


USB_PUBLIC uchar usbFunctionRead(uchar *data, uchar len){
	
	if(r_State == IR_GET_LENGTH){
		data[0] = irparams.lcounter;
		return 1;
		} else {
		data[0] =  (irparams.ircode[r_State - 1] >> 8) & 0x00ff;
		data[1] =  irparams.ircode[r_State - 1] & 0x00ff;
		data[2] =  (irparams.ircode[r_State] >> 8) & 0x00ff;;
		data[3] =  irparams.ircode[r_State ] & 0x00ff;
		data[4] =  (irparams.ircode[r_State + 1] >> 8) & 0x00ff;
		data[5] =  irparams.ircode[r_State + 1] & 0x00ff;
		data[6] =  (irparams.ircode[r_State + 2] >> 8) & 0x00ff;
		data[7] =  irparams.ircode[r_State + 2] & 0x00ff;
		return 8;
	}
	
}


// This gets called when data is sent from PC to the device
USB_PUBLIC uchar usbFunctionWrite(uchar *data, uchar len) {

	uchar i;
	uchar j = 0;
	for(i = 0; i < 8; i += 2){
		irparams.ircode[dataReceived + j++] = ((data[i] << 8) & 0xff00) | (data[i+1] & 0x00ff);
	}
	
	dataReceived = dataReceived + 4;
	
	return 1; // 1 if we received it all, 0 if not
	
}

int main() {
	uchar i;
	irparams.isrec = 1;
	s_State = STALL;
	DDRB = 0x0f; // PB0 as output
	OCR1B = 20;
	OCR1A = 40;
	
	
	//wdt_enable(WDTO_1S); // enable 1s watchdog timer

	usbInit();
	
	usbDeviceDisconnect(); // enforce re-enumeration
	for(i = 0; i<250; i++) { // wait 500 ms
		//wdt_reset(); // keep the watchdog happy
		_delay_ms(2);
	}
	usbDeviceConnect();
	
	sei(); // Enable interrupts after re-enumeration
	conInit();
	
	while(1) {
		//       wdt_reset(); // keep the watchdog happy
		
		usbPoll();
		if(m_State == PLAY){
			PORTB |= 1;
			while(!ir_rec());
			PORTB &= ~1;
			m_State = STALL;
		}
		
		if (s_State == PLAY){
			irparams.issnd = 1;
			while(!ir_send());
			s_State = STALL;
		}
		
	}
	
	return 0;
}


void conInit(void){
	unsigned int i,j;
	
	PORTB |= 1;
	for(i=0;i<1000;i++)
	for(j=0;j<250;j++){}
	PORTB &= ~1;
	for(i=0;i<1000;i++)
	for(j=0;j<100;j++){}
	PORTB |= 1;
	for(i=0;i<1000;i++)
	for(j=0;j<250;j++){}
	PORTB &= ~1;
	for(i=0;i<1000;i++)
	for(j=0;j<100;j++){}
	PORTB |= 1;
	for(i=0;i<1000;i++)
	for(j=0;j<250;j++){}
	PORTB &= ~1;
	for(i=0;i<1000;i++)
	for(j=0;j<100;j++){}
	PORTB |= 1;
	for(i=0;i<1000;i++)
	for(j=0;j<250;j++){}
	PORTB &= ~1;
	
}