#ifndef INCFILE1_H_
#define INCFILE1_H_

#ifndef uchar
#define uchar   unsigned char
#endif
#ifndef schar
#define schar   signed char
#endif

#include <avr/sleep.h>

#define USECPERTICK 27  //30  // microseconds per clock interrupt tick

// pulse parameters -- nominal use
#define STARTMINN      200
#define STARTMAXN      12000
#define SPACEMINN      100
#define SPACEMAXN      40000
#define BITMARKMINN    100
#define BITMARKMAXN    12000
#define BITSPACEMINN   100
#define BITSPACEMAXN   89900
//#define RPSPACEMINN    40000
//#define RPSPACEMAXN    70000
#define TOTALWAITN      90000



#define BUFFERVALUE    (irparams.bufcounter%2)

#define anyEdgeInt1 MCUCR |= (1 << ISC10)
#define falingEdgeInt1 MCUCR |=  (1 << ISC11) //(1 << ISC11) | (1 << ISC10) 
#define risingEdgeInt1 MCUCR |= (1 << ISC11) | (1 << ISC10) //(1 << ISC11)




#define READY       1
#define GO          2
#define IDLE        3
#define STOPH       4
#define STOPL       5
#define BITMARK     6
#define BITSPACE    7
#define MARKS       8
#define SPACES      9
#define RELEASE     0




#define WAITTIME  100000    // 2sec

#define MAXBUF          128
#define STARTMIN        (unsigned long int)(STARTMINN/USECPERTICK)
#define STARTMAX        (unsigned long int)(STARTMAXN/USECPERTICK)
#define SPACEMIN        (unsigned long int)(SPACEMINN/USECPERTICK)
#define SPACEMAX        (unsigned long int)(SPACEMAXN/USECPERTICK)
#define BITMARKMIN      (unsigned long int)(BITMARKMINN/USECPERTICK)
#define BITMARKMAX      (unsigned long int)(BITMARKMAXN/USECPERTICK)
#define BITSPACEMIN     (unsigned long int)(BITSPACEMINN/USECPERTICK)
#define BITSPACEMAX     (unsigned long int)(BITSPACEMAXN/USECPERTICK)
//#define RPSPACEMIN      (unsigned long int)(RPSPACEMINN/USECPERTICK)
//#define RPSPACEMAX      (unsigned long int)(RPSPACEMAXN/USECPERTICK)
#define TOTALWAIT       (unsigned long int)(TOTALWAITN/USECPERTICK)

#define NBITS           4
#define NBITSMAX        100


#define MARK    0
#define SPACE   1

unsigned int EEMEM EschIrcode[2][MAXBUF];

// state machine variables irparams
volatile struct {
	unsigned short int rcvstate;          // IR receiver state
	unsigned short int bitcounter;        // bit counter
	unsigned short int bufcounter;
	unsigned short int lcounter;
	unsigned long int sndcounter;
	volatile unsigned long int rcvcounter;
	unsigned short int rcvok;
	unsigned short int sndok;
	unsigned short int isrec;
	unsigned short int issnd;
	unsigned short int rintstate;
	unsigned int  ircode[MAXBUF];
	unsigned short int mode;
	unsigned short int repeat;
	unsigned short int inirec;
	unsigned short int rel;
	
	unsigned int schIrcode[1][MAXBUF];
	
} irparams;


int ir_rec();
int ir_send();



