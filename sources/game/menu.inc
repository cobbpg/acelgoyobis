Menu:
 ld (MenuAddress),hl		; Saving the pointer
 ld a,1						; Putting the cursor to the first item
 ld (MenuItem),a
 ld a,(hl)
 ld (MenuItems),a
 ld b,(hl)
 ld de,MenuCoords			; Initialising the coordinates
 ld a,MENURIGHT
Menu_ClearX:
 ld (de),a
 inc de
 djnz Menu_ClearX
 ld hl,VScreen
 call ClearScreen
 ld hl,LogoData
 ld de,VScreen+12
 ld bc,288
 ldir
Menu_Loop:
 ld a,(FrameCount)
 inc a
 ld (FrameCount),a
 cp DELAYFACTOR
 jr c,Menu_RenderOK
 xor a
 ld (FrameCount),a
 call UpdateCoordinates
 call DisplayMenu
Menu_RenderOK:
 call ReadKeyboard			; Reading keypresses
 call ValidateKeys			; Preventing repetition
 call MenuDelay
 ld a,(ValidKey+6)			; 2nd
 and 32
 jr z,Menu_Jump
 ld a,(ValidKey)
 and 1
 call z,Menu_Down
 ld a,(ValidKey)
 and 8
 call z,Menu_Up
 jr Menu_Loop

MenuDelay:					; Simple delay
 ld bc,$80
MD_Loop:
 push hl
 pop hl
 dec bc
 ld a,b
 or c
 jr nz,MD_Loop
 ret

Menu_Jump:
 ld a,(MenuItem)
 add a,a
 dec a
 ld d,0
 ld e,a
 ld hl,(MenuAddress)
 add hl,de
 ld a,(hl)
 inc hl
 ld h,(hl)
 ld l,a
 jp (hl)

Menu_Down:
 ld hl,(MenuAddress)
 ld a,(MenuItem)
 inc a
 cp (hl)
 jr c,Menu_Update
 jr z,Menu_Update
 ld a,1
 jr Menu_Update

Menu_Up:
 ld a,(MenuItem)
 dec a
 jr nz,Menu_Update
 ld a,(MenuItems)

Menu_Update:
 ld (MenuItem),a
 ret

UpdateCoordinates:
 ld bc,(MenuItem)			; B = MenuItems; C = MenuItem
 ld de,MenuCoords
UC_Loop:
 ld a,(MenuItems)
 sub b
 inc a
 cp c
 ld a,(de)
 jr nz,UC_NotCurrent
 cp MENULEFT
 jr z,UC_EndLoop
 dec a
 jr UC_EndUpdate
UC_NotCurrent:
 cp MENURIGHT
 jr z,UC_EndLoop
 inc a
UC_EndUpdate:
 ld (de),a
UC_EndLoop:
 inc de
 djnz UC_Loop
 ret

FireEffect:					; Make BC bytes at HL a screen in fire
 push hl
 ld de,12
 add hl,de
 ex de,hl
 pop hl
FE_CHack	.equ	$+2
 ld ix,1000
FE_Clear:
 ld a,(de)
 and (ix)
 ld (hl),a
 inc de
 inc hl
 inc ix
 dec bc
 ld a,b
 or c
 jp nz,FE_Clear
 ld a,ixh
 and 37
 ld ixh,a
 ld (FE_CHack),ix
 ret

DisplayMenu:				; Displaying the menu at its current state
 ld hl,VScreen+324
 ld bc,360
 call FireEffect
 ld hl,(MenuAddress)
 ld b,(hl)
 inc hl
 ld a,b
 add a,a
 ld d,0
 ld e,a
 add hl,de
 ld de,MenuCoords
DM_Loop:
 push bc
 ld a,(de)
 inc de
 push de
 ld d,a
 ld a,(MenuItems)
 sub b
 add a,a
 ld e,a
 add a,e
 add a,e
 add a,32
 ld e,a
 ld b,(hl)
 inc hl
 ld c,5
 push hl
 push bc
 call SpriteBig
 pop de
 pop hl
 ld e,d
 ld d,0
 add hl,de
 add hl,de
 add hl,de
 add hl,de
 add hl,de
 pop de
 pop bc
 djnz DM_Loop
 ld hl,VScreen
 jp FlipScreen

.echo "Menu code: "
.echo $-Menu
.echo "\n"
