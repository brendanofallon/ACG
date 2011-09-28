#!/usr/bin/perl

#Prepends the license header file to all files with the .java suffix given as arguments

#Header to prepend
my $file = "/Users/brendano/workspace/ACG/licenseHeader.txt";

my $tmp = "license.tmp";

foreach my $argFile (@ARGV) {
	print "Doing file : $argFile \n";
	system("rm -f $tmp");
	system("touch $tmp");
	system("cat $file > $tmp ");
	system("cat $argFile >> $tmp");
	system("mv $tmp $argFile");
}