//checked
int ir_send()
{
	
	
	if(irparams.issnd){
		
		//USART DISABLE
		UCSRB = ((0<<TXEN)|(0<<RXEN) | (0<<RXCIE));
		
		OCR1A = 40;
		OCR1B = 20;
		
		TCCR1A = (1 << COM1B1) | (1 << WGM11 ) | (1 << WGM10);
		TCCR1B |= (1 << WGM13 ) | (1 << WGM12) | (1 << CS11);
		
		
		TIMSK |= (1 << OCIE1A);
		
		
		irparams.sndok = 0;
		irparams.issnd = 0;
		irparams.sndcounter = 0;
		irparams.bufcounter = 0;

	}
	
	switch (BUFFERVALUE) {
		case MARK:
		if (irparams.ircode[irparams.bufcounter] > STARTMAXN){
			irparams.bufcounter = 0;
			irparams.sndcounter = 0;
			irparams.repeat++;
			TCCR1A |= (1 << COM1B1);
		}
		if(irparams.sndcounter>=irparams.ircode[irparams.bufcounter]){
			irparams.bufcounter++;
			TCCR1A &=  ~(1 << COM1B1);       // no OC1A output change
			irparams.sndcounter = 0;
		}
		
		if( irparams.bufcounter > irparams.lcounter){
			
			irparams.bufcounter = 0;
			irparams.sndcounter = 0;
			irparams.repeat++;
			TCCR1A |= (1 << COM1B1);
			
		}
		
		break;
		case SPACE:
		
		if (irparams.ircode[irparams.bufcounter] > STARTMAXN){
			irparams.bufcounter = 0;
			irparams.sndcounter = 0;
			irparams.repeat++;
			TCCR1A |= (1 << COM1B1);
		}
		
		if(irparams.sndcounter>=irparams.ircode[irparams.bufcounter]){
			irparams.bufcounter++;
			TCCR1A |= (1 << COM1B1);
			irparams.sndcounter = 0;
		}
		
		if( irparams.bufcounter > irparams.lcounter){
			
			irparams.bufcounter = 0;
			irparams.sndcounter = 0;
			irparams.repeat++;
			TCCR1A |= (1 << COM1B1);
			
		}
		
		break;
		
	};
	
	if(irparams.repeat > 0) {
		TCCR1A = 0;
		TCCR1B = 0;
		TIMSK &= ~(1 << OCIE1A);
		irparams.sndok = 1;
		irparams.issnd = 1;
		irparams.lcounter = 0;
		irparams.repeat = 0;
		//USART ENABLE
		UCSRB = ((1<<TXEN)|(1<<RXEN) | (1<<RXCIE));
		
	}
	return irparams.issnd;

}




