package cleaner;

import java.util.Arrays;

/**
 *
 * @author Babak Alipour (babak.alipour@gmail.com)
 */
public class StatisticsCalc {

    public StatisticsCalc() {
    }

    /**
     * Mean calculated on second column (flow)
     *
     * @param data
     * @return
     */
    public double getMean(int[][] data) {
        double sum = 0;
        for (int i = 1; i < data.length; i++) {
            sum += data[i][1];
        }
        return sum / (float) data.length;
    }

    /**
     * Mean calculated on second column (flow) ignoring less than 0 and bigger than 100 flows
     *
     * @param data
     * @return
     */
    public double getAdjustedMean(int[][] data) {
        double sum = 0;
        int ignoredCount = 0;
        for (int i = 1; i < data.length; i++) {
            if (data[i][1] < 0 || data[i][1] >= 100) {
                ignoredCount++;
            } else {
                sum += data[i][1];
            }
        }
        int length = data.length - ignoredCount;
        return sum / (float) length;
    }

    /**
     * Mean calculated on second column (flow) from startPos (inclusive) to endPos (exclusive)
     *
     * @param data
     * @param startPos
     * @param endPos
     * @return
     */
    public double getMean(int[][] data, int startPos, int endPos) {
        double sum = 0;
        for (int i = startPos; i < endPos; i++) {
            sum += data[i][1];
        }
        return sum / (float) (endPos - startPos);
    }

    /**
     * Mean calculated on second column (flow) from startPos (inclusive) to endPos (exclusive)
     *
     * @param data
     * @param startPos
     * @param endPos
     * @return
     */
    public double getAdjustedMean(int[][] data, int startPos, int endPos) {
        double sum = 0;
        int ignoredCount = 0;
        for (int i = startPos; i < endPos; i++) {
            if (data[i][1] < 0 || data[i][1] >= 100) {
                ignoredCount++;
            } else {
                sum += data[i][1];
            }
        }
        int length = (endPos - startPos) - ignoredCount;
        if (length == 0) {
            return -1;
        }
        return sum / (float) length;
    }

    /**
     * Standard Deviation calculated on second column (flow)
     *
     * @param data
     * @param mean
     * @return
     */
    public double getStandardDeviation(int[][] data, double mean) {
        return Math.sqrt(getVariance(data, mean));
    }

    /**
     * Standard Deviation calculated on second column (flow) ignoring less than 0 and bigger than
     * 100 flows
     *
     * @param data
     * @param mean
     * @return
     */
    public double getAdjustedStandardDeviation(int[][] data, double mean) {
        return Math.sqrt(getAdjustedVariance(data, mean));
    }

    /**
     * Standard Deviation calculated on second column (flow) ignoring less than 0 and bigger than
     * 100 flows
     *
     * @param data
     * @param mean
     * @param startPos
     * @param endPos
     * @return
     */
    public double getAdjustedStandardDeviation(int[][] data, double mean, int startPos, int endPos) {
        return Math.sqrt(getAdjustedVariance(data, mean, startPos, endPos));
    }

    /**
     * Standard Deviation calculated on second column (flow) from startPos (inclusive) to endPos
     * (exclusive)
     *
     * @param data
     * @param mean
     * @param startPos
     * @param endPos
     * @return
     */
    public double getStandardDeviation(int[][] data, double mean, int startPos, int endPos) {
        return Math.sqrt(getVariance(data, mean, startPos, endPos));
    }

    private double getVariance(int[][] data, double mean) {
        double sum = 0;
        for (int i = 1; i < data.length; i++) {
            double tmp = data[i][1] - mean;
            sum += tmp * tmp;
        }
        return (sum / (float) data.length);
    }

    private double getAdjustedVariance(int[][] data, double mean) {
        double sum = 0;
        int ignoredCount = 0;
        for (int i = 1; i < data.length; i++) {
            if (data[i][1] < 0 || data[i][1] >= 100) {
                ignoredCount++;
            } else {
                double tmp = data[i][1] - mean;
                sum += tmp * tmp;
            }
        }
        int length = data.length - ignoredCount;
        return (sum / (float) length);
    }

    private double getVariance(int[][] data, double mean, int startPos, int endPos) {
        double sum = 0;
        for (int i = startPos; i < endPos; i++) {
            double tmp = data[i][1] - mean;
            sum += tmp * tmp;
        }
        return sum / (float) (endPos - startPos);
    }

    private double getAdjustedVariance(int[][] data, double mean, int startPos, int endPos) {
        double sum = 0;
        int ignoredCount = 0;
        for (int i = startPos; i < endPos; i++) {
            if (data[i][1] < 0 || data[i][1] >= 100) {
                ignoredCount++;
            } else {
                double tmp = data[i][1] - mean;
                sum += tmp * tmp;
            }
        }
        int length = data.length - ignoredCount;
        if (length == 0) {
            return 100000;
        }
        return (sum / (float) length);
    }

    /**
     * Median calculated on flow
     *
     * @param data
     * @param startPos
     * @param endPos
     * @return
     */
    public int getMedian(int[][] data, int startPos, int endPos) {
        int[][] temp = Arrays.copyOfRange(data, startPos, endPos);
        int validInTempCount = 0;
        for (int i = 0; i < temp.length; i++) {
            if (temp[i][1] >= 0 && temp[i][1] < 100) {
                validInTempCount++;
            }
        }

        if (validInTempCount == 0) {
            return -1;
        }

        int[] flows = new int[validInTempCount];
        int j = 0;
        for (int i = 0; i < temp.length; i++) {
            if (temp[i][1] >= 0 && temp[i][1] < 100) {
                flows[j] = temp[i][1];
                j++;
            }
        }
        Arrays.sort(flows);
        int middle = validInTempCount / 2;
        if (validInTempCount % 2 == 0) {
            return (int) Math.round(((double) (flows[middle - 1] + flows[middle])) / 2d);
        } else {
            return flows[middle];
        }
    }

}
