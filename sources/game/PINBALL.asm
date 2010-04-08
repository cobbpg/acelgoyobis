.NOLIST

; To compile the different versions you have to define the following macro labels at the beginning:
;
; TI82C		- TI-82 CrASH
; TI82C196	- TI-82 CrASH 19.006
; TI82S		- TI-82 SNG
; TI83		- TI-83 ION
; TI83V		- TI-83 Venus
; TI83P		- TI-83+ ION

; -----[ Calculator dependent values ]---------------------------------------------------------------------------------------

#include "models.inc"		; Model specific information: equates and header

; -----[ Generic code ]------------------------------------------------------------------------------------------------------

BackupSP		.equ	Vars			; 2 bytes	; The initial value of SP
SaveSP			.equ	Vars+2			; 2 bytes	; Temporary storage for SP
FrameCount		.equ	Vars+4			; 1 byte	; Increased by the timer interrupt

MenuData		.equ	Vars+5			; Can peacefully coexist with arcade variables

MB_Var			.equ	Vars+5			; 2 bytes	; Used in MoveBall while playing
TableSlope		.equ	Vars+7			; 2 bytes	; Pointer to the current slope array
TableData		.equ	Vars+9			; 2 bytes	; Pointer to the current table image
TableFlippers	.equ	Vars+11			; 2 bytes	; Pointer to flipper data
GizmoList		.equ	Vars+13			; 2 bytes	; Pointer to gizmo pointers
TimerList		.equ	Vars+15			; 2 bytes	; Pointer to timer pointers
CounterList		.equ	Vars+17			; 2 bytes	; Pointer to counter pointers
BounceList		.equ	Vars+19			; 2 bytes	; Pointer to bounce objects (!)
BounceNum		.equ	Vars+21			; 1 byte	; Number of bounces
ObjSrc			.equ	Vars+22			; 2 bytes	; Pointer to clean objects
ObjDst			.equ	Vars+24			; 2 bytes	; Pointer to dynamic objects
ObjLen			.equ	Vars+26			; 2 bytes	; Total length of object block
BallData		.equ	Vars+28			; 9 bytes	; Dynamic ball variables
TableVars		.equ	BallData+9		; 4 bytes	; Table state variables
GameVars		.equ	TableVars+4		; ? bytes	; Gameplay information

__X				.equ	0					; 2 bytes
__Y				.equ	2					; 2 bytes
__VX			.equ	4					; 2 bytes
__VYlow			.equ	6					; 1 byte (required for accuracy)
__VY			.equ	7					; 2 bytes

BallX			.equ	BallData+__X		; Ball X coordinate
BallY			.equ	BallData+__Y		; Ball Y coordinate
BallVX			.equ	BallData+__VX		; Ball X velocity
BallVYlow		.equ	BallData+__VYlow	; Ball Y velocity lowest byte
BallVY			.equ	BallData+__VY		; Ball Y velocity

__LeftFlipper	.equ	0							; 1 byte	; The relative addresses
__RightFlipper	.equ	__LeftFlipper+1				; 1 byte	; of these variables must
__LeftPushed	.equ	__LeftFlipper+2				; 1 byte	; remain as defined
__RightPushed	.equ	__RightFlipper+2			; 1 byte

LeftFlipper		.equ	TableVars+__LeftFlipper		; Left flipper height (0-4)
RightFlipper	.equ	TableVars+__RightFlipper	; Right flipper height (0-4)
LeftPushed		.equ	TableVars+__LeftPushed		; Left flipper pushed (0 if yes)
RightPushed		.equ	TableVars+__RightPushed		; Right flipper pushed (0 if yes)

__BonusVal		.equ	0							; 4 bytes
__BonusMul		.equ	4							; 1 byte
__Lives			.equ	5							; 1 byte
__Score			.equ	6							; 4 bytes
__TiltFrames	.equ	10							; 2 bytes

