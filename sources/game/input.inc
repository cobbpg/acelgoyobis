ReadKeyboard:		; Fills 7 bytes at KeyPressed; if a key is pressed, the corresponding
 ld hl,KeyPressed	; bit is set to ZERO. Works with disabled interrupts as well.
 ld b,7
 ld c,$fe
RK_Loop:
 ld a,$ff
 out (1),a
 ld a,c
 out (1),a
 in a,(1)
 ld (hl),a
 inc hl
 rlc c
 djnz RK_Loop
 ret

ValidateKeys:		; Fills the ValidKey array; if a key was just pressed, the corresponding
 ld hl,KeyPressed	; bit is reset (zeroed). Must be called after ReadKeyboard.
 ld de,KeyState
 ld ix,ValidKey
 ld b,7
VK_Loop:
 ld a,(de)
 or (hl)
 ld (ix),a
 ld a,(hl)
 cpl
 ld (de),a
 inc hl
 inc de
 inc ix
 djnz VK_Loop
 ret

.echo "Keyboard handling: "
.echo $-ReadKeyboard
.echo "\n"
