@echo off
echo ----- Assembling %1 for the TI-82 CrASH
echo #define TI82C >temp.z80
if exist %1.asm type %1.asm >>temp.z80
tasm -80 -i -b temp.z80 %1.bin
if errorlevel 1 goto ERRORS
bincalc %1.bin %1.82p -%1
echo ----- Assembling %1 for the TI-82 CrASH 19.006
echo #define TI82C196 >temp.z80
if exist %1.asm type %1.asm >>temp.z80
tasm -80 -i -b temp.z80 %1.bin
if errorlevel 1 goto ERRORS
bincalc %1.bin %1x.82p -%1
echo ----- Assembling %1 for the TI-82 SNG
echo #define TI82S >temp.z80
if exist %1.asm type %1.asm >>temp.z80
tasm -80 -i -b temp.z80 %1.bin
if errorlevel 1 goto ERRORS
bincalc %1.bin %1s.82p %1
echo ----- Assembling %1 for the TI-83 ION
echo #define TI83 >temp.z80
if exist %1.asm type %1.asm >>temp.z80
tasm -80 -i -b temp.z80 %1.bin
if errorlevel 1 goto ERRORS
bincalc %1.bin %1.83p %1
echo ----- Assembling %1 for the TI-83 Venus
echo #define TI83V >temp.z80
if exist %1.asm type %1.asm >>temp.z80
tasm -80 -i -b temp.z80 %1.bin
if errorlevel 1 goto ERRORS
bincalc %1.bin %1v.83p %1
echo ----- Assembling %1 for the TI-83 Plus ION
echo #define TI83P >temp.z80
if exist %1.asm type %1.asm >>temp.z80
tasm -80 -i -b temp.z80 %1.bin
if errorlevel 1 goto ERRORS
bincalc %1.bin %1.8xp %1
echo ----- Success!
echo TI-82 CrASH version is %1.82p
echo TI-82 CrASH 19.006 version is %1X.82p
echo TI-82 SNG version is %1S.82p
echo TI-83 ION version is %1.83p
echo TI-83 Venus version is %1V.83p
echo TI-83 Plus ION version is %1.8xp
goto DONE
:ERRORS
echo ----- There were errors.
:DONE
del temp.z80 >nul
del temp.lst >nul
del %1.bin >nul