BonusVal		.equ	GameVars+__BonusVal			; Current bonus value
BonusMul		.equ	GameVars+__BonusMul			; Current bonus multiplier
Lives			.equ	GameVars+__Lives			; Number of balls available
Score			.equ	GameVars+__Score			; Current score
TiltFrames		.equ	GameVars+__TiltFrames		; Number of frames to the next legal tilt

MenuAddress		.equ	MenuData		; Pointer to menu data
MenuItem		.equ	MenuData+2		; Current menu item selected
MenuItems		.equ	MenuData+3		; Number of menu items
MenuCoords		.equ	MenuData+4		; X coordinates of items (max. 10)

BigSprite			.equ	MenuData+14		; 8 bytes	; Used in SpriteBig
TablePointers		.equ	MenuData+22		; 20 bytes	; Pointers to available tables
TableMenu			.equ	VScreenB		; Pointer to dynamic menu needed for choosing tables
Sin_Table			.equ	VScreenB		; Sine table used during game
RightFlipperSprites	.equ	VScreenB+256	; Sprites of the rightward flippers
TableCorrection		.equ	VScreenB+766	; Pointer to the current table

FRAMEDELAY	.equ	3		; Number of interrupts (ticks) to wait between game frames
DELAYFACTOR	.equ	20		; Number of interrupts to wait between menu frames
MENULEFT	.equ	6		; X position of selected item
MENURIGHT	.equ	12		; X position of other item
GRAVITY		.equ	6000	; Gravitational acceleration (1/65536 pixels per ticks squared)
FORCEFACTOR	.equ	1400	; Strength of the flippers
TILTFACTOR	.equ	1000	; Minimum number of frames between subsequent tilts

MEMUSED		.equ	$1300	; Memory needed to store slope and object data in the game

; -----[ Entry point ]-------------------------------------------------------------------------------------------------------

Main:

#ifdef TI82

#ifdef TI82S

 bcall(DEL_TMP)
 ld hl,MEMUSED
 bcall(MEM_FREE)
 jr nc,EnoughMem
 bcall(CREATE_TMP)
 ret
EnoughMem:
 ld hl,MEMUSED				; Nearly all RAM is allocated to remove phantom table files
IncreaseMem:
 ld (MEMALLOC),hl
 ld d,8						; This increment is about half of a minimal table, therefore sufficiently small
 add hl,de
 push hl
 bcall(MEM_FREE)
 pop hl
 jr nc,IncreaseMem
 ld hl,(MEMALLOC)
 ld de,LastByte
 bcall(INSERT_MEM)
 bcall(CREATE_TMP)
 ld hl,LastByte				; Initialising pointers to the free RAM (82 SNG)
 ld (MEMSTART),hl
 ld de,(MEMALLOC)
 add hl,de
 ld (MEMEND),hl

#else

 ld hl,LastByte				; Initialising pointers to the free RAM (82 CrASH)
 ld (MEMSTART),hl
 ld hl,LastByte+MEMUSED
 ld (MEMEND),hl

#endif

#else

 ld de,(MEMEND)				; Checking free memory before doing anything (83 and 83+)
 ld hl,(MEMSTART)
 ld bc,MEMUSED
 add hl,bc
 ret c
 sbc hl,de
 ret nc						; Exit if there isn't enough memory

#endif

CheckTables:
 ld de,(MEMSTART)			; Clearing free RAM
 ld hl,(MEMEND)
 scf
 sbc hl,de
 ld b,h
 ld c,l
 ld h,d
 ld l,e
 inc de
 ld (hl),0
 ldir
 ld hl,0
 ld (TableCorrection),hl
 ld hl,LastByte-1
 call FindTable
 ld a,h
 cp $f0
 jr nc,FastExit				; Exit if no tables found

Start:
 im 1						; For MirageOS and CrASH compatibility
 di
 ld (BackupSP),sp
 res 3,(iy+5)				; textinverse/textflags
 set 7,(iy+20)				; textwrite/sgrflags
Trunk:
 ld hl,0
 ld (TableCorrection),hl
 ld hl,MainMenu				; Entering the main menu
 call Menu
 jr Trunk

