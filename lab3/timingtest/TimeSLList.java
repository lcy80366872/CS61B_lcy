package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        SLList<Integer> L =new SLList<>();
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        int temp=1000;
        while(temp<=62800){
            Ns.addLast(temp);
            opCounts.addLast(temp);
            temp=temp*2;
        }
        for (int i = 0; i < Ns.size(); i++) {
            int N = Ns.get(i);
            int j = 0;

            while (j<N){
                L.addLast(j);
                j = j+1;
            }
            Stopwatch sw = new Stopwatch();
            for (int k=0;k<10000;k++) {
                L.getLast();
            }
            times.addLast(sw.elapsedTime());
        }
        printTimingTable(Ns,times,opCounts);
    }

}
