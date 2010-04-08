Acelgoyobis

by Patai Gergely

patai_gergely@fastmail.fm
http://www.hszk.bme.hu/~pg429/


Introduction

For some reason I suspect that you expect me to explain this weird title. It is
quite simple: adding a letter and two acute accents gives you the word
'ac�lgoly�bis', which means 'little steel ball' in Hungarian. A fitting title,
surprisingly.

The little steel ball plays the principal role in this game. However, I don't
think I need to introduce the concept of pinball to anyone. Instead, let's have
a look at the peculiarities of this very implementation.


Requirements

There are six executables in the distribution. They run on the following
model-shell combinations:

PINBALL.82p  - TI-82 CrASH
PINBALLx.82p - TI-82 CrASH 19.006
PINBALLs.82p - TI-82 SNG
PINBALL.83p  - TI-83 ION
PINBALLv.83p - TI-83 Venus
PINBALL.8xp  - TI-83+ ION and compatible

For TI-82 users I strongly recommend the SNG version, because it is smaller than
CrASH executables and does not have issues with phantom tables (see below). The
TI-83+ ION version should be able to run on any upper model (SE, 84+/SE). I
don't know how it behaves under MirageOS, but I see no reason for any problem.

You need to have at least one table file present and five kilobytes of free RAM
available, otherwise the program will immediately exit. The CrASH versions don't
need the extra RAM, because they occupy it permanently. The program will find
only the tables in the RAM, so you must unarchive them if you have an 83+ or
compatible model.


Controls

Use the up and down arrows to navigate in the menu, 2nd to select an item. The
items are the following:

START - start playing; just select a table from the list and press 2nd again
HIGH SCORES - highest scores per table; pressing 2nd returns to the menu
QUIT - exit the program

The following keys are available during the game:

When the ball is at rest on the spring, you can pull the string with the right
arrow. The longer you press it the faster the ball will be launched. No other
key works in this position.

When the ball is already moving, the controls are as follows:

Y=    - raise the left flippers
Graph - raise the right flippers
Zoom  - pause and unpause
2nd   - exit to the main menu
Enter - tilt (random force on the ball)

The flippers remain in raised position until you release the corresponding keys,
so you can even hold the ball with them when needed. As for the tilt function,
use it only when the ball seems to have stuck somewhere. If you press Enter
twice in a row too fast, you will be penalised for cheating and lose the current
ball. As a rule of thumb, count slowly from one to ten between subsequent tilts.

When you lose a ball in any way the bonus is added with the current multiplier.
The calculation is displayed on the screen. To advance, just press 2nd and you
are on the spring again.


Tables

The tables come in separate files. Each table holds its highest score, so if you
want to keep your scores, you need to make a backup before deleting them and use
the backup to 'reinstall' them. The factory tables are described in tables.txt.
You can also build your own tables with the editor supplied. Please read the
design notes (design.txt), understanding them is absolutely necessary to be able
to create playable and flawlessly working tables.


Known issues

- physics artifacts: the ball can sometimes get stuck or fall through walls when
  approaching in odd angles. These are not bugs but come from the limitations of
  the engine, so the only way to resolve them would be a complete rewrite. You
  can prevent these phenomena from appearing with clever table design.

- phantom tables: this is a TI-82 CrASH specific issue. Due to the way the 82
  handles memory each table can appear multiple times in the list. Playing these
  phantom tables is safe, but you can never be sure which one belongs to the
  real file, so there is no guarantee you can retain your high scores. The SNG
  version is not affected, and it is a lot smaller, so I recommend using that
  one instead.


Supporters

- pacHa - beta testing the 82 versions and giving ideas and opinion; without his
          support you wouldn't be playing this game
- Guillaume Hoffmann - another guy who devoted much of his time to beta testing
                       and gave some ideas; also indispensable :)
- Maarten Zwartbol - the creator of the Speed table
- Julien Leguen - additional testing
- Hans T�rnqvist - moral support in the hard times :)
- Benjamin Moody - fixing the crashes happening when free memory is scarce
- others who gave feedback and kept praising me ;)


History

2004/11/08 - Low RAM bug fixed
2004/11/04 - Original release
2003/07/15 - Development started


Last words

I hope you will enjoy playing this game. After all, that's why I made it.