Exit:						; This is where the program should jump when the user quits
 call CorrectChecksum
 ld sp,(BackupSP)
 res 7,(iy+20)				; textwrite/sgrflags

FastExit:

#ifdef TI82S

 bcall(DEL_TMP)				; Freeing memory allocated at the beginning
 ld de,(MEMALLOC)
 ld hl,LastByte
 bcall(DEL_MEM)
 bcall(CREATE_TMP)

#endif

#ifdef TI83V

 call $475D					; _clrScrnFull (the screen needs to be cleared for Venus)
 call $4775					; _homeUp

#endif

 ei
 ret

; -----[ High scores ]-------------------------------------------------------------------------------------------------------

DisplayHighScores:			; Display the high scores for every table
 ld hl,VScreen
 call ClearScreen
 ld hl,HighScoreLabel
 ld de,$1206
 ld bc,$0805
 call SpriteBig
 ld hl,LastByte-1
 ld a,20
DHS_Loop:					; Looking for tables and saving their addresses
 push af
 inc hl
 call FindTable
 ld a,h
 cp $f0
 jr nc,DHS_End
 pop bc						; Retrieving Y coordinate
 push bc
 ld c,2
 ld (PenColRow),bc
 push hl					; HL points to the table
 ld de,6					; Offset to table title
 add hl,de
 push hl
 bcall(_vputs)
 pop de
 pop hl
 pop bc
 push bc
 push hl
 ex de,hl					; Displaying score
DHS_EndName:
 xor a
 or (hl)
 inc hl
 jr nz,DHS_EndName
 ld e,b
 inc e
 ld d,50
 call DisplayNumber
 pop hl
 pop af
 add a,8					; Increasing Y coordinate
 jr DHS_Loop
DHS_End:
 pop af
 cp 20
 jr z,Exit					; Error if there are no tables
 ld hl,VScreen
 call FlipScreen
DHS_WaitKey:				; Waiting for 2nd
 call ReadKeyboard
 call ValidateKeys
 ld a,(ValidKey+6)			; 2nd
 and 32
 jr nz,DHS_WaitKey
 ret

; -----[ Arcade part ]-------------------------------------------------------------------------------------------------------

Game:
 call BuildTableMenu
 ld hl,TableMenu
 jp Menu
GameTableChosen:
 call BuildData				; Calculating derived in-game data
 call FindChosenTable
 call LoadTable
 ld a,3
 ld (Lives),a
 ld hl,0
 ld (Score),hl
 ld (Score+2),hl
 call InitTable
GameLoop:
 call CheckBall				; Ball management
 call RenderFrame			; Rendering the frame if FRAMEDELAY frames have been passed
							; Also moving the flippers if necessary and handling the timers
 call MoveBall				; Moving the ball and adjusting its speed if needed
 call Interactions			; Handling interactive elements
 call ReadKeyboard			; Reading keyboard mask
 call ValidateKeys			; Registering keyboard changes
 ld hl,FrameCount			; Advancing the frame counter
 inc (hl)
 ld a,(ValidKey+1)			; Enter
 and 1
 call z,Tilt
 ld a,(ValidKey+6)			; 2nd
 bit 5,a
 jr z,GameEnded
 and 4						; Zoom
 jr nz,GameLoop

GamePaused:					; Displaying 'PAUSE' and waiting for 2nd
 ld hl,PausedPicture
 ld de,VScreen+351
 ld b,7
GP_Loop:
 push bc
 ld a,(de)
 and 254
 ld (de),a
 inc de
 ldi
 ldi
 ldi
 ldi
 ex de,hl
 ld bc,7
 add hl,bc
 ex de,hl
 pop bc
 djnz GP_Loop
 ld hl,VScreen
 call FlipScreen
GP_Wait:
 call ReadKeyboard
 call ValidateKeys
 ld a,(ValidKey+6)			; Zoom
 and 4
 jr nz,GP_Wait
 jr GameLoop

GameEnded:
 call DisplayScore
 ld hl,(TableCorrection)	; Looking for the current high score
 inc hl
 inc hl