int ir_rec(){                  // source = 0 from remote, 1 from device
if(irparams.isrec){
	irparams.rintstate = READY;
	irparams.isrec = 0;
	irparams.inirec = 0;
	irparams.lcounter = 0;
	irparams.rcvstate = IDLE;
	irparams.rcvok = 0;
	irparams.bitcounter = 0;
	irparams.sndcounter = 0;
	irparams.rel = 0;
	
	cli();
	//TCCR1A = 0;	//(1 << WGM11 ) | (1 << WGM10);
	TCCR1A = (1 << WGM11 ) | (1 << WGM10);
	//TCCR1B |= (1 << CS11); //(1 << WGM13 ) | (1 << WGM12) | (1 << CS11);
	TCCR1B |= (1 << WGM13 ) | (1 << WGM12) | (1 << CS11);
	OCR1A = 40;
	GICR |= 1 << INT1;
	
	anyEdgeInt1;
	
	TIMSK |= (1 << OCIE1A);

	sei();
	
	// enable global interrupts
	
	
}



if(irparams.rintstate == GO){
	
	switch (irparams.rcvstate) {
		case IDLE:
		
		irparams.lcounter = 0;
		irparams.bufcounter = 0;
		irparams.rcvcounter = 0;
		irparams.bitcounter = 0;
		irparams.rcvstate = STOPH;
		irparams.inirec = 0;
		irparams.rintstate = READY;
		
		break;
		case STOPH:
		if( irparams.rcvcounter > (STARTMIN) && irparams.rcvcounter < (STARTMAX)){
			
			irparams.ircode[irparams.bufcounter] = irparams.rcvcounter;

			irparams.bufcounter++;
			irparams.lcounter++;
			irparams.rcvcounter = 0;
			irparams.rcvstate = STOPL;
			irparams.rintstate = READY;
			
		}
		else{
			irparams.rcvstate = RELEASE;
			irparams.rintstate = READY;
			irparams.rcvok = 0;
			
		}
		break;
		case STOPL:
			if(irparams.rcvcounter > (SPACEMIN) && irparams.rcvcounter < (SPACEMAX)){
				
				irparams.ircode[irparams.bufcounter] = irparams.rcvcounter;
				irparams.bufcounter++;
				irparams.lcounter++;
				irparams.rcvcounter = 0;
				irparams.rcvstate = BITMARK;
				irparams.rintstate = READY;
			}

			else{
				irparams.rcvstate = RELEASE;
				irparams.rintstate = READY;
				irparams.rcvok = 0;
				
			}
			break;
		case BITMARK:
			if(irparams.rcvcounter > (BITMARKMIN) && irparams.rcvcounter < (BITMARKMAX)){
					
				irparams.ircode[irparams.bufcounter] = irparams.rcvcounter;
				irparams.bufcounter++;
				irparams.lcounter++;
				irparams.rcvcounter = 0;
				irparams.rcvstate = BITSPACE;
				irparams.rintstate = READY;
			}
			else{
				irparams.rcvstate = RELEASE;
				irparams.rintstate = READY;
				irparams.rcvok = 0;	
			}
			break;
		case BITSPACE:
				if(irparams.rcvcounter > (BITSPACEMIN) && irparams.rcvcounter < (BITSPACEMAX)){
					irparams.ircode[irparams.bufcounter] = irparams.rcvcounter;
					irparams.bufcounter++;
					irparams.lcounter++;
					irparams.rcvcounter = 0;
					irparams.bitcounter++;
					if(irparams.bitcounter < NBITSMAX){
						irparams.rcvstate = BITMARK;
						irparams.rintstate = READY;
					} else {
						TCCR1A = 0;
						TCCR1B = 0;
						TIMSK &= ~(1 << OCIE1A); //~(1 << OCIE1A);  //TIMSK=0x11;
						irparams.rcvcounter = 0;
						irparams.bufcounter = 0;
						irparams.sndcounter = 0;
						irparams.isrec = 1;
						irparams.lcounter++;
						irparams.rcvok = 1;
						irparams.inirec = 0;
					}
						
					} 
					else {
						irparams.rcvstate = RELEASE;
						irparams.rintstate = READY;
						irparams.rcvok = 0;
					}
					break;
				case RELEASE:
					for(int cn=0;cn < MAXBUF; cn++){
						irparams.ircode[cn] = 0;
					}
					irparams.lcounter = 4;	// tell host no data
					irparams.rel = 1;
					irparams.inirec = 0;
					TCCR1A = 0;
					TCCR1B = 0;
					TIMSK &= ~(1 << OCIE1A); //~(1 << OCIE1A);  //TIMSK=0x11;
					irparams.rcvok = 0;
					irparams.rcvcounter = 0;
					irparams.bufcounter = 0;
					irparams.sndcounter = 0;
					irparams.isrec = 1;
					break;
				};
			}
			
			if( (irparams.rcvcounter > TOTALWAIT) && (irparams.bitcounter> NBITS) ) {
				
				TCCR1A = 0;
				TCCR1B = 0;
				TIMSK &= ~(1 << OCIE1A); //~(1 << OCIE1A);  //TIMSK=0x11;

				irparams.rcvcounter = 0;
				irparams.bufcounter = 0;
				irparams.sndcounter = 0;
				irparams.isrec = 1;
				irparams.rcvok = 1;
				irparams.inirec = 0;
				
				
			}
			
				
			if(irparams.sndcounter > 500000)
			{
				
				for(int cn=0;cn < MAXBUF; cn++){
					irparams.ircode[cn] = 0;
				}
				irparams.inirec = 0;
				TCCR1A = 0;
				TCCR1B = 0;
				TIMSK &= ~(1 << OCIE1A); //~(1 << OCIE1A);  //TIMSK=0x11;
				irparams.rcvok = 0;
				irparams.rcvcounter = 0;
				irparams.bufcounter = 0;
				irparams.sndcounter = 0;
				irparams.isrec = 1;
			}
			
		return irparams.isrec;

}

#endif /* INCFILE1_H_ */