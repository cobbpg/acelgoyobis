; Note: these routines usually destroy the contents of all registers.
;
; ClearScreen:
; input: HL=virtual screen address
; output: fills 768 bytes at HL with zeroes
;
; FlipScreen (a replica of ION FastCopy by Joe Wingbermuehle):
; input: HL=virtual screen address
; output: displays the virtual screen on the LCD (non-destructively)
;
; AlignSprite:
; input: B=sprite Y size
;        C=sprite X size
;        DE=direct screen address of the sprite
;        HL=pointer to sprite data
; output: draws an aligned sprite at the specified address; useful for
;         displaying little picture elements. After execution, HL points
;         to the data immediately following the sprite, and DE points
;         to the byte immediately below its bottom left corner.
;
; AlignMaskedSprite:
; input: B=sprite Y size
;        C=sprite X size
;        DE=direct screen address of the sprite
;        HL=pointer to sprite data
; output: draws an aligned masked sprite at the specified address;
;         the sprite bytes and the mask bytes are interleaved - each
;         sprite byte is preceded by its corresponding mask byte
;         (first the mask is ANDed, then the sprite is ORed to the
;         contents of the screen)
;
; Sprite:
; input: A=sprite X coordinate
;        L=sprite Y coordinate
;        BC=pointer to sprite data
; output: draws a 8x8 OR sprite on VScreen
;
; WhiteSprite:
; input: A=sprite X coordinate
;        L=sprite Y coordinate
;        BC=pointer to sprite data
; output: draws a 8x8 "white OR" sprite on VScreen (makes the black
;         pixels of the source sprite white on the screen and leaves
;         the others intact
;
; SpriteBig:
; input: B=sprite Y size
;        C=sprite X size (in bytes)
;        D=sprite X coordinate
;        E=sprite Y coordinate
;        HL=sprite address
; output: draws a bigger OR sprite using the 8x8 routine

ClearScreen:			; Clearing the virtual screen at HL (~6500 cycles)
 ld (CS_saveSP+1),sp	; Backing up SP
CS_hackSize1:
 ld de,768
 add hl,de
 ld sp,hl
 ld hl,0
CS_hackSize2:
 ld b,48
CS_loop:
 push hl				; 16 bytes are cleared in 8*11+13=101 cycles
 push hl				; That would be 16*21=336 with LDIR and 16*16+13=269 with LDIs
 push hl
 push hl
 push hl
 push hl
 push hl
 push hl
 djnz CS_loop
CS_saveSP:
 ld sp,0
 ret

FlipScreen:				; Copies the screen at HL to the LCD in ~52000 cycles (ION FastCopy)
 ld a,$80
 out ($10),a
 ld de,755
 add hl,de
 ld a,$20
 ld c,a
 inc hl
 dec hl
FS_column:
 ld b,64
 inc c
 ld de,-767
 out ($10),a
 add hl,de
 ld de,10
FS_inner:
 add hl,de
 inc hl
 inc hl
 inc de
 ld a,(hl)
 out ($11),a
 dec de
 djnz FS_inner
 ld a,c
 cp $2c
 jp nz,FS_column
 ret

AlignSprite:			; Draws a sprite at HL with size B (vert), C (horz) to DE
 push bc
 ld b,0
 push de
 ldir
 pop de
 ld c,12
 ex de,hl
 add hl,bc
 ex de,hl
 pop bc
 djnz AlignSprite
 ret

AlignMaskedSprite:		; Draws a masked sprite at HL with size B (vert), C (horz) to DE
 push bc
 ld b,c
 push de
AMS_loop:
 ld a,(de)
 and (hl)
 inc hl
 or (hl)
 inc hl
 ld (de),a
 inc de
 djnz AMS_loop
 pop de
 ld c,12
 ex de,hl
 add hl,bc
 ex de,hl
 pop bc
 djnz AlignMaskedSprite
 ret

WhiteSprite:
 ld d,a
 ld a,$ac				; XOR H
 ld (S_XORH),a
 inc a					; XOR L
 ld (S_XORL),a
 ld a,d
 call Sprite
 xor a					; NOP
 ld (S_XORH),a
 ld (S_XORL),a
 ret

Sprite:					; Simple 8x8 OR sprite routine
 ld h,0					; A, L: X, Y coordinates; BC: sprite address
 add hl,hl
 add hl,hl
 ld d,h
 ld e,l
 add hl,de
 add hl,de
Sprite_Address:
 ld de,VScreen
 add hl,de
 ld d,0
 ld e,a
 srl e
 srl e
 srl e
 add hl,de				; HL contains the final byte address here
 push hl
 pop ix
 and 7
 ld (S_Shift+1),a		; Shift hack
 ld d,b
 ld e,c
 ld b,8
S_Loop:
 ld a,(de)
 inc de
 ld l,a
 ld h,0
S_Shift:
 jr $
 add hl,hl
 add hl,hl
 add hl,hl
 add hl,hl
 add hl,hl
 add hl,hl
 add hl,hl
 add hl,hl
 ld a,h
 or (ix)
S_XORH:
 nop					; XOR H for white
 ld (ix),a
 ld a,l
 or (ix+1)
S_XORL:
 nop					; XOR L for white
 ld (ix+1),a
 ex de,hl
 ld de,12
 add ix,de
 ex de,hl
 djnz S_Loop
 ret

SpriteBig:				; Drawing a bigger sprite using the 8x8 routine
 ld ix,BigSprite		; B, C: sprite size in bytes; D, E: coordinates; HL: sprite address
 push bc
 ld b,8
 xor a
SB_Clear:
 ld (ix),a
 inc ix
 djnz SB_Clear
 pop bc
 ld a,b
 ld (SB_Size+1),a
SB_Loop:
 push bc
 push de
 push hl
 push de
SB_Size:
 ld de,0
 ld b,c
 ld ix,BigSprite
SB_Copy:
 ld a,(hl)
 add hl,de
 ld (ix),a
 inc ix
 djnz SB_Copy
 pop hl					; Coordinates
 ld bc,BigSprite
 ld a,h
 call Sprite
 pop hl
 inc hl
 pop de
 ld a,d
 add a,8
 ld d,a
 pop bc
 djnz SB_Loop
 ret

DisplayNumber:			; Displaying (OR) a 32-bit integer pointed by HL
 ld c,(hl)				; D, E: Screen coordinates
 inc hl
 ld b,(hl)
 inc hl
 ld (DN_LoWord),bc
 ld c,(hl)
 inc hl
 ld b,(hl)
 inc hl
 ld (DN_HiWord),bc
 ld b,9
DN_Number:				; Outputting one digit per iteration
 push bc
 push de
 ld de,$05f5			; $05F5E100 = 100000000
 ld bc,$e100
 ld a,-1
 or a					; Clearing the carry
DN_Digit:
 inc a
DN_LoWord = $+1
 ld hl,0				; Low word
 sbc hl,bc
 ld (DN_LoWord),hl
DN_HiWord = $+1
 ld hl,0				; High word
 sbc hl,de
 ld (DN_HiWord),hl
 jr nc,DN_Digit
 ld hl,(DN_LoWord)
 ld de,(DN_HiWord)
 add hl,bc \ ex de,hl \ ld bc,$05f5 \ adc hl,bc \ ex de,hl		; DEHL += 100000000
 add hl,hl \ ex de,hl \ adc hl,hl \ ex de,hl					; DEHL *= 2
 push de
 ld b,h
 ld c,l
 add hl,hl \ ex de,hl \ adc hl,hl \ ex de,hl					; DEHL *= 2
 add hl,hl \ ex de,hl \ adc hl,hl \ ex de,hl					; DEHL *= 2
 add hl,bc \ ex de,hl \ pop bc \ adc hl,bc \ ex de,hl			; DEHL += DEHL/4
 ld (DN_LoWord),hl
 ld (DN_HiWord),de
 pop de
 call DisplayDigit
 pop bc
 djnz DN_Number
 ret

DisplayDigit:			; Displaying a digit in A at D, E and advance the coordinates
 ld hl,Numbers			; Calculating the address of the sprite
 ld b,0
 ld c,a
 add a,a
 add a,a
 add hl,bc
 ld c,a
 add hl,bc
 ld b,h
 ld c,l
 ld a,5					; Hacking the sprite routine to render 5 lines only
 ld (S_Loop-1),a
 ld a,d
 ld l,e
 push de
 call Sprite
 pop de
 ld a,d
 add a,5
 ld d,a
 ld a,8					; Restoring the sprite routine
 ld (S_Loop-1),a
 ret

.echo "Graphics routines: "
.echo $-ClearScreen
.echo "\n"
