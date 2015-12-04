package cleaner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.StringTokenizer;

/**
 *
 * @author Babak Alipour (babak.alipour@gmail.com)
 */
public class Cleaner implements Runnable {

    String inputFile;
    String outputFile;
    StatisticsCalc calc;

    public Cleaner(String input, String output) {
        inputFile = input;
        outputFile = output;
        calc = new StatisticsCalc();
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();

        String input = null;
        StringTokenizer st = null;
        String input2 = null;
        StringTokenizer st2 = null;
        try {
            File f = new File(inputFile);
            /**
             * Ignore files bigger than Integer.MAX_VALUE
             */
            int length;
            int length2 = 0;
            if (f.length() > Integer.MAX_VALUE - 1000) {
                length = Integer.MAX_VALUE - 1000;
                length2 = (int) (f.length() - length);
            } else {
                length = (int) f.length();
            }
            RandomAccessFile raf;
            raf = new RandomAccessFile(f, "r");
            byte[] bytes = new byte[length];
            raf.read(bytes);
            input = new String(bytes);
            st = new StringTokenizer(input, "\n");
            bytes = null;

            input2 = null;
            st2 = null;
            if (length2 > 0) {
                byte[] bytes2 = new byte[length2];
                raf.seek(length);
                raf.read(bytes2);
                input2 = new String(bytes2);
                st2 = new StringTokenizer(input2, "\n");
                bytes2 = null;
            }
            /**
             * done reading the input
             */
            raf.close();
        } catch (Exception e) {
            System.err.println("FAILED TO LOAD FILE: " + e.getMessage());
            System.exit(1);
        }
        /**
         * Release memory (byte array objects no longer needed)
         */
        System.gc();

//        System.out.println("read:" + (System.nanoTime() - startTime) / 1000000 + "ms");
//        startTime = System.nanoTime();
        /**
         * skip first line
         */
        st.nextToken();
        int firstLineCount = st.countTokens();
        int lineCount = firstLineCount;
        if (st2 != null) {
            lineCount += st2.countTokens();
        }
        /**
         * 0: lane_id, 1: flow, 2: lineNumber
         */
        int[][] data = new int[lineCount][3];

        for (int i = 1; i < lineCount; i++) {
            String[] sss;
            if (st.hasMoreTokens()) {
                String s = st.nextToken();
                sss = s.split(",");
                if (sss.length < 6) {
                    s += st2.nextToken();
                }
                sss = s.split(",");
            } else {
                sss = st2.nextToken().split(",");
            }
            data[i][0] = parseInt(sss[0]);
            data[i][1] = parseInt(sss[3]);
            data[i][2] = i;
        }
//        System.out.println("parse:" + (System.nanoTime() - startTime) / 1000000 + "ms");
//        startTime = System.nanoTime();

        input = null;
        st = null;
        if (input2 != null) {
            input2 = null;
            st2 = null;
        }
        System.gc();

//        System.out.println("gc:" + (System.nanoTime() - startTime) / 1000000 + "ms");
//        startTime = System.nanoTime();
        sort(data);

//        System.out.println("sort:" + (System.nanoTime() - startTime) / 1000000 + "ms");
//        startTime = System.nanoTime();
        /**
         * 2136 possible unique lane_id according to inventory file
         */
        int[] groupBoundaries = new int[2136];
        int j = 0;
        int last_lane_id = data[1][0];
        groupBoundaries[0] = 1;
        for (int i = 1; i < lineCount; i++) {
            if (data[i][0] != last_lane_id) {
                j++;
                groupBoundaries[j] = i;
                last_lane_id = data[i][0];
            }
        }

        double globalMean = calc.getAdjustedMean(data);
        double globalStd = calc.getAdjustedStandardDeviation(data, globalMean);

        int currentGroup = 0;
        last_lane_id = data[1][0];
        double mean = 0;
        double std = 0;

        double groupMean = calc.getAdjustedMean(data, groupBoundaries[currentGroup], groupBoundaries[currentGroup + 1]);;
        //#initialize lastValid with mean (~50ms penalty)
        int lastValid = (int) Math.round(calc.getAdjustedMean(data, groupBoundaries[currentGroup], groupBoundaries[currentGroup + 1]));

        byte[] isCorrect = new byte[lineCount];
        byte[] errorCodes = new byte[lineCount];
        /**
         * The big loop
         */
        for (int i = 1; i < lineCount; i++) {
            //if group changed, update lastValid with mean of that group
            if (data[i][0] != last_lane_id) {
                currentGroup++;
                lastValid = (int) Math.round(calc.getAdjustedMean(data, groupBoundaries[currentGroup], groupBoundaries[currentGroup + 1]));
                last_lane_id = data[i][0];
                mean = 0;
                std = 0;

                groupMean = calc.getAdjustedMean(data, groupBoundaries[currentGroup], groupBoundaries[currentGroup + 1]);
            }

            int startIndex = i - 4 < groupBoundaries[currentGroup] ? groupBoundaries[currentGroup] : i - 4;
            int endIndex = i + 5 > groupBoundaries[currentGroup + 1] ? groupBoundaries[currentGroup + 1] : i + 5;
            if (groupBoundaries[currentGroup + 1] == 0) {
                endIndex = i + 5;
                if (endIndex > lineCount) {
                    endIndex = lineCount;
                }
            }

            int median = calc.getMedian(data, startIndex, endIndex);

            mean = calc.getAdjustedMean(data, startIndex, endIndex);

            if (mean == -1) {
                //if mean is NaN, std is meaningless, set to a very high number
                std = 10000;
            } else {
                std = calc.getAdjustedStandardDeviation(data, mean, startIndex, endIndex);
            }

            Tuple t = cleanFlow(data[i][1], groupMean, globalStd, std, median, lastValid);
            isCorrect[i] = t.isCorrect;
            errorCodes[i] = t.errorCode;

            if (t.isCorrect == 1) {
                lastValid = t.flow;
            } else {
                data[i][1] = t.flow;
                //TODO: set correct,errorCode
            }
        }
//        System.out.println("process:" + (System.nanoTime() - startTime) / 1000000 + "ms");
//        startTime = System.nanoTime();

        /**
         * No longer needed
         */
        groupBoundaries = null;

        sortOnLineNumber(data);

//        System.out.println("sortOn#:" + (System.nanoTime() - startTime) / 1000000 + "ms");
//        startTime = System.nanoTime();
        StringBuilder output = new StringBuilder();

        for (int i = 1; i < lineCount; i++) {
            output.append(isCorrect[i]).append("\t").append(data[i][1]).append("\t").append(errorCodes[i] == n ? " " : errorCodes[i]).append("\n");
        }
        File f2 = new File(outputFile);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f2), 65_536)) {
            bw.append(output);
            bw.flush();
        } catch (IOException ex) {
            System.err.println("FAILED TO WRITE OUTPUT:" + ex.getMessage());
        }
        System.out.println("Cleaning:" + inputFile + " finished in " + (System.nanoTime() - startTime) / 1000000 + "ms \t" + "Output:" + outputFile);
    }

    private int parseInt(String s) {
        if (s.isEmpty()) {
            return 255;
        }
        int value = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                value = value * 10 + (c - 48);
            } else if (c == '.') {
                break;
            } else {
                return 255;
            }
        }
        return value;
    }

    private void sort(int[][] data) {
        java.util.Arrays.sort(data, new Comparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                return Integer.compare(a[0], b[0]);
            }
        });
    }

    private void sortOnLineNumber(int[][] data) {
        java.util.Arrays.sort(data, new Comparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                return Integer.compare(a[2], b[2]);
            }
        });
    }

    /**
     *
     * @param flow
     * @param mean
     * @param std
     * @param median
     * @param lastValid
     * @return a tuple (correctedFlow,isCorrect,errorCode)
     */
    byte _0 = 0;
    byte _1 = 1;
    byte _2 = 2;
    byte _3 = 3;
    byte n = 10;

    private Tuple cleanFlow(int flow, double globalMean, double globalStd, double localStd, int localMedian, int lastValid) {
        if (Math.abs(flow - globalMean) > 4 * globalStd) {
            return new Tuple(lastValid, _0, _3);
        } else if (localMedian == -1) {
            if (flow < 0) {
                return new Tuple(0, _0, _1);
            }
        } else if (flow < 0) {
            return new Tuple(localMedian, _0, _1);
        } else if (Math.abs(flow - localMedian) > Math.max(7, Main.sdThresh * localStd)) {
            return new Tuple(localMedian, _0, _2);
        }
        return new Tuple(flow, _1, n);
    }
}