GE_EndName:
 xor a
 or (hl)
 inc hl
 jr nz,GE_EndName
 push hl
 ld c,(hl)					; BC: high score low word
 inc hl
 ld b,(hl)
 inc hl
 ld e,(hl)					; DE: high score high word
 inc hl
 ld d,(hl)
 pop ix
 ld hl,(Score+2)
 or a
 sbc hl,de
 jr c,GE_Finish
 jr nz,GE_NewScore
 ld hl,(Score)
 sbc hl,bc
 jr c,GE_Finish
GE_NewScore:
 ld hl,(Score)				; Updating high score
 ld (ix),l
 ld (ix+1),h
 ld hl,(Score+2)
 ld (ix+2),l
 ld (ix+3),h
GE_Finish:
 jp CorrectChecksum			; So the table is found again

Tilt:						; Tilting the ball if possible
 ld hl,(TiltFrames)			; If it's still illegal to tilt, the ball is lost
 ld a,h
 or l
 jr nz,LoseBall
 ld bc,-8					; Random upward acceleration to the ball
 ld a,(Score)
 ld e,a
 ld d,0
 ld hl,(BallVX)
 add hl,de
 add hl,de
 add hl,de
 add hl,bc
 ld (BallVX),hl
 ld a,(BonusVal)
 ld e,a
 ld hl,(BallVY)
 sbc hl,de
 sbc hl,de
 sbc hl,de
 ld (BallVY),hl
 ld hl,TILTFACTOR			; Starting tilt frame counter
 ld (TiltFrames),hl
 ret

CheckBall:
 ld hl,(TiltFrames)			; Decreasing the tilt frame count if active
 ld a,h
 or l
 jr z,CB_TiltOK
 dec hl
 ld (TiltFrames),hl
CB_TiltOK:
 ld a,(BallY+1)				; Checking if the ball was lost
 cp 163
 ret c						; Returning if not yet
LoseBall:
 ld a,(Lives)
 dec a
 jr nz,CB_LivesRemain
 pop hl						; All the lives are lost
 jp GameEnded
CB_LivesRemain:
 ld (Lives),a
 call DisplayScore
 ld hl,(ObjSrc)				; Restoring table state
 ld de,(ObjDst)
 ld bc,(ObjLen)
 ldir
InitTable:
 ld hl,$5B00				; Initialising variables
 ld (BallX),hl
 ld hl,$9600
 ld (BallY),hl
 ld h,l
 ld (BallVX),hl
 ld (BonusVal),hl
 ld (BonusVal+2),hl
 ld (LeftFlipper),hl
 ld (TiltFrames),hl
 ld a,l
 ld (BallVYlow),a
 ld a,1
 ld (BonusMul),a
 call DrawSpring
 call InitElements			; Rendering the initial state of interactive elements
 call ForceRenderFrame
IT_StartWaiting:			; Waiting for right arrow
 call ReadKeyboard
 call ValidateKeys
 ld a,(KeyPressed)
 and 4
 jr nz,IT_StartWaiting
 ld hl,-5000				; Strength
IT_WaitSpring:
 ld bc,$1000
IT_SpringDelay:
 push hl
 pop hl
 dec bc
 ld a,b
 or c
 jr nz,IT_SpringDelay
 ld ix,VScreen+659			; Drawing back the spring
 ld b,11
 ld de,-12
IT_DrawBack:
 ld a,(ix-12)
 ld (ix),a
 add ix,de
 djnz IT_DrawBack
 push hl
 ld hl,VScreen
 call FlipScreen
 call ReadKeyboard
 pop hl
 ld de,-1000
 add hl,de
 ld a,(KeyPressed)			; Right
 and 4
 jr nz,IT_Release
 ld a,h
 cp $d5
 jr nc,IT_WaitSpring
IT_Release:
 ld (BallVY),hl
 ld a,255
 ld (FrameCount),a
 ret

DisplayScore:
 call ForceRenderFrame      ; Rendering the losing moment
 ld hl,VScreen+242			; Clearing the middle of the screen
 ld a,23
