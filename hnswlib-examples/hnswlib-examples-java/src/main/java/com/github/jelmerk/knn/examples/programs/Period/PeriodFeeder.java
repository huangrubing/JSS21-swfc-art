package com.github.jelmerk.knn.examples.programs.Period;

import com.github.jelmerk.knn.DistanceFunctions;
import com.github.jelmerk.knn.examples.auxiliary.model.Point;
import com.github.jelmerk.knn.examples.fscs.art.FSCS_ART;
import com.github.jelmerk.knn.examples.hnsw.art.HNSW_ART;
import com.github.jelmerk.knn.examples.kdfc.art.KDFC_ART;
import com.github.jelmerk.knn.examples.programs.binomial.BinomialFeeder;
import com.github.jelmerk.knn.hnsw.HnswIndex;
import org.apache.commons.math.util.MathUtils;
import org.joda.time.Period;
import org.joda.time.Weeks;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class PeriodFeeder {
    private static final double[][] bd = {{-10000, 10000}, {-10000, 10000}, {-10000, 10000}, {-10000, 10000}};
    private static double theta = 0.000032;
    private static int d1, d2, d3, d4;


    public static double getFailureRate() {
        Random generator = new Random();
        double totalFMeasure = 0;
        int iterations = 3000;
        // int iterations = 100;

        for (int i = 0; i <= iterations; i++) {
            long fMeasure = 0;
            while (true) {
                Point tc = Point.generateRandP(bd, generator);
                d1 = (int) tc.vector[0];
                d2 = (int) tc.vector[1];
                d3 = (int) tc.vector[2];
                d4 = (int) tc.vector[3];
                fMeasure++;

                Weeks original_output = new Period(d1, d2, d3, d4).toStandardWeeks();
                Weeks mutated_output = new Period(d1, d2, d3, d4).toStandardWeeks_m();
//                System.out.println(Arrays.toString(tc.coordPoint) + "\t" + original_output + "\t" + mutated_output);
                boolean revealFailure = !original_output.toString().equals(mutated_output.toString());
                if (revealFailure) {
                    System.out.print(fMeasure + "\t");
                    totalFMeasure += fMeasure;
                    break;
                }
            }
        }
        System.out.println(1 / (totalFMeasure / iterations));
        return 1 / (totalFMeasure / iterations);
    }

    private Random getRandomGenerator(long mySeed) {
        Random generator;
        if (mySeed == 0) {
            generator = new Random();
        } else {
            generator = new Random(mySeed);
        }
        return generator;
    }


    private long[] randomTesting(long mySeed, BufferedWriter writer) throws IOException {
        Random generator = getRandomGenerator(mySeed);
        long fMeasure = 0;
        long tcGenTime = 0;
        long execTime = 0;
        while (true) {
            long start = System.nanoTime();
            // double tc = min[0] + (max[0] - min[0]) * generator.nextDouble();
            Point tc = Point.generateValidRandP(bd, generator);
            tcGenTime = tcGenTime + (System.nanoTime() - start);

            d1 = (int) tc.vector[0];
            d2 = (int) tc.vector[1];
            d3 = (int) tc.vector[2];
            d4 = (int) tc.vector[3];

            fMeasure++;
            start = System.nanoTime();
            Weeks original_output = new Period(d1, d2, d3, d4).toStandardWeeks();
            Weeks mutated_output = new Period(d1, d2, d3, d4).toStandardWeeks_m();
            execTime = execTime + (System.nanoTime() - start);
            boolean revealFailure = !original_output.toString().equals(mutated_output.toString());

            if (revealFailure) {
                writer.write(fMeasure + "\t" + tcGenTime + "\t" + execTime + "\n");
                return new long[]{fMeasure, tcGenTime, execTime};
            }
        }
    }

    public long[] fscsTesting(long mySeed, BufferedWriter writer) throws IOException {
        Random generator = getRandomGenerator(mySeed);
        long fMeasure = 0;
        long tcGenTime = 0;
        long execTime = 0;

        FSCS_ART fscs = new FSCS_ART(10);
        int maxTry = (int) (30 / theta);
        int selected;
        Point[] tcP = new Point[maxTry + 2];
        Point[] candP = new Point[fscs.candNum];

        tcP[0] = Point.generateValidRandP(bd, generator);  // first test case
        fMeasure++; // increasing f-measure without testing first test case

        while (true) {
            long start = System.nanoTime();
            for (int i = 0; i < fscs.candNum; i++) {
                candP[i] = Point.generateValidRandP(bd, generator);
            }
            selected = fscs.findFarestCandidate(tcP, (int) fMeasure, candP);
            tcGenTime = tcGenTime + (System.nanoTime() - start);

            tcP[(int) fMeasure] = candP[selected];
            d1 = (int) tcP[(int) (fMeasure - 1)].vector[0];
            d2 = (int) tcP[(int) (fMeasure - 1)].vector[1];
            d3 = (int) tcP[(int) (fMeasure - 1)].vector[2];
            d4 = (int) tcP[(int) (fMeasure - 1)].vector[3];

            fMeasure++;

            start = System.nanoTime();
            Weeks original_output = new Period(d1, d2, d3, d4).toStandardWeeks();
            Weeks mutated_output = new Period(d1, d2, d3, d4).toStandardWeeks_m();
            execTime = execTime + (System.nanoTime() - start);
            boolean revealFailure = !original_output.toString().equals(mutated_output.toString());
            if (revealFailure) {
                writer.write(fMeasure + "\t" + tcGenTime + "\t" + execTime + "\n");
                return new long[]{fMeasure, tcGenTime, execTime};
            }
        }
    }

    public long[] limBalKdfcTesting(long mySeed, BufferedWriter writer) throws IOException {
        Random generator = getRandomGenerator(mySeed);
        long tcGenTime = 0;
        long execTime = 0;
        long fMeasure = 0;

        // initialization phase
        int[] backNum = new int[100 * (int) (1 / theta)];
        backNum[0] = 1;
        backNum[1] = 1;
        double d = bd.length;
        for (int i = 2; i < backNum.length; i++) {
            backNum[i] = (int) Math.ceil(1 / 2.0 * Math.pow((d + 1 / d), 2) * (Math.log(i) / Math.log(2)));
        }
        Point p = Point.generateValidRandP(bd, generator);
        KDFC_ART kdfc = new KDFC_ART(bd);
        kdfc.insertPointByStrategy(p);
        Point finalCase;
        ArrayList<Point> canD;

        while (true) {
            long start = System.nanoTime();
            canD = new ArrayList<>();
            for (int i = 0; i < kdfc.candidateNum; i++) {
                canD.add(Point.generateValidRandP(kdfc.inputDomain, generator));
            }
            finalCase = canD.get(0);
            int back = backNum[kdfc.size];
            double distance = kdfc.getMinDisByBacktracking(finalCase, back);
            for (int c = 1; c < kdfc.candidateNum; c++) {
                d = kdfc.getMinDisByBacktracking(canD.get(c), back); // get minimum distance
                if (distance < d) { // get the candidate for smallest distance
                    distance = d;
                    finalCase = canD.get(c);
                }
            }
            d1 = (int) finalCase.vector[0];
            d2 = (int) finalCase.vector[1];
            d3 = (int) finalCase.vector[2];
            d4 = (int) finalCase.vector[3];

            kdfc.insertPointByStrategy(finalCase);
            tcGenTime = tcGenTime + (System.nanoTime() - start);


            start = System.nanoTime();
            Weeks original_output = new Period(d1, d2, d3, d4).toStandardWeeks();
            Weeks mutated_output = new Period(d1, d2, d3, d4).toStandardWeeks_m();
            execTime = execTime + (System.nanoTime() - start);
            boolean revealFailure = !original_output.toString().equals(mutated_output.toString());
            if (revealFailure) {
                fMeasure = kdfc.size;
                writer.write(fMeasure + "\t" + tcGenTime + "\t" + execTime + "\n");
                return new long[]{fMeasure, tcGenTime, execTime};
            }
        }
    }

    public long[] hnswTesting(int ef, long mySeed, BufferedWriter writer) throws InterruptedException, IOException {
        Random generator = getRandomGenerator(mySeed);
        HNSW_ART myHnsw = new HNSW_ART(10);
        int selected;
        Point[] candP = new Point[myHnsw.candNum]; // Candidate test Case set (candidate_set)
        ArrayList<Point> selected_set = new ArrayList<>();

        long fMeasure = 0;
        long tcGenTime = 0;
        long execTime = 0;

        // HNSW GRAPH INITIALIZATION
        int givenM = bd.length * 3;
        int graphSize = 10000;
        int efConst = 3 * (int) Math.ceil(Math.log10(graphSize));
        HnswIndex<Integer, double[], Point, Double> hnswIndex = HnswIndex
                .newBuilder(DistanceFunctions.DOUBLE_EUCLIDEAN_DISTANCE, graphSize).withM(givenM).withEf(ef)
                .withEfConstruction(efConst).build();
        Point first_test_case = Point.generateValidRandP(bd, generator);
        hnswIndex.add(first_test_case);
        selected_set.add(first_test_case);

        while (true) {
            long start = System.nanoTime();
            for (int i = 0; i < myHnsw.candNum; i++) { // Randomly generate n candidate test cases
                candP[i] = Point.generateValidRandP(bd, generator);
            }
            selected = myHnsw.findFarestCandidate(candP, hnswIndex);
            selected_set.add(candP[selected]);
            hnswIndex.add(candP[selected]);
            tcGenTime = tcGenTime + (System.nanoTime() - start);

            d1 = (int) selected_set.get(selected_set.size() - 1).vector[0];
            d2 = (int) selected_set.get(selected_set.size() - 1).vector[1];
            d3 = (int) selected_set.get(selected_set.size() - 1).vector[2];
            d4 = (int) selected_set.get(selected_set.size() - 1).vector[3];

            start = System.nanoTime();
            Weeks original_output = new Period(d1, d2, d3, d4).toStandardWeeks();
            Weeks mutated_output = new Period(d1, d2, d3, d4).toStandardWeeks_m();
            execTime = execTime + (System.nanoTime() - start);
            boolean revealFailure = !original_output.toString().equals(mutated_output.toString());
            if (revealFailure) {
                fMeasure = selected_set.size();
                writer.write(fMeasure + "\t" + tcGenTime + "\t" + execTime + "\n");
                return new long[]{fMeasure, tcGenTime, execTime};
            }
            // Make new HNSW Graph with double size of initial provided size and add all previous test cases
            if (selected_set.size() >= hnswIndex.getMaxItemCount()) {
                // System.out.println("doubling");
                graphSize = graphSize * 2;
                efConst = 3 * (int) Math.ceil(Math.log10(graphSize));
                hnswIndex = HnswIndex.newBuilder(DistanceFunctions.DOUBLE_EUCLIDEAN_DISTANCE, graphSize).withM(givenM)
                        .withEf(ef).withEfConstruction(efConst).build();
                hnswIndex.addAll(selected_set);
            }
        }
    }

    private void SimRunner(int sim, boolean withSameSeed) throws InterruptedException, IOException {
        BufferedWriter RTwriter = new BufferedWriter(new FileWriter("PeriodRT.txt"));
        BufferedWriter FSCSwriter = new BufferedWriter(new FileWriter("PeriodFSCS.txt"));
        BufferedWriter LimBalKdfcwriter = new BufferedWriter(new FileWriter("PeriodLimBalKdfc.txt"));
        BufferedWriter HNSWwriter = new BufferedWriter(new FileWriter("PeriodHNSW.txt"));
        long RT_Fms, RT_tcGenTimes, RT_execTimes, FSCS_Fms, FSCS_tcGenTimes, FSCS_execTimes, limbalKdfc_Fms, limbalKdfc_tcGenTimes, limbalKdfc_execTimes, hnsw_Fms, hnsw_tcGenTimes, hnsw_execTimes;
        RT_Fms = RT_tcGenTimes = RT_execTimes = FSCS_Fms = FSCS_tcGenTimes = FSCS_execTimes = limbalKdfc_Fms = limbalKdfc_tcGenTimes = limbalKdfc_execTimes = hnsw_Fms = hnsw_tcGenTimes = hnsw_execTimes = 0;
        for (int i = 0; i < sim; i++) {
            long mySeed;
            if (withSameSeed) {
                mySeed = new Random().nextLong();
            } else {
                mySeed = 0;
            }
            long[] resp = randomTesting(mySeed, RTwriter);
            System.out.println(i + "\t" + Arrays.toString(resp));
            RT_Fms = RT_Fms + resp[0];
            RT_tcGenTimes = RT_tcGenTimes + resp[1];
            RT_execTimes = RT_execTimes + resp[2];

            resp = fscsTesting(mySeed, FSCSwriter);
            System.out.println(i + "\t" + Arrays.toString(resp));
            FSCS_Fms = FSCS_Fms + resp[0];
            FSCS_tcGenTimes = FSCS_tcGenTimes + resp[1];
            FSCS_execTimes = FSCS_execTimes + resp[2];

            resp = limBalKdfcTesting(mySeed, LimBalKdfcwriter);
            System.out.println(i + "\t" + Arrays.toString(resp));
            limbalKdfc_Fms = limbalKdfc_Fms + resp[0];
            limbalKdfc_tcGenTimes = limbalKdfc_tcGenTimes + resp[1];
            limbalKdfc_execTimes = limbalKdfc_execTimes + resp[2];

            resp = hnswTesting(2, mySeed, HNSWwriter);
            System.out.println(i + "\t" + Arrays.toString(resp));
            hnsw_Fms = hnsw_Fms + resp[0];
            hnsw_tcGenTimes = hnsw_tcGenTimes + resp[1];
            hnsw_execTimes = hnsw_execTimes + resp[2];
        }

        System.out.println("F_MEASURE \t tcGenTime \t execTime");
        System.out.println("RT:\t" + (RT_Fms / sim) + "\t" + (RT_tcGenTimes / sim) + "\t" + (RT_execTimes / sim));
        System.out.println("FSCS:\t:" + (FSCS_Fms / sim) + "\t" + (FSCS_tcGenTimes / sim) + "\t" + (FSCS_execTimes / sim));
        System.out.println("LimBalKdfc:\t" + (limbalKdfc_Fms / sim) + "\t" + (limbalKdfc_tcGenTimes / sim) + "\t" + (limbalKdfc_execTimes / sim));
        System.out.println("HNSW:\t" + (hnsw_Fms / sim) + "\t" + (hnsw_tcGenTimes / sim) + "\t" + (hnsw_execTimes / sim));

        RTwriter.write((RT_Fms / sim) + "\t" + (RT_tcGenTimes / sim) + "\t" + (RT_execTimes / sim));
        FSCSwriter.write((FSCS_Fms / sim) + "\t" + (FSCS_tcGenTimes / sim) + "\t" + (FSCS_execTimes / sim));
        LimBalKdfcwriter.write((limbalKdfc_Fms / sim) + "\t" + (limbalKdfc_tcGenTimes / sim) + "\t" + (limbalKdfc_execTimes / sim));
        HNSWwriter.write((hnsw_Fms / sim) + "\t" + (hnsw_tcGenTimes / sim) + "\t" + (hnsw_execTimes / sim));
        RTwriter.close();
        FSCSwriter.close();
        LimBalKdfcwriter.close();
        HNSWwriter.close();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("---START---");
        int simulations = 5000;
        PeriodFeeder myPeriodFeeder = new PeriodFeeder();
        boolean seedOpt = false;
        myPeriodFeeder.SimRunner(simulations, seedOpt);
        // myBinomialFeeder.getFailureRate();
        System.out.println("---END---");
    }


}
