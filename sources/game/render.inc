RenderFrame:				; Rendering and displaying one frame
 ld a,(FrameCount)
 cp FRAMEDELAY
 ret c						; Rendering after FRAMEDELAY interrupts
ForceRenderFrame:
 xor a
 ld (FrameCount),a
 inc a						; Resetting the push state of the flippers
 ld (LeftPushed),a
 ld (RightPushed),a
 call MoveLeftFlippers		; Taking care of the flippers
 call MoveRightFlippers
 call DrawFlippers
 ld a,(BallY+1)				; Converting ball Y coordinate to table Y offset
 ld d,0						; 0..160 -> -12..108, which is stripped to 0..96
 ld e,a
 ld h,d
 ld l,e
 add hl,hl
 add hl,de
 srl h
 rr l
 srl h
 rr l
 ld a,l						; This is the unbounded Y offset
 sub 8
 jr nc,RF_NotNeg
 xor a
RF_NotNeg:
 cp 103
 jr c,RF_NotBig
 ld a,103
RF_NotBig:
 ld h,d
 ld l,a						; Bounded Y offset
 add hl,hl
 add hl,hl
 ex de,hl
 ld hl,(TableData)
 add hl,de
 add hl,de
 add hl,de
 ld de,VScreen
 ld bc,684
 ldir
 ld l,a
 ld a,(BallY+1)				; Calculating the screen Y coordinate of the ball
 sub l
 sub 2
 cp 60
 jr nc,RF_Flip
 ld l,a
 ld a,(BallX+1)
 sub 2
 ld bc,Ball
 ld h,a
 push hl
 call Sprite
 pop hl
 ld a,h
 ld bc,BallWhite
 call WhiteSprite
RF_Flip:
 call HandleTimers			; Decreasing the event timers and executing their events
 ld hl,VScreen+684
 ld de,VScreen+685
 ld bc,83
 ld (hl),0
 ldir
 ld hl,Score				; Displaying score
 ld de,$1B3A
 call DisplayNumber
 ld hl,VScreen
 jp FlipScreen

MoveLeftFlippers:
 ld a,(KeyPressed+6)		; Left flipper: Y= key
 and 16
 ld (LeftPushed),a			; Saving push state
 ld a,(LeftFlipper)
 jr nz,MLF_Released
 cp 4						; Raising flipper
 ret nc
 inc a
 jr MLF_Exit				; Saving two bytes...
MLF_Released:
 or a						; Lowering flipper
 ret z
 dec a
MLF_Exit:
 ld (LeftFlipper),a
 ret

MoveRightFlippers:
 ld a,(KeyPressed+6)		; Right flipper: Graph key
 and 1
 ld (RightPushed),a			; Saving push state
 ld a,(RightFlipper)
 jr nz,MRF_Released
 cp 4						; Raising flipper
 ret nc
 inc a
 jr MRF_Exit				; Saving two bytes...
MRF_Released:
 or a						; Lowering flipper
 ret z
 dec a
MRF_Exit:
 ld (RightFlipper),a
 ret

DrawFlippers:				; Draws the flippers right into TableData (collision mask)
 ld hl,(TableFlippers)
 ld b,(hl)
 inc hl
DFs_Loop:
 push bc
 ld b,(hl)					; X coordinate + flipper direction
 inc hl
 ld c,(hl)					; Y coordinate
 inc hl
 push hl
 push bc
 call DrawFlipper			; Draws a flipper arm (used for masking as well)
 pop bc
 call UpdateAngles			; Fills the slope table corresponding to the flipper
 pop hl
 pop bc
 djnz DFs_Loop
 ret

DrawFlipper:				; Draws a flipper at B, C into TableData
 ld a,b						; The direction of the flipper is in the upper bit of B
 add a,a
 jr nc,DF_ChooseLeft
 ld a,(RightFlipper)
 ld hl,RightFlipperSprites
 jr DF_Chosen
DF_ChooseLeft:
 ld a,(LeftFlipper)
 ld hl,LeftFlipperSprites
DF_Chosen:
 add a,a
 add a,a
 add a,a
 add a,a
 ld d,0
 ld e,a
 add hl,de
 push hl					; Flipper sprite address in the stack
 ld l,c						; Calculating the target address
 ld h,d
 add hl,hl
 add hl,hl
 ex de,hl
 ld hl,(TableData)
 add hl,de
 add hl,de
 add hl,de
 ld a,b
 and $7f
 srl a
 srl a
 srl a
 ld e,a
 ld d,0
 add hl,de					; HL contains the correct table byte
 pop de
 ld a,b						; Deciding if the sprite is aligned or not
 and 7
 jr nz,DF_UnAligned
 ex de,hl
 ld bc,$0802
 jp AlignSprite				; Using another routine for the aligned version
DF_UnAligned:
 ld (DF_Loop-2),a			; Hacking the loop counter
 ld c,%10000000 \ dec a \ jr z,DF_MaskOK
 ld c,%11000000 \ dec a \ jr z,DF_MaskOK
 ld c,%11100000 \ dec a \ jr z,DF_MaskOK
 ld c,%11110000 \ dec a \ jr z,DF_MaskOK
 ld c,%11111000 \ dec a \ jr z,DF_MaskOK
 ld c,%11111100 \ dec a \ jr z,DF_MaskOK
 ld c,%11111110
DF_MaskOK:
 ld a,c						; Hacking the mask into the inner loop
 ld (DF_Mask1+1),a
 cpl
 ld (DF_Mask2+1),a
 ld b,8
DF_Line:
 push bc
 ld a,(de)
 inc de
 ld b,a
 ld a,(de)
 inc de
 push de
 ld de,0
DF_Loop:
 srl b
 rra
 rr d
 dec e
 jp nz,DF_Loop
 ld c,a
 ld a,(hl)
DF_Mask1:
 and 0
 or b
 ld (hl),a
 inc hl
 ld (hl),c
 inc hl
 ld a,(hl)
DF_Mask2:
 and 0
 or d
 ld (hl),a
 ld de,10
 add hl,de
 pop de
 pop bc
 djnz DF_Line
 ret

DrawSpring:
 ld hl,(TableData)			; Rendering the spring
 ld de,1835
 add hl,de
 ld de,Spring
 ld ixl,7
 ld bc,12
DS_Bytes:
 ld a,(de)
 inc de
 ld (hl),a
 add hl,bc
 dec ixl
 jr nz,DS_Bytes
 ret

.echo "Rendering engine: "
.echo $-RenderFrame
.echo "\n"
