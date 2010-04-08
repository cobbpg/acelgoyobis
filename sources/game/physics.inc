UpdateAngles:			; Updates the angles of a flipper at B, C in TableSlope
 ld hl,FlipperAngles
 ld d,0
 ld a,b					; The direction of the flipper is in the upper bit of B
 add a,a
 jr nc,UD_ChooseLeft
 ld a,(RightFlipper)
 ld e,a
 add hl,de
 ld a,(hl)
 neg
 jr UD_Chosen
UD_ChooseLeft:
 ld a,(LeftFlipper)
 ld e,a
 add hl,de
 ld a,(hl)
UD_Chosen:
 ld d,a					; If the ball is under the flipper, adding 180 degrees
 ld a,(BallY+1)
 sub 4
 cp c
 ld a,d
 jr c,UD_Above
 xor $80
UD_Above:
 push af
 ld a,c					; Calculating the address of the upper left corner
 srl a 
 ld h,0
 ld l,a
 add hl,hl
 add hl,hl
 add hl,hl
 add hl,hl
 ex de,hl
 ld hl,(TableSlope)
 add hl,de
 add hl,de
 add hl,de
 ld a,b
 and $7f
 srl a
 ld d,0
 ld e,a
 add hl,de
 pop af
 ld de,41
 ld b,4					; Filling a 8x4 (16x8 pixels) area with the angle
UD_Line:
 ld (hl),a \ inc hl \ ld (hl),a \ inc hl \ ld (hl),a \ inc hl \ ld (hl),a \ inc hl
 ld (hl),a \ inc hl \ ld (hl),a \ inc hl \ ld (hl),a \ inc hl \ ld (hl),a \ add hl,de
 djnz UD_Line
 ret

MoveBall:				; Taking care of the ball
 ld hl,(BallX)			; BallX += BallVX
 ld de,(BallVX)
 sra d \ rr e \ sra d \ rr e \ sra d \ rr e \ sra d \ rr e
 add hl,de
 ld (BallX),hl
 ld hl,(BallY)			; BallY += BallVY
 ld de,(BallVY)
 sra d \ rr e \ sra d \ rr e \ sra d \ rr e \ sra d \ rr e
 add hl,de
 ld (BallY),hl
 ld hl,(BallVYlow)		; BallVY += GRAVITY / 256
 ld a,(BallVY+1)
PARAM_GRAVITY	.equ	$+1
 ld de,GRAVITY
 add hl,de
 adc a,0
 ld (BallVYlow),hl
 ld (BallVY+1),a
 ld a,(BallX+1)			; Checking left and right sides
 cp 2
 jp p,MB_XLeftOK
 ld a,2
MB_XLeftOK:
 cp 94
 jr c,MB_XRightOK
 ld a,94
MB_XRightOK:
 ld (BallX+1),a
 ld bc,$feff			; Checking eight points of the ball for collision
 call MB_Bounce
 ld bc,$fe01
 call MB_Bounce
 ld bc,$ff02
 call MB_Bounce
 ld bc,$0102
 call MB_Bounce
 ld bc,$0201
 call MB_Bounce
 ld bc,$02ff
 call MB_Bounce
 ld bc,$01fe
 call MB_Bounce
 ld bc,$fffe
MB_Bounce:
 ld a,(BallY+1)			; Checking if the ball touches a wall
 add a,b
 cp 160
 ret nc					; No checks under the table area
 ld (MB_HackY+1),a
 ld h,0
 ld l,a
 add hl,hl
 add hl,hl
 ex de,hl
 ld hl,(TableData)
 add hl,de
 add hl,de
 add hl,de
 ld a,(BallX+1)
 add a,c
 ld (MB_HackX+1),a
 ld d,0
 ld e,a
 srl e
 srl e
 srl e
 add hl,de
 ld c,128
 and 7
 jr z,MB_MaskOK
MB_MaskShift:
 srl c
 dec a
 jr nz,MB_MaskShift
MB_MaskOK:
 ld a,c
 and (hl)
 ret z					; Nothing touched, nothing to do
