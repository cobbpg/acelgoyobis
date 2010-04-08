; Mathematical calculations optimized for high speed
; The tables needed are in miscdata.inc

Mul_B_SinDE:		; HL=B*sin(DE*PI/128)*127 (B is signed, D must be 0)
 ld hl,Sin_Table
 add hl,de
 ld a,(hl)
 ld e,a
 rla
 sbc a,a
 ld d,a
 ld a,b
 cp $80
 push af
 jp c,Mul_Signed_A_DE
 neg
Mul_Signed_A_DE:
 call Mul_A_DE
 pop af
 ret c
 xor a
 sub l
 ld l,a
 ld a,0
 sbc a,h
 ld h,a
 ret

Mul_A_DE:		; HL=A*DE, where A is unsigned and DE is signed, in less than 250 cycles!
 ld hl,0		; A can be greater than 127 as opposed to signed multiplication.
 rra \ jp nc,$+4 \ add hl,de \ sla e \ rl d
 rra \ jp nc,$+4 \ add hl,de \ sla e \ rl d
 rra \ jp nc,$+4 \ add hl,de \ sla e \ rl d
 rra \ jp nc,$+4 \ add hl,de \ sla e \ rl d
 rra \ jp nc,$+4 \ add hl,de \ sla e \ rl d
 rra \ jp nc,$+4 \ add hl,de \ sla e \ rl d
 rra \ jp nc,$+4 \ add hl,de
 rra
 ret nc
 add hl,de		; Faster than shifting and adding only once
 add hl,de
 ret

Div_Signed_BC_D:	; Division with BC treated as a signed integer
 ld a,$f1		; RET is replaced with POP AF
 ld (Div_Ret),a
 ld a,b
 rla
 push af
 jp nc,Div_BC_D
 xor a
 sub c
 ld c,a
 ld a,0
 sbc a,b
 ld b,a
Div_BC_D:		; HL=BC/D (unsigned, D<128) - in less than 500 cycles!
 ld l,c			; Can be called separately as well (unlike the core of signed
 ld h,b			; multiplication above)
 xor a			; (This routine is simply _divHLbyA unrolled)
 ld b,3			; Saving on size a bit...
Div_BC_D_Loop:
 add hl,hl \ rla \ cp d \ jp c,$+5 \ sub d \ inc l
 add hl,hl \ rla \ cp d \ jp c,$+5 \ sub d \ inc l
 add hl,hl \ rla \ cp d \ jp c,$+5 \ sub d \ inc l
 add hl,hl \ rla \ cp d \ jp c,$+5 \ sub d \ inc l
 add hl,hl \ rla \ cp d \ jp c,$+5 \ sub d \ inc l
 djnz Div_BC_D_Loop
 add hl,hl \ rla \ cp d \ jp c,$+4 \ inc l
Div_Ret:
 ret
 ld a,$c9		; RET is restored
 ld (Div_Ret),a
 ret nc
 xor a
 sub l
 ld l,a
 ld a,0
 sbc a,h
 ld h,a
 ret

ArcTan2_BC:		; Calculating the angle of the (B, C) vector, where B and C are
 ld a,b			; 8-bit signed integers
 ld d,c
 ld e,0			; E contains the angle to add to the result
 rl d
 rra
 and $c0		; A7: sign of C; A6: sign of B
 jr z,AT2_LookUp	; Right bottom (+0)
 cp $80
 jr z,AT2_0		; Right top (+192)
 cp $40
 jr z,AT2_180		; Left bottom (+64)
 ld e,128		; Left top (+128)
 ld a,b
 neg
 ld b,a
 ld a,c
 neg
 ld c,a
 jr AT2_LookUp
AT2_0:
 ld e,192		; Rotating vector
 ld a,c
 ld d,b
 neg
 ld b,a
 ld c,d
 jr AT2_LookUp
AT2_180:
 ld e,64		; Rotating vector
 ld a,b
 ld d,c
 neg
 ld c,a
 ld b,d
AT2_LookUp:
 ld a,b			; Shifting until both coordinates are within 4 bits
 cp 16
 jr nc,AT2_Shift
 ld a,c
 cp 16
 jr c,AT2_ShiftOK
AT2_Shift:
 srl b
 srl c
 jr AT2_LookUp
AT2_ShiftOK:
 ld a,c
 rlca
 rlca
 rlca
 rlca
 or b
 ld c,a
 ld b,0
 ld hl,Angle_Table
 add hl,bc
 ld a,(hl)
 add a,e
 ret

.echo "Arithmetics: "
.echo $-Mul_B_SinDE
.echo "\n"