DS_ClearLoop:
 ld e,0
 cp 9
 jr nz,DS_NoBlack
 dec e
DS_NoBlack:
 ld b,8
DS_ClearByte:
 ld (hl),e
 inc hl
 djnz DS_ClearByte
 inc hl
 inc hl
 inc hl
 inc hl
 dec a
 jr nz,DS_ClearLoop
 ld hl,Score				; Displaying score
 ld de,$2216
 call DisplayNumber
 ld hl,BonusVal				; Displaying bonus
 ld de,$221c
 call DisplayNumber
 ld a,(BonusMul)			; Displaying multiplier
 ld b,-1
DS_CalcBonus10:
 inc b
 sub 10
 jr nc,DS_CalcBonus10
 push af
 ld a,b
 ld de,$121c
 call DisplayDigit
 pop af
 add a,10
 call DisplayDigit
 ld ix,VScreen+351			; The x operator
 ld a,(ix)
 or 5
 ld (ix),a
 ld a,(ix+12)
 or 2
 ld (ix+12),a
 ld a,(ix+24)
 or 5
 ld (ix+24),a
 ld bc,(BonusVal)
 ld de,(BonusVal+2)
 ld ix,(Score)
 ld hl,(Score+2)
 ld a,(BonusMul)
DS_AddBonus:
 add ix,bc
 adc hl,de
 dec a
 jr nz,DS_AddBonus
 ld (Score),ix
 ld (Score+2),hl
 ld hl,Score				; Displaying new score
 ld de,$2224
 call DisplayNumber
 ld hl,VScreen
 call FlipScreen
DS_Wait2nd:
 call ReadKeyboard
 call ValidateKeys
 ld a,(ValidKey+6)			; 2nd
 and 32
 jr nz,DS_Wait2nd
 ret

BuildTableMenu:				; Create a menu containing the table titles
 ld hl,LastByte-1
 ld de,TablePointers
 xor a
BTM_Loop:					; Looking for tables and saving their addresses
 push af
 inc hl
 push de
 call FindTable
 pop de
 ld a,h
 cp $f0
 jr nc,BTM_End
 ld a,l
 ld (de),a
 inc de
 ld a,h
 ld (de),a
 inc de
 pop af
 inc a
 jr BTM_Loop
BTM_End:
 pop af
 or a
 jp z,Exit					; Error if zero!!!!!
 ld hl,TableMenu			; Building the jump address table
 ld (hl),a
 inc hl
 ld b,a
 ld de,GameTableChosen
BTM_MenuAddress:
 ld (hl),e
 inc hl
 ld (hl),d
 inc hl
 djnz BTM_MenuAddress
 push hl
 ld hl,VScreen				; Drawing the titles
 call ClearScreen
 ld c,a
 ld b,a
BTM_Titles:
 push bc
 ld a,c
 sub b
 ld d,0
 add a,a
 push af
 ld e,a
 ld hl,TablePointers
 add hl,de
 ld a,(hl)
 inc hl
 ld h,(hl)
 ld l,a
 ld e,6						; Offset to table title
 add hl,de
 pop af
 ld b,a
 add a,a
 add a,b					; 6*counter (Y coordinate)
 ld b,a
 ld c,0
 ld (PenColRow),bc
 bcall(_vputs)
 pop bc
 djnz BTM_Titles
 pop de
 ld hl,VScreen+12
 ld b,c
 ld a,12					; String width: constant 12
BTM_CopyTitles:
 push bc
 ld (de),a
 inc de
 ld bc,60
 ldir
 ld c,a						; BC = 12
 add hl,bc
 pop bc
 djnz BTM_CopyTitles
 ret

FindChosenTable:
 ld hl,0
 ld (TableCorrection),hl
 ld a,(MenuItem)
 ld b,a
 ld hl,LastByte-1
FCT_Loop:
 push bc
 inc hl
 call FindTable
 pop bc
 djnz FCT_Loop
 ret

