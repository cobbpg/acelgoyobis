Interactions:			; Inducing gizmo event scripts upon collision
 ld a,(BallX+1)			; Fetching the ball's coordinates
 ld ixh,a
 ld a,(BallY+1)
 ld ixl,a
 ld hl,(GizmoList)
 ld b,(hl)
 inc hl
IA_Loop:
 push bc
 ld e,(hl)				; Pointer to the next gizmo
 inc hl
 ld d,(hl)
 inc hl
 push hl
 ld a,(de)				; Gizmo flags
 ld c,a
 and 4
 jr z,IA_Skip			; The element is inactive
 inc de
 ld a,(de)
 sub 3
 cp ixh
 jp p,IA_Skip			; Ball is far in the left
 inc de
 ld a,(de)
 sub 3
 cp ixl
 jp p,IA_Skip			; Ball is far above
 inc de
 ld a,(de)
 add a,2
 cp ixh
 jr c,IA_Skip			; Ball is far in the right
 inc de
 ld a,(de)
 add a,2
 cp ixl
 jr c,IA_Skip			; Ball is far below
 ld hl,1
 rr c
 jr nc,IA_NoSprite
 ld l,9
IA_NoSprite:
 add hl,de				; HL now holds the address to the event script
 call EventHandler
IA_Skip:
 pop hl
 pop bc
 djnz IA_Loop
 ret

InitElements:			; Drawing the elements onto the table
 ld hl,(TableData)		; Sprite address hack
 ld (Sprite_Address+1),hl
 ld hl,(GizmoList)
 ld b,(hl)				; Number of elements of a given type
 inc hl
IE_Elements:
 push bc
 ld e,(hl)				; The pointer to the current element
 inc hl
 ld d,(hl)
 inc hl
 push hl
 ld a,(de)
 rra
 jr nc,IE_Skip			; There is no sprite data for this element
 rra
 ld hl,WhiteSprite
 jr nc,IE_Colour		; The element is currently not visible
 ld hl,Sprite
IE_Colour:
 ld (IE_Hack),hl
 inc de					; Drawing the sprite
 ld a,(de)
 inc de
 ld h,a
 ld a,(de)
 inc de
 inc de
 inc de
 ld l,a
 ld a,h
 ld b,d
 ld c,e
IE_Hack = $+1
 call Sprite
IE_Skip:
 pop hl
 pop bc
 djnz IE_Elements
 ld hl,VScreen
 ld (Sprite_Address+1),hl
 ret

DrawGizmo:				; Explicitely drawing a gizmo specified in E
 ld hl,(TableData)		; Sprite address hack
 ld (Sprite_Address+1),hl
 ld d,0
 ld hl,(GizmoList)
 inc hl
 add hl,de
 add hl,de
 ld e,(hl)
 inc hl
 ld d,(hl)
 ld a,(de)
 rra
 jr nc,DG_Exit			; There is no sprite data for this element
 ld hl,Sprite
 rra
 jr c,DG_Visible		; The element is currently visible (black)
 ld hl,WhiteSprite
DG_Visible:
 ld (DG_Address+1),hl
 inc de					; Drawing the sprite
 ld a,(de)
 inc de
 ld h,a
 ld a,(de)
 inc de
 inc de
 inc de
 ld l,a
 ld a,h
 ld b,d
 ld c,e
DG_Address:
 call Sprite
DG_Exit:
 ld hl,VScreen
 ld (Sprite_Address+1),hl
 ret

HandleTimers:			; Taking care of the timers (automatic counters)
 ld hl,(TimerList)
 ld a,(hl)				; Exit if no timers on the table (unlikely though)
 or a
 ret z
 ld b,a
 inc hl
HT_Loop:
 push bc
 ld e,(hl)				; Selecting a timer
 inc hl
 ld d,(hl)
 inc hl
 push hl
 ld a,(de)
 or a
 jr z,HT_Skip			; The timer is inactive
 dec a					; Decreasing timer
 ld (de),a
 or a
 jr nz,HT_Skip			; Nothing to do yet
 ex de,hl
 inc hl
 call EventHandler		; Handling the events
HT_Skip:
 pop hl
 pop bc
 djnz HT_Loop
 ret