MB_HackY:
 ld a,0					; Determining the angle of the surface touched
 srl a
 ld h,0
 ld l,a
 add hl,hl
 add hl,hl
 add hl,hl
 add hl,hl
 ex de,hl
 ld hl,(TableSlope)
 add hl,de
 add hl,de
 add hl,de
MB_HackX:
 ld a,0
 srl a
 ld d,0
 ld e,a
 add hl,de
 ld a,(hl)
 or a
 ret z					; Zero (theoretically upwards) angle denotes unmasked area
 push hl
 ld hl,(BallVX)			; Calculating the current angle of the ball's velocity
 add hl,hl
 ld b,h
 ld hl,(BallVY)
 add hl,hl
 ld c,h
 call ArcTan2_BC
 pop hl
 sub (hl)				; No collision at acute angle with the surface normal
 add a,64
 ret p
 push hl
 ld a,(hl)
 ld ix,Sin_Table		; Just pushing the ball 1/4 pixel away from the point touched
 ld b,0
 ld c,a
 add ix,bc
 ld a,(ix)				; BallX += Sin(Angle) / 4
 ld e,a
 add a,a
 sbc a,a
 ld d,a
 ld (MB_BVX),de
 sra e
 ld hl,(BallX)
 add hl,de
 ld (BallX),hl
 ld a,c
 sub 64
 ld c,a
 ld ix,Sin_Table
 add ix,bc
 ld a,(ix)				; BallY -= Cos(Angle) / 4
 ld e,a
 add a,a
 sbc a,a
 ld d,a
 ld (MB_BVY),de
 sra e
 ld hl,(BallY)
 add hl,de
 ld (BallY),hl
 ld a,(BallX+1)			; Bounce verification
 ld d,a
 ld e,h					; BallY integer part
 ld hl,(BounceList)
 ld a,(BounceNum)
 or a
 jr z,MB_NoBounce		; No bounces at all on the table
 ld b,a
 ld c,0
MB_VBounce:
 push hl
 ld a,(hl)
 sub 3
 cp d
 jp p,MB_SkipB			; Ball is far in the left
 inc hl
 ld a,(hl)
 sub 3
 cp e					; Ball is far above
 jp p,MB_SkipB
 inc hl
 ld a,(hl)
 add a,2
 cp d
 jr c,MB_SkipB			; Ball is far in the right
 inc hl
 ld a,(hl)
 add a,2
 cp e
 jr c,MB_SkipB			; Ball is far below
 ld c,1
MB_SkipB:
 pop hl
 inc hl
 inc hl
 inc hl
 inc hl
 djnz MB_VBounce
 ld a,c
 or a
 jr z,MB_NoBounce		; Applying extra spring force in the bounce area
MB_BVX = $+1
 ld hl,0
 call NegHL32
 ld de,(BallVX)
 add hl,de
 ld (BallVX),hl
MB_BVY = $+1
 ld hl,0
 call NegHL32
 ld de,(BallVY)
 add hl,de
 ld (BallVY),hl
MB_NoBounce:
 pop hl
 ld a,(hl)				; Var = BallVX * Sin(Angle) / 2
 ld (MB_HackAngle+1),a
 ld e,a
 push hl
 ld bc,(BallVX)
 call Mul_BC_SinE
 ld (MB_Var),hl
 pop hl
 ld a,(hl)				; Var -= BallVY * Cos(Angle) / 2
 add a,192
 ld e,a
 push hl
 ld bc,(BallVY)
 call Mul_BC_SinE
 ex de,hl
 ld hl,(MB_Var)
 add hl,de
 add hl,hl				; Var *= 2
 ld (MB_Var),hl
 pop hl
 ld a,(hl)				; BallVX -= Var * Sin(Angle) * 3
 add a,128
 ld e,a
 push hl
 ld bc,(MB_Var)
 call Mul_BC_SinE
 ex de,hl
 ld hl,(BallVX)
 add hl,de
 add hl,de
 add hl,de
 ld (BallVX),hl
 pop hl
 ld a,(hl)				; BallVY += Var * Cos(Angle) * 3
 add a,64
 ld e,a
 push hl
 ld bc,(MB_Var)
 call Mul_BC_SinE
 ex de,hl
 ld hl,(BallVY)
 add hl,de
 add hl,de
 add hl,de
 ld (BallVY),hl
 pop hl
 ld a,(hl)
 add a,64
 and 128
 rlca
 ld (MB_HackPush+1),a	; A is 1 if the surface touched is pointing downwards
 ld hl,(TableFlippers)	; Handling collision with flippers
 ld b,(hl)
 inc hl
