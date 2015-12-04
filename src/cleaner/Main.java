package cleaner;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Babak Alipour (babak.alipour@gmail.com)
 */
public class Main {

    public static int sdThresh = 3;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String input;
        String output;
        String sep = File.separator;
        try {
            input = args[0];
            output = input + sep + "output";
        } catch (Exception e) {
            //Test folder on a Windows machine
            input = "C:\\Users\\Babak\\s3\\core\\lane_measurements\\test";
            output = input + "\\output";
            System.err.println("Input directory path not provided, using my default windows path");
        }
        File outputDir = new File(output);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        long startTime = System.nanoTime();
        ArrayList<String> filePaths = new ArrayList<>();
        File[] files = new File(input).listFiles();
        long totalSize = 0;
        for (File file : files) {
            if (file.isFile()) {
                filePaths.add(file.getAbsolutePath());
                totalSize += file.length();
            }
        }
        if (filePaths.isEmpty()) {
            System.err.println("INVALID INPUT PATH");
            System.exit(1);
        }
        int coreCount = Runtime.getRuntime().availableProcessors()/2;
        ExecutorService exs = Executors.newFixedThreadPool(coreCount);

        for (String path : filePaths) {
            Cleaner c = new Cleaner(path, path.replace(input, output).replace("cleaning_test", "cleaning_subm").replace(".csv", "_08.tsv"));
            exs.execute(c);
        }
        exs.shutdown();

        final long size = totalSize;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Cleaned " + filePaths.size() + " files,"
                        + " with a total size of " + size / 1_048_576 + "MBs"
                        + " in " + (System.nanoTime() - startTime) / 1000000 + "ms");
            }
        });
    }
}
