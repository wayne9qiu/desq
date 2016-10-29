package de.uni_mannheim.desq.examples;

import com.google.common.base.Stopwatch;
import de.uni_mannheim.desq.dictionary.Dictionary;
import de.uni_mannheim.desq.io.CountPatternWriter;
import de.uni_mannheim.desq.io.DelSequenceReader;
import de.uni_mannheim.desq.io.SequenceReader;
import de.uni_mannheim.desq.mining.DesqDfs;
import de.uni_mannheim.desq.mining.DesqMiner;
import de.uni_mannheim.desq.mining.DesqMinerContext;
import de.uni_mannheim.desq.mining.Sequence;
import de.uni_mannheim.desq.util.DesqProperties;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import scala.Tuple2;

import java.util.ArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DesqDfsLocalRun {

	static boolean caseSet = false;
	static long sigma;
	static String patternExp;
	static File dataFile;
	static Dictionary dict;

    public static void runPartitionConstruction() throws IOException {

		setCase("N5");


		DesqProperties minerConf = DesqDfs.createConf(patternExp, sigma);
		SequenceReader dataReader = new DelSequenceReader(new FileInputStream(dataFile), true);
		dataReader.setDictionary(dict);



		// experiment
		minerConf.setProperty("desq.mining.skip.non.pivot.transitions", false);
		minerConf.setProperty("desq.mining.use.minmax.pivot", false);




		// create context
		DesqMinerContext ctx = new DesqMinerContext();
		minerConf.setProperty("desq.mining.prune.irrelevant.inputs", false);
		minerConf.setProperty("desq.mining.use.two.pass", false);
		ctx.dict = dataReader.getDictionary();
		CountPatternWriter result = new CountPatternWriter();
		ctx.patternWriter = result;

		ctx.conf = minerConf;

		ctx.conf.prettyPrint();

		// perform the mining
		System.out.print("Creating miner... ");
		Stopwatch prepTime = Stopwatch.createStarted();
		DesqDfs miner = (DesqDfs) DesqDfs.create(ctx);
		prepTime.stop();
		System.out.println(prepTime.elapsed(TimeUnit.MILLISECONDS) + "ms");

		System.out.print("Reading input sequences into memory... ");
		Stopwatch ioTime = Stopwatch.createStarted();
		ObjectArrayList<Sequence> inputSequences = new ObjectArrayList<Sequence>();
		Sequence inputSequence = new Sequence();
		while (dataReader.readAsFids(inputSequence)) {
			inputSequences.add(inputSequence);
			inputSequence = new Sequence();
		}
		ioTime.stop();
		System.out.println(ioTime.elapsed(TimeUnit.MILLISECONDS) + "ms");


		System.out.print("Determining pivot items... ");
		Stopwatch miningTime = Stopwatch.createStarted();
		Tuple2<Integer, Integer> stats = miner.determinePivotElementsForSequences(inputSequences);
		miningTime.stop();
		System.out.println(miningTime.elapsed(TimeUnit.MILLISECONDS) + "ms");


		System.out.println("Total time: " +
				(prepTime.elapsed(TimeUnit.MILLISECONDS) + ioTime.elapsed(TimeUnit.MILLISECONDS) +  miningTime.elapsed(TimeUnit.MILLISECONDS)
						) + "ms");


		// print results
		System.out.println("Number of sequences: " + stats._1);
		System.out.println("Total frequency of all patterns: " + stats._2);

		// combined print
		System.out.println("create time, read time, process time, no. seq, no. piv, total Recursions, trs used, mxp used");
		System.out.println(prepTime.elapsed(TimeUnit.MILLISECONDS) + "\t" + ioTime.elapsed(TimeUnit.MILLISECONDS) + "\t" + miningTime.elapsed(TimeUnit.MILLISECONDS) + "\t" +
				stats._1 + "\t" + stats._2 + "\t" + miner.counterTotalRecursions + "\t" + miner.counterNonPivotTransitionsSkipped + "\t" + miner.counterMaxPivotUsed);
	}


	private static void setCase(String useCase) throws IOException {
		 switch (useCase) {
			 case "N5":
				 patternExp = "([.^ . .]|[. .^ .]|[. . .^])";
				 sigma = 1000;
				 String dataDir = "/home/alex/Data/nyt/";
				 dict = Dictionary.loadFrom(dataDir + "nyt-dict.avro.gz");
				 dataFile  = new File(dataDir + "nyt-data.del");
				 break;
			 case "A1":
				 patternExp = "(Electronics^)[.{0,2}(Electronics^)]{1,4}";
				 sigma = 500;
				 setAmznData();
				 break;
			 case "A2":
				 patternExp = "(Books)[.{0,2}(Books)]{1,4}";
				 sigma = 100;
				 setAmznData();
				 break;
			 case "A3":
				 patternExp = "Digital_Cameras@Electronics[.{0,3}(.^)]{1,4}";
				 sigma = 100;
				 setAmznData();
				 break;
			 case "A4":
				 patternExp = "(Musical_Instruments^)[.{0,2}(Musical_Instruments^)]{1,4}";
				 sigma = 100;
				 setAmznData();
				 break;
		 }
	}

	private static void setAmznData() throws IOException {
		String dataDir = "/home/alex/Data/amzn/";
		dict = Dictionary.loadFrom(dataDir + "amzn-dict.avro.gz");
		dataFile = new File(dataDir + "amzn-data.del");
	}

	public static void runMining() throws IOException {

		// Run N5
		//N5 String patternExp = "([.^ . .]|[. .^ .]|[. . .^])";

        // A1
        long sigma = 500;
        String patternExp = "(Electronics^)[.{0,2}(Electronics^)]{1,4}";


		DesqProperties minerConf = DesqDfs.createConf(patternExp, sigma);
		// conf.setProperty("desq.mining.prune.irrelevant.inputs", true);


		/*String dataDir = "/home/alex/Data/nyt/";
		Dictionary dict = Dictionary.loadFrom(dataDir + "nyt-dict.avro.gz");
		File dataFile = new File(dataDir + "nyt-data.del");
		SequenceReader dataReader = new DelSequenceReader(new FileInputStream(dataFile), true);
		dataReader.setDictionary(dict);*/

        String dataDir = "/home/alex/Data/amzn/";
        Dictionary dict = Dictionary.loadFrom(dataDir + "amzn-dict.avro.gz");
        File dataFile = new File(dataDir + "amzn-data.del");
        SequenceReader dataReader = new DelSequenceReader(new FileInputStream(dataFile), true);
        dataReader.setDictionary(dict);

		// create context
		DesqMinerContext ctx = new DesqMinerContext();
		ctx.dict = dataReader.getDictionary();
		CountPatternWriter result = new CountPatternWriter();
		ctx.patternWriter = result;
		ctx.conf = minerConf;

		// perform the mining
		DesqMiner miner = ExampleUtils.runMiner(dataReader, ctx);

		// print results
		System.out.println("Number of patterns: " + result.getCount());
		System.out.println("Total frequency of all patterns: " + result.getTotalFrequency());


		//icdm16(args);
		//nyt();
		//netflixFlat();
        //netflixDeep();
	}



	public static void main(String[] args) throws IOException {
		runPartitionConstruction();
	}
}