MB_Flippers:
 push bc
 ld a,(hl)				; Flipper X coordinate
 ld de,LeftFlipper
 ld b,3
 or a
 jp p,MB_DirOK
 inc de					; Because RightFlipper comes just after LeftFlipper
 ld b,0
MB_DirOK:
 ld (MB_HackDir+1),de
 and $7f
 ld d,a
 ld a,b
 ld (MB_HackJump+1),a
 pop bc
 inc hl
 ld e,(hl)				; Flipper Y coordinate
 inc hl
 ld a,(BallY+1)			; Checking if the ball is within the flipper's area
 add a,2
 sub e
 jr c,MB_NoFlipper
 cp 12
 jr nc,MB_NoFlipper
 ld a,(BallX+1)
 add a,1
 sub d
 jr c,MB_NoFlipper
 cp 18
 jr nc,MB_NoFlipper
MB_HackJump:
 jr MB_HackAngle
 cpl
 add a,19
MB_HackAngle:
 ld e,0					; Angle of flipper surface
 push de
PARAM_FORCEFACTOR = $+1
 ld de,FORCEFACTOR		; Force: proportional to the distance from the axis of rotation
 call Mul_A_DE			; of the flipper
 ld b,h
 ld c,l
 pop de
MB_HackDir:
 ld hl,0				; No force exerted if the flipper is at either extreme position
 ld a,(hl)
 or a
 ret z
 cp 4
 ret z
 inc hl					; No force exerted if the flipper is moving away from the ball
 inc hl
 ld a,(hl)
 ld h,0					; If the flipper is being pushed, A will be 1, otherwise 0
 or a
 jr nz,MB_PushOK
 inc h
MB_PushOK:
 ld a,h
MB_HackPush:
 xor 0					; A=0 if the flipper was touched from the bottom while being
 ret z					; pushed or touched at the top while being released
 push de
 push bc
 call Mul_BC_SinE
 ex de,hl
 ld hl,(BallVX)
 add hl,de
 ld (BallVX),hl
 pop bc
 pop de
 ld a,e
 add a,192
 ld e,a
 call Mul_BC_SinE
 ex de,hl
 ld hl,(BallVY)
 add hl,de
 ld (BallVY),hl
 ret
MB_NoFlipper:
 djnz MB_Flippers
 ret

Mul_BC_SinE:			; HL=BC*sin(E*PI/128)/2 - BC is signed
 ld d,0
 ld hl,Sin_Table
 add hl,de
 ld e,(hl)
 ld a,e
 add a,a
 sbc a,a
 ld d,a					; DE=sin(E)
 push de				; To calculate: HL=ext(hi(C*DE))+B*DE+s*E<<8
 ld a,c
 call Mul_A_DE			; C*DE
 pop de
 ld c,h					; C=hi(C*DE)
 push de
 ld a,b
 call Mul_A_DE			; B*DE
 ld e,c
 ld a,e
 add a,a
 sbc a,a
 ld d,a					; DE=ext(hi(C*DE)) - sign extension
 add hl,de				; HL=ext(hi(C*DE))+B*DE
 pop de
 ld a,b
 add a,a
 ret nc
 ld a,h
 sub e					; HL-=E<<8 if BC is negative
 ld h,a
 ret

NegHL32:				; Multiplies HL by -32
 add hl,hl
 add hl,hl
 add hl,hl
 add hl,hl
 add hl,hl
NegHL:					; Multiplies HL by -1
 ld a,l
 cpl
 ld l,a
 ld a,h
 cpl
 ld h,a
 inc hl
 ret

.echo "Physics code: "
.echo $-UpdateAngles
.echo "\n"
