#!/usr/bin/perl 

# Brendan O'Fallon
# University of Washington
# July 2010
#
# A script that uses phylip (actually, dnamlk) to compute the 'data likelihood' of a data set on 
# a tree, and compares it to a user-specified likelihood. 
#
# Takes two arguments. First, a phylip-formatted file that contains the sequences to 
# compute the liklihood on (for?).  Second, a file that contains a list of likelihoods and
# trees, where each line contains the likelihood, one or more spaces, and then the whole tree.
# Example :
# -13.472 ((a:1, b:2), (c:3, d:4));
# -16.234 ((a:2, c:2), (c:3, d:4));
# -1687   ((d:2, c:2), (a:3, d:4));
# ... etc
#
#  This also supports the addition of a comment string in the trees file that is printed 
# out if a tree is found whose given likelihood does not match the phylip-calculated likelihood
# This could be useful if you're performing a number of different arrangements, and you'd like
# to know which one it is that (occasionally) produces incorrect likelihoods. 
#
# An important note or two: This uses a couple of temporary files. '.phyinput.txt' is used
# to store the commands to phylip. 'intree' and 'outree' are used by phylip itself. 
#
#

use strict;
use Bio::Perl;
use Bio::AlignIO;

#my $path_to_phylip = "/home/brendan/phylip-3.69/exe/dnamlk";
my $path_to_phylip = "/Users/brendano/phylip-3.69/exe/dnamlk";

my $seqfile = $ARGV[0];
my $logfile = $ARGV[1];

system("cp $seqfile infile");



#Store all sequences in a list so we can write subsequences to a file easily
my @seqs;
my $count = 0;
my $inseq = Bio::SeqIO->new(-file => "<$seqfile", -format => 'fasta', -verbose => -1 );
$inseq->verbose(-1);
while (my $seq = $inseq->next_seq) {
	push(@seqs, $seq);
} 

my $prevlength = 0;

open loghandle, $logfile or die "Could not open log file $logfile \n";

#Input to phylip. First number is the transition to transversion ratio
# Set of four numbers is are the frequencies of the bases. 
#open tmpfile, ">.phyinput.txt";
#print tmpfile "R
#U
#L
#T
#1.0
#F
#0.2 0.1 0.25 0.45
#Y
#R";
#close tmpfile;

my $linenum = 0;

my $count = 0;
my $shortBranchErrors = 0;

for my $line (<loghandle>) {
	$linenum++;
	my $treestart = index($line, "(");
	my $treeend = rindex($line, ")");
	my $tree = substr($line, $treestart, $treeend - $treestart +2);
	
	$line =~ /^([\-\.\d]+)\s/;
	my $dl = $1;
	
	my $modstart = index($line, "[");
	my $modend = index($line, "]");
	my $modstr = "";
	if ($modstart>0 && $modend>0) {
		$modstr = substr($line, $modstart, $modend-$modstart+2);
	}

	$line =~ /kappa=([\d.\d]+)/;
	my $kappa = $1;
	
	$line =~ / a=([\d.\d]+)/;
	my $AFreq = $1;
	
	$line =~ / c=([\d.\d]+)/;
	my $CFreq = $1;
	
	$line =~ / g=([\d.\d]+)/;
	my $GFreq = $1;
	
	$line =~ / t=([\d.\d]+)/;
	my $TFreq = $1;
		
	$line =~ /rates=([\d]+)/;
	my $cats = $1;


	my $probs = "";
	my $rates = "";
	#print "Found $cats total categories \n";
	for my $cat (0..($cats-1)) {
		$line =~ /rate$cat=([\d.\d]+)/;
		my $rate = $1;
		$rates = $rates . " " . $rate;
		
		$line =~ /prob$cat=([\d.\d]+)/;
		my $prob = $1;
		$probs = $probs . " " . $prob;
		
		#print "Cat: $cat  rate: $rate prob: $prob \n";
	}
	
	

	
open tmpfile, ">.phyinput.txt";
print tmpfile "R
U
L
T
$kappa
F
$AFreq $CFreq $GFreq $TFreq
R
R
R
Y
$cats
$rates
$probs
R
";

close tmpfile;


	#print "Got DL: $dl \n Got tree: $tree \n";

	open treehandle, ">intree" or die "Could not open tree file handle";
	print treehandle $tree . "\n";
	close treehandle;

    # ------- Write only those sites in the appropriate range to 'infile', which phylip reads -------#
    
    $line =~ /start=([\d]+)/;
	my $startsite = $1;
	$line =~ /end=([\d]+)/;
	my $endsite = $1;
	if ($endsite > $startsite) {
		my @rangedSeqs;
		#No way to write sequences directly as phylip, so write as fasta and the convert to to phylip
		open tmpfas, ">.tmp.fas" or die "Could not open temporary fasta file";
		for my $seq (@seqs) {
			my $truncSeq = substr($seq->seq, $startsite, $endsite - $startsite);
			my $newSeq = Bio::Seq->new( -seq=>$truncSeq, -display_id=>$seq->display_id, -verbose => -1);
			print tmpfas ">" . $seq->display_id . "\n" . $truncSeq . "\n";
		}
		close tmpfas;
	
		#uggh...now read in fasta alignment we just wrote, and write it as phylip
		my $in  = Bio::AlignIO->new(-file   => "<.tmp.fas" ,
								 -format => 'fasta', -verbose => -1);
		my $out = Bio::AlignIO->new(-file   => ">infile" ,
								 -format => 'phylip', -verbose => -1);
		while ( my $aln = $in->next_aln() ) {
			$out->write_aln($aln);
		}
	  
		#Now actually execute phylip
		system("$path_to_phylip < .phyinput.txt > .phycrap.txt ");
	
		open outfilehandle, "outfile" or die "Could not open phylip output file \n";
		my $phylipDL = "X";
		for my $outlines (<outfilehandle>) {
		 if ($outlines =~ /Likelihood =[\s]+([\-\.\d]+)/) {
	 		$phylipDL = $1;
		 }
		}

	
		for my $b (0..$prevlength) {
			print "\b";
		}
	
		if ($linenum %10 == 0) {
			print $linenum;
		}
	
		$prevlength = length( $linenum );
	
		if ($phylipDL eq "X" && $startsite > $endsite) {
			print "Could not parse DL from phylip output file...\n";
			print "Line is: \n $line";
			#exit(1);
		}
		else {
			my $dif =  ($dl - $phylipDL);
			if ($dif < 0) {
			 $dif = -1 * $dif;
			}
		
			if ($dif > 0.001) {
				print "\n Tree : \n $tree \n";
				print "Proposed dl was : $dl \n";
				print "Got phylipDL of : $phylipDL \n";
				print "Modifier string was $modstr \n";
				#print "Kappa : $kappa \n";
				$line =~ /#accept=([\d]+), #proposed=([\d]+)/;
				my $accepted = $1;
				my $proposed = $2;
				print "Likelihoods do not match on line $linenum ! dif = $dif\n";
				
				print "Category rates : $rates \n";
				
				if ($tree =~ /E-7/ || $tree =~ /E-8/) {
					print " ** Tree contains at least one short branch, ignoring **\n";
					$shortBranchErrors++;
				}
				else {			
					exit(0);
				}
				$count++;
			}
		}
	}
}

 if ($count==0) {
	 print "\n All likelihoods appear to be valid. \n";
  }
  else {
  	print "Found errors on $count lines\n";
  	print "Errors ostensibly due to very short branches: $shortBranchErrors \n";
  }