EventHandler:			; Runs the event script at HL
 ld a,(hl)
 or a
 ret z					; Event script ended
 inc hl
 dec a					; There are so few events that a jump table is unnecessary
 jr z,Event_Score		; Score S B
 dec a
 jr z,Event_Bonus		; Bonus
 dec a
 jr z,Event_SetFlags	; SetFlags Gizmo Flags
 dec a
 jr z,Event_ResetFlags	; ResetFlags Gizmo Flags
 dec a
 jr z,Event_Activate	; Activate Gizmo
 dec a
 jr z,Event_Hide		; Hide Gizmo
 dec a
 jr z,Event_SetCounter	; SetCounter Counter N
 dec a
 jp z,Event_Decrease	; Decrease Counter
 dec a
 jp z,Event_SetTimer	; SetTimer Timer N

; If we arrive here, that's an error... It should never happen!

 jp Exit				; Returning to the OS

Event_Score:
 ld c,(hl)				; BC: score increase
 inc hl
 ld b,(hl)
 inc hl
 ld e,(hl)				; DE: bonus increase
 inc hl
 ld d,(hl)
 inc hl
 push hl
 ld hl,(Score)
 add hl,bc
 ld (Score),hl
 jr nc,E_S_OK1
 ld hl,(Score+2)
 inc hl
 ld (Score+2),hl
E_S_OK1:
 ld hl,(BonusVal)
 add hl,de
 ld (BonusVal),hl
 jr nc,E_S_OK2
 ld hl,(BonusVal+2)
 inc hl
 ld (BonusVal+2),hl
E_S_OK2:
 pop hl
 jr EventHandler

Event_Bonus:
 ld a,(BonusMul)		; Increasing the bonus multiplier
 inc a
 cp 100
 jr z,EventHandler		; The maximum is 99
 ld (BonusMul),a
 jr EventHandler

Event_SetFlags:			; SetFlags Gizmo Flags
 ld e,(hl)
 inc hl
 ld c,(hl)
EvtSF:
 ld a,$b1				; OR C
EvtSF_Cont:
 inc hl
 ld (EvtSF_Op),a
 push hl
 ld d,0
 ld hl,(GizmoList)
 inc hl
 add hl,de
 add hl,de
 ld a,(hl)
 inc hl
 ld h,(hl)
 ld l,a
 ld a,(hl)
EvtSF_Op:
 nop					; OR C for SetFlags, AND C for ResetFlags
 ld (hl),a
 call DrawGizmo			; Updating the table
 pop hl
 jr EventHandler

Event_ResetFlags:		; ResetFlags Gizmo Flags
 ld e,(hl)
 inc hl
 ld c,(hl)
EvtRF:
 ld a,$a1				; AND C
 jr EvtSF_Cont

Event_Activate:			; Activate Gizmo = SetFlags Gizmo 6
 ld e,(hl)
 ld c,6
 jr EvtSF

Event_Hide:				; Hide Gizmo = ResetFlags Gizmo 249
 ld e,(hl)
 ld c,249
 jr EvtRF

Event_SetCounter:		; SetCounter Counter N
 ld ix,(CounterList)
 inc ix
EvtSC:
 ld e,(hl)				; Counter/Timer ID
 inc hl
 ld c,(hl)				; New value
 inc hl
 push hl
 ld d,0
 push ix
 pop hl
 add hl,de
 add hl,de
 ld a,(hl)
 inc hl
 ld h,(hl)
 ld l,a
 ld (hl),c
 pop hl
 jp EventHandler

Event_Decrease:			; Decrease Counter
 ld e,(hl)
 inc hl
 push hl
 ld d,0
 ld hl,(CounterList)
 inc hl
 add hl,de
 add hl,de
 ld a,(hl)
 inc hl
 ld h,(hl)
 ld l,a
 dec (hl)
 jr nz,EvtDec_OK		; Nothing to do yet
 inc hl
 call EventHandler		; Running the event script of the counter
EvtDec_OK:
 pop hl
 jp EventHandler

Event_SetTimer:			; SetTimer Timer N
 ld ix,(TimerList)
 inc ix
 jr EvtSC

.echo "Interaction engine: "
.echo $-Interactions
.echo "\n"
