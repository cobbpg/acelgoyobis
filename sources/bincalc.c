// Binary to calcfile converter by Patai Gergely
//
// Note that for 82 shells you have to add the header bytes manually before conversion:
// CrASH: $D5 $00 $11
// SNG: $53 $4E $47 ("SNG")
//
// Also, if the on-calc name starts with a dash, it will be converted to a prefix byte (inverse a)
//
// Naturally you do not need to do this for data files, as they are not bound to specific shells.

#include<stdio.h>

#define TYPE_ANY	0
#define TYPE_82P	1
#define TYPE_83P	2
#define TYPE_8XP	3

int infer_type(char *name) {
	int t = TYPE_ANY;

	if (strstr(name, ".82p")) t = TYPE_82P;
	if (strstr(name, ".83p")) t = TYPE_83P;
	if (strstr(name, ".8xp")) t = TYPE_8XP;

	return t;
}

void create_calcfile(FILE *fin, int type, char *outname, char *calcname) {
	FILE *fout;
	int size, ofs, chksum;
	unsigned char *buf;
	char name[9];
	int i;

	// Some notification
	strncpy(name, calcname, 8);
	name[8] = 0;
	if (name[0] == '-') name[0] = 220;	// Helping CrASH conversion
	printf("Creating %s (%s)\n", outname, name);

	// Determining the size of the buffer
	fseek(fin, 0, SEEK_END);
	size = ftell(fin);
	rewind(fin);
	switch(type) {
		case TYPE_82P: ofs = 72; break;
		case TYPE_83P: ofs = 72; break;
		case TYPE_8XP: ofs = 74; break;
	}

	// Allocating the buffer
	if ((buf = (char*)malloc(size + ofs + 2)) == NULL) {
		printf("Not enough memory.\n");
		exit(1);
	}
	memset(buf, 0, size + ofs + 2);

	// Reading the raw data
	fread(buf + ofs, 1, size, fin);

	// Preparing the output
	fout = fopen(outname, "wb");

	// 82, 83, 83+
	sprintf(buf, "**TI83**\x1a\x0a");
	if (type == TYPE_82P) buf[5] = '2';
	if (type == TYPE_8XP) buf[6] = 'F';
	sprintf(buf + 11, "File created with bincalc");
	buf[53] = (size + ofs - 55) & 0xff;
	buf[54] = (size + ofs - 55) >> 8;
	buf[55] = ofs - 61;	// Header length
	buf[57] = (size + 2) & 0xff;
	buf[58] = (size + 2) >> 8;
	buf[59] = 6;	// Protected program
	strcpy(buf + 60, name);	// Name on calculator
	buf[68] = 1;	// Flag for the 83+ (overwritten by others)
	buf[ofs - 4] = (size + 2) & 0xff;
	buf[ofs - 3] = (size + 2) >> 8;
	buf[ofs - 2] = size & 0xff;
	buf[ofs - 1] = size >> 8;

	// Checksum (from the header length to the end of the data)
	chksum = 0;
	for(i = 55; i < size + ofs; i++) chksum += buf[i];
	buf[size + ofs] = chksum & 0xff;
	buf[size + ofs + 1] = chksum >> 8;

	// Getting rid of the buffer
	fwrite(buf, 1, size + ofs + 2, fout);
	fclose(fout);
}

int main(int argc, char *argv[]) {
	FILE *fp;
	char cname[256];

	if (argc < 4) {	// Too few arguments
		printf("Usage: bincalc <input-file> <output-file> <calc-file>\n");
		printf("<input-file> - Raw binary file.\n");
		printf("<output-file> - Full filename (the type is inferred from the extension).\n");
		printf("                If you do not provide a known extension, all types of files\n");
		printf("                will be created by adding each ending (82p, 83p, 8xp).\n");
		printf("<calc-file> - The name of the file on the calculator.\n");
		exit(1);
	}

	if ((fp = fopen(argv[1], "rb")) == NULL) {
		printf("Input file could not be opened.\n");
		exit(1);
	}

	// Creating output file(s) of the appropriate type
	switch(infer_type(argv[2])) {
		case TYPE_ANY:
			sprintf(cname, "%s.82p", argv[2]);
			create_calcfile(fp, TYPE_82P, cname, argv[3]);
			sprintf(cname, "%s.83p", argv[2]);
			create_calcfile(fp, TYPE_83P, cname, argv[3]);
			sprintf(cname, "%s.8xp", argv[2]);
			create_calcfile(fp, TYPE_8XP, cname, argv[3]);
		break;
		case TYPE_82P:
		case TYPE_83P:
		case TYPE_8XP:
			create_calcfile(fp, infer_type(argv[2]), argv[2], argv[3]); break;
		break;
	}

	// Successful termination
	fclose(fp);
	printf("Done.\n");

	return 0;
}

