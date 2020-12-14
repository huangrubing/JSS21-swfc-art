// package com.github.jelmerk.knn.examples.programs.triangle;
//
// import com.github.jelmerk.knn.DistanceFunctions;
// import com.github.jelmerk.knn.examples.auxiliary.model.Point;
// import com.github.jelmerk.knn.examples.fscs.art.FSCS_ART;
// import com.github.jelmerk.knn.examples.hnsw.art.HNSW_ART;
// import com.github.jelmerk.knn.examples.kdfc.art.KDFC_ART;
// import com.github.jelmerk.knn.hnsw.HnswIndex;
//
// import java.io.BufferedWriter;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.util.ArrayList;
//
//
// public class triangle_feeder {
//     public static int[][] bd = {{-25, 25}, {-25, 25}, {-25, 25}, {-25, 25}, {-25, 25}, {-25, 25}};
//     public static int Dimension = 6;
//     public static int d1, d2, d3, d4, d5, d6;
//
//     public static double theta = 0.000713;
//     private static Triangle myTriangle = new Triangle();
//
//     public static int randomTesting() {
//         int f_measure = 1;
//         do {
//             d1 = bd[0][0] + (int) (Math.random() * ((bd[0][1] - bd[0][0]) + 1));
//             d2 = bd[1][0] + (int) (Math.random() * ((bd[1][1] - bd[1][0]) + 1));
//             d3 = bd[2][0] + (int) (Math.random() * ((bd[2][1] - bd[2][0]) + 1));
//             d4 = bd[3][0] + (int) (Math.random() * ((bd[3][1] - bd[3][0]) + 1));
//             d5 = bd[4][0] + (int) (Math.random() * ((bd[4][1] - bd[4][0]) + 1));
//             d6 = bd[5][0] + (int) (Math.random() * ((bd[5][1] - bd[5][0]) + 1));
//             f_measure++;
//         } while (!myTriangle.producesError(d1, d2, d3, d4, d5, d6));
//         return f_measure;
//     }
//
//     public static int fscsTesting() {
//         FSCS_ART fscs = new FSCS_ART(10);
//         int generatedNum = 0;
//         int maxTry = Integer.MAX_VALUE /100;
//         int selected;
//         Point[] tcP = new Point[maxTry + 2];
//         Point[] candP = new Point[fscs.candNum];
//
//         tcP[0] = Point.generateRandP(bd);  // first test case
//         generatedNum++; // increasing f-measure without testing first test case
//         do {
//             for (int i = 0; i < fscs.candNum; i++) { // Ëæ»úÉú³Én¸öºòÑ¡µÄ²âÊÔÓÃÀý
//                 candP[i] = Point.generateRandP(bd);
//             }
//             selected = fscs.findFarestCandidate(tcP, generatedNum, candP);
//             tcP[generatedNum] = candP[selected];
//             generatedNum++;
//             d1 = (int) tcP[generatedNum - 1].coordPoint[0];
//             d2 = (int) tcP[generatedNum - 1].coordPoint[1];
//             d3 = (int) tcP[generatedNum - 1].coordPoint[2];
//             d4 = (int) tcP[generatedNum - 1].coordPoint[3];
//             d5 = (int) tcP[generatedNum - 1].coordPoint[4];
//             d6 = (int) tcP[generatedNum - 1].coordPoint[5];
//             // System.out.println(tcP[generatedNum - 1].toString());
//             if (myTriangle.producesError(d1, d2, d3, d4, d5, d6)) {
//                 break;
//             }
//         } while (generatedNum < maxTry);
//         return generatedNum;
//     }
//
//     public static int limBalKdfcTesting() {
//         int[] backNum = new int[100 * (int) (1 / theta)];
//         backNum[0] = 1;
//         backNum[1] = 1;
//         double d = bd.length;
//         for (int i = 2; i < backNum.length; i++) {
//             backNum[i] = (int) Math.ceil(1 / 2.0 * Math.pow((d + 1 / d), 2) * (Math.log(i) / Math.log(2)));
//         }
//         Point p = Point.generateRandP(bd);
//         KDFC_ART kdfc = new KDFC_ART(bd);
//         kdfc.insertPointByStrategy(p);
//         Point finalCase;
//         ArrayList<Point> canD;
//         while (true) {
//             canD = new ArrayList<>(); // ����������ѡ��
//             for (int i = 0; i < kdfc.candidateNum; i++) {
//                 canD.add(Point.generateRandP(kdfc.inputDomain));
//             }
//             finalCase = canD.get(0);
//             int back = backNum[kdfc.size];
//             double distance = kdfc.getMinDisByBacktracking(finalCase, back);
//             for (int c = 1; c < kdfc.candidateNum; c++) {
//                 d = kdfc.getMinDisByBacktracking(canD.get(c), back); // get minimum distance
//                 if (distance < d) { // get the candidate for smallest distance
//                     distance = d;
//                     finalCase = canD.get(c);
//                 }
//             }
//             kdfc.insertPointByStrategy(finalCase);
//             d1 = (int) finalCase.coordPoint[0];
//             d2 = (int) finalCase.coordPoint[1];
//             d3 = (int) finalCase.coordPoint[2];
//             d4 = (int) finalCase.coordPoint[3];
//             d5 = (int) finalCase.coordPoint[4];
//             d6 = (int) finalCase.coordPoint[5];
//             // System.out.println(tcP[generatedNum - 1].toString());
//             if (myTriangle.producesError(d1, d2, d3, d4, d5, d6)) {
//                 break;
//             }
//         }
//         return kdfc.size;
//     }
//
//     public static int hnswTesting(int ef) throws InterruptedException {
//         HNSW_ART myHnsw = new HNSW_ART(10);
//         int selected;
//         Point[] candP = new Point[myHnsw.candNum]; // Candidate test Case set (candidate_set)
//         // HNSW GRAPH INITIALIZATION
//         int givenM = Dimension * 3;
//         int graphSize = 10000;
//         int efConst = 3 * (int) Math.ceil(Math.log10(graphSize));
//         HnswIndex<Integer, double[], Point, Double> hnswIndex = HnswIndex
//                 .newBuilder(DistanceFunctions.DOUBLE_EUCLIDEAN_DISTANCE, graphSize).withM(givenM).withEf(ef)
//                 .withEfConstruction(efConst).build();
//         Point first_test_case = Point.generateRandP(bd);
//         hnswIndex.add(first_test_case);
//         do {
//             // Randomly generate n candidate test cases
//             for (int i = 0; i < myHnsw.candNum; i++) {
//                 candP[i] = Point.generateRandP(bd);
//                 // System.out.print(candP[i].printTestCase() + ",");
//
//             }
//             // Make new HNSW Graph with double size of initial provided size and add all previous test cases
//             if (hnswIndex.size() >= hnswIndex.getMaxItemCount()) {
//                 // System.out.println("doubling");
//                 HnswIndex<Integer, double[], Point, Double> temp_hnswIndex = hnswIndex;
//                 graphSize = graphSize * 2;
//                 efConst = 3 * (int) Math.ceil(Math.log10(graphSize));
//                 hnswIndex = HnswIndex.newBuilder(DistanceFunctions.DOUBLE_EUCLIDEAN_DISTANCE, graphSize).withM(givenM)
//                         .withEf(ef).withEfConstruction(efConst).build();
//                 hnswIndex.addAll(temp_hnswIndex.items());
//             }
//             selected = myHnsw.findFarestCandidate(candP, hnswIndex);
//             hnswIndex.add(candP[selected]);
//             d1 = (int) candP[selected].vector[0];
//             d2 = (int) candP[selected].vector[1];
//             d3 = (int) candP[selected].vector[2];
//             d4 = (int) candP[selected].vector[3];
//             d5 = (int) candP[selected].vector[4];
//             d6 = (int) candP[selected].vector[5];
//             // System.out.print(selected_set.get(f_measure).printTestCase());
//         } while (!myTriangle.producesError(d1, d2, d3, d4, d5, d6));
//         return hnswIndex.size();
//     }
//
//
//     public static void main(String[] args) throws InterruptedException, IOException {
//         int simulations = 5000;
//         int f_measure;
//         long f_time, total_f_time, totalFMeasure;
//         String fileName = "Trianglekdfc&hnsw.txt";
//         BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
//
//
//         // System.out.println("\nFSCS");
//         // writer.write("\nFSCS");
//         // total_f_time = 0;
//         // totalFMeasure = 0;
//         // for (int i = 1; i <= simulations; i++) {
//         //     long start_time = System.currentTimeMillis();
//         //     f_measure = fscsTesting();
//         //     long end_time = System.currentTimeMillis();
//         //
//         //     f_time = end_time - start_time;
//         //     totalFMeasure = totalFMeasure + f_measure;
//         //     total_f_time = total_f_time + f_time;
//         //
//         //     System.out.println(i + "\tF-measure: " + f_measure + "\tF-time: " + f_time + "\tMeanFmeasure: " + (float) totalFMeasure / (float) i
//         //             + "\tMeanF-time: " + (float) total_f_time / (float) i);
//         //     writer.write("\n" + i + "\tF-measure: " + f_measure + "\tF-time: " + f_time + "\tMeanFmeasure: " + (float) totalFMeasure / (float) i
//         //             + "\tMeanF-time: " + (float) total_f_time / (float) i);
//         // }
//         // System.out.println("AvgF_measure: " + (double) totalFMeasure / (double) simulations);
//         // writer.write("\nAvgF_measure: " + (double) totalFMeasure / (double) simulations);
//         // System.out.println("AvgF_time: " + (double) total_f_time / (double) simulations);
//         // writer.write("\nAvgF_time: " + (double) total_f_time / (double) simulations);
//
//
//         System.out.println("\nLIMBAL KDFC");
//         writer.write("\nLIMBAL KDFC");
//         total_f_time = 0;
//         totalFMeasure = 0;
//         for (int i = 1; i <= simulations; i++) {
//             long start_time = System.currentTimeMillis();
//             f_measure = limBalKdfcTesting();
//             long end_time = System.currentTimeMillis();
//
//             f_time = end_time - start_time;
//             totalFMeasure = totalFMeasure + f_measure;
//             total_f_time = total_f_time + f_time;
//
//             System.out.println(i + "\tF-measure: " + f_measure + "\tF-time: " + f_time + "\tMeanFmeasure: " + (float) totalFMeasure / (float) i
//                     + "\tMeanF-time: " + (float) total_f_time / (float) i);
//             writer.write("\n" + i + "\tF-measure: " + f_measure + "\tF-time: " + f_time + "\tMeanFmeasure: " + (float) totalFMeasure / (float) i
//                     + "\tMeanF-time: " + (float) total_f_time / (float) i);
//         }
//         System.out.println("AvgF_measure: " + (double) totalFMeasure / (double) simulations);
//         writer.write("\nAvgF_measure: " + (double) totalFMeasure / (double) simulations);
//         System.out.println("AvgF_time: " + (double) total_f_time / (double) simulations);
//         writer.write("\nAvgF_time: " + (double) total_f_time / (double) simulations);
//
//
//         System.out.println("\nHNSW1");
//         writer.write("\nHNSW1");
//         total_f_time = 0;
//         totalFMeasure = 0;
//         for (int i = 1; i <= simulations; i++) {
//             long start_time = System.currentTimeMillis();
//             f_measure = hnswTesting(1);
//             long end_time = System.currentTimeMillis();
//
//             f_time = end_time - start_time;
//             totalFMeasure = totalFMeasure + f_measure;
//             total_f_time = total_f_time + f_time;
//
//             System.out.println(i + "\tF-measure: " + f_measure + "\tF-time: " + f_time + "\tMeanFmeasure: " + (float) totalFMeasure / (float) i
//                     + "\tMeanF-time: " + (float) total_f_time / (float) i);
//             writer.write("\n" + i + "\tF-measure: " + f_measure + "\tF-time: " + f_time + "\tMeanFmeasure: " + (float) totalFMeasure / (float) i
//                     + "\tMeanF-time: " + (float) total_f_time / (float) i);
//         }
//         System.out.println("AvgF_measure: " + (double) totalFMeasure / (double) simulations);
//         writer.write("\nAvgF_measure: " + (double) totalFMeasure / (double) simulations);
//         System.out.println("AvgF_time: " + (double) total_f_time / (double) simulations);
//         writer.write("\nAvgF_time: " + (double) total_f_time / (double) simulations);
//
//
//         System.out.println("\nHNSW2");
//         writer.write("\nHNSW2");
//         total_f_time = 0;
//         totalFMeasure = 0;
//         for (int i = 1; i <= simulations; i++) {
//             long start_time = System.currentTimeMillis();
//             f_measure = hnswTesting(2);
//             long end_time = System.currentTimeMillis();
//
//             f_time = end_time - start_time;
//             totalFMeasure = totalFMeasure + f_measure;
//             total_f_time = total_f_time + f_time;
//
//             System.out.println(i + "\tF-measure: " + f_measure + "\tF-time: " + f_time + "\tMeanFmeasure: " + (float) totalFMeasure / (float) i
//                     + "\tMeanF-time: " + (float) total_f_time / (float) i);
//             writer.write("\n" + i + "\tF-measure: " + f_measure + "\tF-time: " + f_time + "\tMeanFmeasure: " + (float) totalFMeasure / (float) i
//                     + "\tMeanF-time: " + (float) total_f_time / (float) i);
//         }
//         System.out.println("AvgF_measure: " + (double) totalFMeasure / (double) simulations);
//         writer.write("\nAvgF_measure: " + (double) totalFMeasure / (double) simulations);
//         System.out.println("AvgF_time: " + (double) total_f_time / (double) simulations);
//         writer.write("\nAvgF_time: " + (double) total_f_time / (double) simulations);
//
//         writer.close();
//     }
// }
//
