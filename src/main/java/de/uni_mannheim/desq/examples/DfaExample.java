package de.uni_mannheim.desq.examples;

import com.google.common.base.Stopwatch;
import de.uni_mannheim.desq.dictionary.Dictionary;
import de.uni_mannheim.desq.fst.Dfa;
import de.uni_mannheim.desq.fst.Fst;
import de.uni_mannheim.desq.patex.PatEx;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author kbeedkar {kbeedkar@uni-mannheim.de}.
 */
public class DfaExample {

    Stopwatch dfaTime = Stopwatch.createUnstarted();

    public void amzn() throws IOException {

        Dictionary dict = Dictionary.loadFrom("data-local/amzn-dict.avro.gz");

        long sigma = 0L;
        String patternExpression = "";

        // SLOW
        sigma = 100;
        patternExpression = "(Books) [.?{2} (Books)]{1,4}";

        // SLOW
        // sigma = 500;
        // patternExpression = "(Electronics^) [.?{2} (Electronics^)]{1,4}";

        // SLOW
        // sigma = 100;
        // patternExpression = "(Musical_Instruments^) [.?{2} (Musical_Instruments^)]{1,4}";

        // VERY SLOW
        // sigma = 100;
        // patternExpression = "Digital_Cameras@Electronics [.?{3} (.^)]{1,4}";

        PatEx p = new PatEx(patternExpression, dict);
        Fst fst = p.translate();
        fst.minimize();

        fst.annotate();

        dfaTime.start();
        Dfa.createReverseDfa(fst, dict, dict.lastFidAbove(sigma));
        dfaTime.stop();
        System.out.println("Reverse Dfa for " + patternExpression + " took " + dfaTime.elapsed(TimeUnit.SECONDS) + "s");


        dfaTime.reset();
        dfaTime.start();
        Dfa.createDfa(fst, dict, dict.lastFidAbove(sigma));
        dfaTime.stop();
        System.out.println("Dfa for " + patternExpression + " took " + dfaTime.elapsed(TimeUnit.SECONDS) + "s");
    }

    public static void main(String[] args) throws IOException {
        new DfaExample().amzn();
    }
}