Ball:
 .byte %01110000
 .byte %11111000
 .byte %11111000
 .byte %11111000
 .byte %01110000
 .byte %00000000
 .byte %00000000
 .byte %00000000

BallWhite:
 .byte %00000000
 .byte %01100000
 .byte %01000000
 .byte %00000000
 .byte %00000000
 .byte %00000000
 .byte %00000000
 .byte %00000000

Numbers:
 .byte %11110000
 .byte %10010000
 .byte %10110000
 .byte %10110000
 .byte %11110000

 .byte %00010000
 .byte %00010000
 .byte %00110000
 .byte %00110000
 .byte %00110000

 .byte %11110000
 .byte %00010000
 .byte %11110000
 .byte %11000000
 .byte %11110000

 .byte %11110000
 .byte %00010000
 .byte %11110000
 .byte %00110000
 .byte %11110000

 .byte %10010000
 .byte %10010000
 .byte %11110000
 .byte %00110000
 .byte %00110000

 .byte %11110000
 .byte %10000000
 .byte %11110000
 .byte %00110000
 .byte %11110000

 .byte %11110000
 .byte %10000000
 .byte %11110000
 .byte %11010000
 .byte %11110000

 .byte %11110000
 .byte %00010000
 .byte %00110000
 .byte %00110000
 .byte %00110000

 .byte %11110000
 .byte %10010000
 .byte %11110000
 .byte %10110000
 .byte %11110000

 .byte %11110000
 .byte %10010000
 .byte %11110000
 .byte %00110000
 .byte %11110000

LeftFlipperSprites:		; Left flipper frames
 .byte $00, $00			; Flipper frame 0 (down)
 .byte $00, $00
 .byte $E0, $00
 .byte $9C, $00
 .byte $87, $00
 .byte $63, $C0
 .byte $1F, $C0
 .byte $01, $80
 .byte $00, $00			; Flipper frame 1
 .byte $00, $00
 .byte $F0, $00
 .byte $8E, $00
 .byte $87, $C0
 .byte $7B, $C0
 .byte $07, $80
 .byte $00, $00
 .byte $00, $00			; Flipper frame 2
 .byte $00, $00
 .byte $FF, $E0
 .byte $83, $E0
 .byte $87, $E0
 .byte $78, $00
 .byte $00, $00
 .byte $00, $00
 .byte $00, $00			; Flipper frame 3
 .byte $07, $80
 .byte $FB, $C0
 .byte $87, $C0
 .byte $9E, $00
 .byte $60, $00
 .byte $00, $00
 .byte $00, $00
 .byte $01, $80			; Flipper frame 4 (up)
 .byte $1F, $C0
 .byte $E3, $C0
 .byte $87, $00
 .byte $9C, $00
 .byte $60, $00
 .byte $00, $00
 .byte $00, $00

Spring:
 .byte %11111111
 .byte %10001111
 .byte %01011101
 .byte %01011101
 .byte %01011101
 .byte %01011101
 .byte %11011111

.echo "Sprites: "
.echo $-Ball
.echo "\n"