FindTable:					; Find a table in the memory starting from HL
 ld a,(hl)
 cp 83						; 83, 12, 217, 154
 jr nz,FT_Skip
 ld d,h
 ld e,l
 inc de \ ld a,(de) \ cp 12 \ jr nz,FT_Skip
 inc de \ ld a,(de) \ cp 217 \ jr nz,FT_Skip
 inc de \ ld a,(de) \ cp 154 \ jr nz,FT_Skip
 push hl
 ex de,hl
 inc hl
 ld (TableCorrection),hl
 call CalculateChecksum
 ex de,hl
 pop hl
 ld a,(de)
 xor ixl
 jr nz,FT_Skip
 inc de
 ld a,(de)
 xor ixh
 ret z						; At this point HL points to the base address of the table data
FT_Skip:
 inc hl
 ld a,h
 cp $f0
 jr c,FindTable
 ret

CalculateChecksum:			; Calculate checksum of block at HL (starting with size word)
 ld c,(hl)					; The sum will be in IX, and HL points after the block in the end
 inc hl
 ld b,(hl)
 inc hl
 ld de,0
 push de
 pop ix
CC_Loop:
 ld e,(hl)
 inc hl
 add ix,de
 dec bc
 ld a,b
 or c
 jr nz,CC_Loop
 ret

CorrectChecksum:			; Correcting the checksum of the current table
 ld hl,(TableCorrection)
 ld a,h
 or a
 ret z						; Do nothing if no table was selected
 call CalculateChecksum
 push ix
 pop de
 ld (hl),e
 inc hl
 ld (hl),d
 ret

LoadTable:					; Load table pointed by HL
 ld a,h
 cp $f0						; Returning to the shell if there is no valid table
 jr c,LT_Valid
 jp Exit
LT_Valid:					; Skipping the name, the high score and the number of elements
 ld de,6
 add hl,de
LT_EndName:
 xor a
 or (hl)
 inc hl
 jr nz,LT_EndName
 ld e,4
 add hl,de					; Skipping the high score
 push hl
 pop ix						; IX points to the number of various elements
 add hl,de
 ld (TableFlippers),hl		; Flipper data
 ld e,(hl)
 add hl,de
 add hl,de
 inc hl
 ld (TableData),hl			; Table image
 ld de,12*160
 add hl,de
 call UncompressTable		; Decompressing slope data
 ld c,(hl)
 inc hl
 ld b,(hl)
 inc hl
 ld (ObjSrc),hl				; Saving object block data for later retrieval
 ld (ObjDst),de
 ld (ObjLen),bc
 push de					; The first byte after the slope data
 ldir						; Interactive objects are copied there
 pop hl						; DE: pointer table; HL: object data
 ld a,(ix)					; Number of gizmos
 ld (GizmoList),de
 ld (de),a
 inc de
 or a
 jr z,LT_GizmoOK
 ld b,a
LT_GizmoList:
 push bc
 ld a,l						; Storing gizmo pointer
 ld (de),a
 inc de
 ld a,h
 ld (de),a
 inc de
 ld a,(hl)					; Skipping event script
 ld bc,5
 rra
 jr nc,LT_SpriteOK
 ld bc,13
LT_SpriteOK:
 add hl,bc
 call SkipEvents
 pop bc
 djnz LT_GizmoList
LT_GizmoOK:
 ld a,(ix+1)				; Number of timers
 ld (TimerList),de
 call LT_TimerCounter
 ld a,(ix+2)				; Number of counters
 ld (CounterList),de
 call LT_TimerCounter
 ld a,(ix+3)				; Number of bounces
 ld (BounceNum),a
 ld (BounceList),hl			; Bounce objects at DE
 ret

LT_TimerCounter:			; Building pointer table for timers and counters (data at HL)
 ld (de),a					; The number of objects of the given type
 inc de
 or a
 ret z						; No elements of this kind
 ld b,a
LT_TCList:
 push bc
 ld a,l						; Storing object pointer
 ld (de),a
 inc de
 ld a,h
 ld (de),a
 inc de
 inc hl
 ld b,0						; Skipping event script
 call SkipEvents
 pop bc
 djnz LT_TCList
 ret

