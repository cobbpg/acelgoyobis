#ifdef TI83V

#define TI83

#endif

#ifdef TI82C

#define TI82

#define bcall(xxxx) call $8D74 \ .word xxxx

_vputs		.equ	$37D4	; Displays a string on the graph screen

#endif

#ifdef TI82C196

#define TI82

#define bcall(xxxx) call xxxx

_vputs		.equ	$390D	; Displays a string on the graph screen

#endif

#ifdef TI82S

#define TI82

#define bcall(xxxx) call $8D74 \ .word xxxx

_vputs		.equ	$37D4	; Displays a string on the graph screen

#endif

#ifdef TI82

PenColRow	.equ	$8215	; Pen column and row address

VScreen 	.equ	$88B8	; Plotsscreen, 768 bytes
VScreenB	.equ	$8228	; Savesscreen, 768 bytes
Vars		.equ	$8028	; OPs (tightly fitting)

MEMSTART	.equ	VScreenB+762	; These will be 'simulated', sort of
MEMEND		.equ	VScreenB+764
MEMALLOC	.equ	VScreenB+760	; The amount of memory allocated

#ifdef TI82S

INSERT_MEM	.equ	$2258
MEM_FREE	.equ	$22AE
DEL_MEM		.equ	$258D
DEL_TMP		.equ	$3606
CREATE_TMP	.equ	$38D0

 .org ($9445)-39

 .byte $3b,$31,$33,$70,$39,$34,$31,$33	; SNG launcher
 .byte $34,$33,$04,$5c,$4b,$10,$31,$39
 .byte $2b,$33,$34,$3f,$72,$04,$5c,$4b
 .byte $10,$31,$39,$2b,$33,$3f,$e0,$31
 .byte $2b,$31,$2b,$2a,$3f,$d9,$3f

#else

 .org $9101
 .byte $D5,$00,$11	; CrASH header

#endif

#endif

#ifdef TI83

#define bcall(xxxx) call xxxx

_vputs		.equ	$4781	; Displays a string on the graph screen
PenColRow	.equ	$8252	; Pen column and row address

VScreen 	.equ	$8E29	; Plotsscreen, 768 bytes
VScreenB	.equ	$8265	; Savesscreen, 768 bytes
Vars		.equ	$8039	; OPs (tightly fitting)
;VarsB		.equ	$858F	; Statvars beginning

MEMSTART	.equ	$930D
MEMEND		.equ	$9311

#ifdef TI83V

 .org $9329
 .byte "ç9_[V?", 0			; Venus header

#else

 .org $9327
 ret						; ION header

#endif

#endif

#ifdef TI83P

#define bcall(xxxx) rst 28h \ .word xxxx

_vputs		.equ	$4561	; Displays a string on the graph screen
PenColRow	.equ	$86D7	; Pen column and row address

VScreen 	.equ	$9340	; Plotsscreen, 768 bytes
VScreenB	.equ	$86EC	; Savesscreen, 768 bytes
Vars		.equ	$8478	; OPs (tightly fitting)
;VarsB		.equ	$9872	; Appbackupscreen

MEMSTART	.equ	$9824
MEMEND		.equ	$9828

 .org $9D93
 .byte $BB,$6D
 ret				; ION header

#endif

#ifndef TI82

 jr nc,Main

#endif

 .byte "Acelgoyobis",0
