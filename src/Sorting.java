import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Sorting {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    /* define constants */
    static long MAXVALUE = 200000000;
    static long MINVALUE = -200000000;
    static int numberOfTrials = 50;
    static int MAXINPUTSIZE = (int) Math.pow(2, 20);
    static int MININPUTSIZE = 1;
    static int STEPNUMBER = 20;
    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time

    static String ResultsFolderPath = "/home/sethowens/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;


    public static void main(String[] args) {
        /*checkFunction(Sorting::bubbleSortNumberList);
        checkFunction(Sorting::mergeSort);
        checkFunction(Sorting::insertionSort);
        checkFunction(Sorting::quickSort);
        checkFunction(Sorting::naiveQuickSort);*/
        //run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("QuickSortPreSorted-Exp1-ThrowAway.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("QuickSortPreSorted-Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("QuickSortPreSorted-Exp3.txt");
    }

    static void runFullExperiment(String resultsFileName) {

        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch (Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file " + ResultsFolderPath + resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();
        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for (int inputSize = MININPUTSIZE; inputSize <= MAXINPUTSIZE; inputSize *= 2) {
            // progress message...
            System.out.println("Running test for input size " + inputSize + " ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial
            System.out.print("    Generating test data...");
            long[] testList = createRandomIntegerList(inputSize);
            testList = quickSort(testList);
            System.out.println("...done.");
            System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();


            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the tirals
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random key to search in the range of a the min/max numbers in the list
                //long testSearchKey = (long) (0 + Math.random() * (testList[testList.length-1]));
                /* force garbage collection before each trial run so it is not included in the time */
                // System.gc();

                //TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                quickSort(testList);
                // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double) numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n", inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }

    public static long[] createRandomIntegerList(long size){
        long[] newList = new long[(int)size];
        for(int j = 0; j<size; j++) {
            newList[j] = (long)(MAXVALUE + Math.random() * (MAXVALUE - MINVALUE));
        }
        return newList;
    }

    public static long getCpuTime(){
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported() ?
                bean.getCurrentThreadCpuTime() : 0L;
    }

    public static long[] bubbleSortNumberList(long[] list){
        for(int i=0; i<list.length; i++){
            for(int j=0;j<list.length-1;j++){
                if(list[j]>list[j+1]){
                    long tmp = list[j];
                    list[j] = list[j+1];
                    list[j+1] = tmp;
                }
            }
        }
        return list;
    }

    public static long[] insertionSort(long[] list){
        //Get the length of the list
        int len = list.length;

        //Loop through the list. Start at the second value since we check everything before it
        for(int iii = 1; iii < len; iii++){
            //get current value
            long currentValue = list[iii];
            int jjj = iii - 1;

            //Loop through the values before iii and check if they are greater than currentValue, then insert
            while(jjj >= 0 && list[jjj] >= currentValue){
                //Shift it forward
                list[jjj + 1] = list[jjj];
                jjj--;
            }
            //Insert the currentValue
            list[jjj + 1] = currentValue;
        }
        return list;
    }

    public static long[] mergeSort(long[] list){
        if (list.length <= 1){
            return list;
        }
        int len = list.length;
        long[] topHalf = new long[(len + 1)/2];
        long[] bottomHalf = new long[len - topHalf.length];

        for(int iii = 0; iii< len; iii++){
            if(iii < topHalf.length){
                topHalf[iii] = list[iii];
            }
            else{
                bottomHalf[iii - topHalf.length] = list[iii];
            }
        }
        long[] bottomSorted = mergeSort(bottomHalf);
        long[] topSorted = mergeSort(topHalf);

        long[] sortedList = merge(topSorted, bottomSorted);
        return sortedList;
    }

    public static long[] merge(long[] topList, long[] bottomList){
        long[] sortedList = new long[topList.length + bottomList.length];
        int iA = 0;
        int iB = 0;
        int iS = 0;

        while(iA < topList.length && iB < bottomList.length){
            if(topList[iA] < bottomList[iB]){
                sortedList[iS] = topList[iA];
                iA++;
            }
            else{
                sortedList[iS] = bottomList[iB];
                iB++;
            }
            iS++;
        }
        while (iA < topList.length){
            sortedList[iS] = topList[iA];
            iA++;
            iS++;
        }
        while (iB < bottomList.length){
            sortedList[iS] = bottomList[iB];
            iB++;
            iS++;
        }
        return sortedList;
    }

    public static long[] quickSort(long[] list){
        int startIndex = 0;
        int endIndex = list.length - 1;
        list = quickSortWorker(list, startIndex, endIndex);
        return list;
    }

    public static long[] quickSortWorker(long[] list, int startIndex, int endIndex){
        int index = smartPivot(list, startIndex, endIndex);
        if(startIndex < index - 1){
            quickSortWorker(list, startIndex, index - 1);
        }
        if(endIndex > index){
            quickSortWorker(list, index, endIndex);
        }
        return list;
    }

    public static long[] naiveQuickSort(long[] list){
        int startIndex = 0;
        int endIndex = list.length - 1;
        naiveQuickSortWorker(list, startIndex, endIndex);
        return list;
    }

    public static void naiveQuickSortWorker(long[] list, int startIndex, int endIndex){
        if (startIndex < endIndex){
            //sort around pivot
            int pivot = naivePivot(list, startIndex, endIndex);

            //Recursive iteration
            naiveQuickSortWorker(list, startIndex, pivot - 1);
            naiveQuickSortWorker(list, pivot + 1, endIndex);
        }
    }

    public static int findPivot(long[] list, int startIndex, int endIndex){
        int pivotIndex = 0;
        int range = endIndex - startIndex;
        //Since the list is random, a random sampling can be uniform steps
        int step = range / STEPNUMBER;
        if (step == 0){
            step = 1;
        }

        int above = 0;
        int below = 0;
        int temp = startIndex;
        for(;startIndex < endIndex; startIndex += step){
            if(list[startIndex] >= list[temp]){
                if(above > below){
                    temp = startIndex;
                    below++;
                }
                above++;
            }
            else{
                if(below > above){
                    temp = startIndex;
                    above++;
                }
                below++;
            }
        }
        return temp;
    }

    public static int smartPivot(long[] list, int startIndex, int endIndex){
        //set pivot to random median
        int medianIndex = findPivot(list, startIndex, endIndex);
        long pivot = list[medianIndex];
        while(startIndex <= endIndex){
            while(list[startIndex] < pivot){
                startIndex++;
            }
            while(list[endIndex] > pivot){
                endIndex--;
            }

            if(startIndex <= endIndex){
                long tempValue = list[startIndex];
                list[startIndex] = list[endIndex];
                list[endIndex] = tempValue;

                startIndex++;
                endIndex--;
            }
        }

        return startIndex;
    }

    public static int naivePivot(long[] list, int startIndex, int endIndex){
        //set the pivot to the first value
        long pivot = list[startIndex];
        //start at the end for index
        int iii = endIndex + 1;
        //Start at end and work way back
        for(int jjj = endIndex; jjj > startIndex; jjj--){
            //if value is larger than pivot value
            if(list[jjj] > pivot){
                iii--;
                long tempValue = list[iii];
                list[iii] = list[jjj];
                list[jjj] = tempValue;
            }
        }

        //swap the pivot
        long tempValue = list[iii - 1];
        list[iii - 1] = list[startIndex];
        list[startIndex] = tempValue;
        return iii - 1;
    }

    public static boolean verifySort(long[] list){
        for(int iii = 0; iii < list.length - 1; iii++){
            if(list[iii] > list[iii + 1]){
                return false;
            }
        }
        return true;
    }

    public static void checkFunction(Function<long[], long[]> testSort){
        if(!smallCheck(testSort)){
            System.out.println("Failed small list check.");
            return;
        }
        long[] list = createRandomIntegerList(50000);

        list = testSort.apply(list);
        if (!verifySort(list)){
            System.out.println("Failed large list check.");
            return;
        }

        System.out.println("All checks successful.");
        return;
    }

    public static boolean smallCheck(Function<long[], long[]> testSort){
        long[] testListA = {3,5,2,4,9,7,16};
        long[] testListB = {2,7,5,3,4};

        testListA = testSort.apply(testListA);
        testListB = testSort.apply(testListB);

        if(verifySort(testListA)){
            for(int iii = 0; iii < testListA.length; iii++){
                System.out.print(testListA[iii] + " ");
            }
            System.out.println("");
        }
        else{
            return false;
        }
        if (verifySort(testListB)){
            for(int iii = 0; iii <  testListB.length; iii++){
                System.out.print(testListB[iii] + " ");
            }
            System.out.println("");
        }
        else{
            return false;
        }
        return true;
    }
}