SkipEvents:					; Jumping over event list at HL; B must be 0
 ld a,(hl)
 inc hl
 or a
 ret z
 push hl
 ld c,a
 ld hl,EventLengths-1
 add hl,bc
 ld c,(hl)
 pop hl
 add hl,bc
 jr SkipEvents

UncompressTable:			; Uncompresses a table at HL
 ld de,(MEMSTART)			; Setting up pointers
 ld (TableSlope),de
 ld c,(hl)					; Size of compressed data in BC
 inc hl
 ld b,(hl)
 inc hl
UCT_Loop:
 ld a,(hl)
 ldi
 or a
 jr nz,UCT_Counter			; Jumping if A is not zero
 ld a,(hl)					; Outputting (HL) nullbytes
 inc hl
 dec bc
 or a
 jr z,UCT_Counter
 push bc
 ld b,a
 xor a
UCT_PutZeros:
 ld (de),a
 inc de
 djnz UCT_PutZeros
 pop bc
UCT_Counter:
 ld a,b
 or c
 jr nz,UCT_Loop
 push hl
 ld hl,(MEMSTART)			; Correcting address
 ld de,3840
 add hl,de
 ex de,hl
 pop hl
 ret

BuildData:					; Calculating in-game data
 ld de,Sin_Table_Source		; Building the sine table
 ld hl,Sin_Table
 ld ix,Sin_Table+128
 ld b,64
BD_SinLoop:
 ld a,(de)
 inc de
 ld (hl),a
 inc hl
 ld (ix),a
 dec ix
 djnz BD_SinLoop
 ld (hl),a
 ld de,Sin_Table
 ld hl,Sin_Table+128
 ld b,128
BD_SinNeg:
 ld a,(de)
 inc de
 neg
 ld (hl),a
 inc hl
 djnz BD_SinNeg
 ld de,LeftFlipperSprites	; Calculating the sprites of the rightward flippers
 ld hl,RightFlipperSprites
 ld b,40
BD_RFLoop:
 push bc
 ld a,(de)
 ld c,a
 inc de
 ld a,(de)
 inc de
 push de
 ld b,8
BD_RFBits:
 rra
 rl d
 rr c
 rl e
 djnz BD_RFBits
 ld (hl),d
 inc hl
 ld (hl),e
 inc hl
 pop de
 pop bc
 djnz BD_RFLoop
 ret

#include "interact.inc"		; Interactive elements, event handling
#include "render.inc"		; Rendering routines
#include "physics.inc"		; Ball handling: gravity, collision, flippers
#include "menu.inc"			; Menu related and text display routines
#include "graph.inc"		; Graphics routine library
#include "input.inc"		; Keyboard routines
#include "math.inc"			; Integer arithmetic

; -----[ Data - non-executable part ]----------------------------------------------------------------------------------------

NonExecutable:

#include "miscdata.inc"		; Data needed by various libraries
#include "sprites.inc"		; Sprites used in the program except for table elements

MainMenu:
 .byte 3
 .word Game
 .word DisplayHighScores
 .word Exit
 .byte 4
 .byte %11111011,%11101111,%10111110,%11111000
 .byte %11000000,%11001100,%10110010,%00110000
 .byte %11111000,%11001111,%10111100,%00110000
 .byte %00011000,%11001100,%10110010,%00110000
 .byte %11111000,%11001100,%10110010,%00110000
 .byte 8
HighScoreLabel:
 .byte %11001011,%01111101,%10010000,%11111011,%11101111,%10111110,%11111011,%11100000
 .byte %11001011,%01100001,%10010000,%11000011,%00001100,%10110010,%11000011,%00000000
 .byte %11111011,%01101101,%11110000,%11111011,%00001100,%10111100,%11110011,%11100000
 .byte %11001011,%01100101,%10010000,%00011011,%00001100,%10110010,%11000000,%01100000
 .byte %11001011,%01111101,%10010000,%11111011,%11101111,%10110010,%11111011,%11100000
 .byte 3
 .byte %11111011,%00101101,%11110000
 .byte %11001011,%00101100,%01100000
 .byte %11001011,%00101100,%01100000
 .byte %11011011,%00101100,%01100000
 .byte %11111011,%11101100,%01100000

FlipperAngles:					; Surface angles for the left flipper frames
 .byte 17, 13, 1, -12, -16		; Right flipper angles: these*(-1)

EventLengths:					; The length in bytes of the parametres of each type of event
 .byte 4, 0, 2, 2, 1, 1, 2, 1, 2

PausedPicture:
 .byte %00000000,%00000000,%00000000,%00000000
 .byte %01111101,%11110110,%01011111,%01111100
 .byte %01100101,%10010110,%01011000,%01100000
 .byte %01111101,%11110110,%01011111,%01111000
 .byte %01100001,%10010110,%01000011,%01100000
 .byte %01100001,%10010111,%11011111,%01111100
 .byte %00000000,%00000000,%00000000,%00000000

LogoData:
 .byte $10, $10, $08, $04, $09, $10, $20, $11, $08, $20, $10, $82
 .byte $44, $44, $41, $10, $44, $41, $08, $44, $11, $08, $44, $20
 .byte $39, $69, $3C, $9D, $93, $27, $3D, $3A, $6C, $F2, $B1, $8A
 .byte $FF, $FF, $FF, $FF, $FF, $FF, $FF, $FF, $FF, $FF, $FF, $FF
 .byte $F0, $78, $1C, $08, $FF, $03, $81, $18, $E0, $60, $27, $03
 .byte $E0, $38, $98, $88, $FE, $23, $11, $18, $C0, $41, $26, $13
 .byte $E6, $AB, $F3, $FA, $FC, $FE, $B9, $98, $AC, $C6, $AC, $FF
 .byte $D7, $77, $F5, $FD, $FD, $7F, $75, $5D, $DD, $57, $75, $7F
 .byte $EA, $AB, $FA, $AA, $FE, $D6, $BB, $AA, $AE, $EA, $EE, $AB
 .byte $D1, $33, $F4, $49, $FC, $4B, $35, $95, $8D, $50, $35, $03
 .byte $C6, $23, $F1, $F8, $FC, $62, $31, $F8, $8C, $46, $27, $E3
 .byte $C6, $23, $F1, $F8, $FC, $62, $31, $F8, $8C, $46, $27, $E3
 .byte $C6, $23, $F1, $F8, $FC, $62, $31, $F8, $8C, $46, $27, $E3
 .byte $C6, $23, $F1, $F8, $FC, $62, $31, $F8, $8C, $46, $27, $E3
 .byte $C6, $23, $F1, $F8, $FC, $62, $31, $F8, $8C, $46, $27, $E3
 .byte $C6, $20, $10, $0C, $04, $02, $01, $01, $80, $40, $24, $03
 .byte $C6, $30, $18, $0E, $04, $06, $03, $03, $80, $C0, $64, $07
 .byte $FF, $FF, $FF, $FF, $FF, $FF, $FF, $FF, $FF, $FF, $FF, $FF
 .byte $39, $69, $3C, $9D, $93, $27, $3D, $3A, $6C, $F2, $B1, $8A
 .byte $44, $44, $41, $10, $44, $41, $08, $44, $11, $08, $44, $20
 .byte $10, $10, $08, $04, $09, $D4, $77, $77, $47, $77, $77, $4A
 .byte $00, $00, $00, $00, $01, $DC, $57, $27, $44, $66, $46, $4E
 .byte $00, $00, $00, $00, $01, $48, $75, $25, $45, $45, $54, $44
 .byte $00, $00, $00, $00, $01, $C8, $45, $25, $47, $75, $77, $74

LastByte:

.echo "Total executed code: "
.echo NonExecutable-Main
.echo "\n"
.echo "Total data: "
.echo $-NonExecutable
.echo "\n"

#ifdef TI82

#ifndef TI82S

.block MEMUSED

#endif

#endif

.end
END
