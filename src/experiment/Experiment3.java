package experiment;

import java.util.Arrays;
import java.util.Scanner;

/**
 * 操作系统实验三：时间片轮转调度算法
 *
 * @author Lex
 * @create 2021-12-22-16:46
 */
public class Experiment3 {
    /**
     * n为进程个数
     */
    private int n;
    /**
     * arrivalTime数组保存各个进程的到达时间
     */
    private int[] arrivalTime;
    /**
     * currentTime记录当前时间
     */
    private int currentTime;
    /**
     * arrived数组记录各个进程在当前时间是否已到达
     */
    private boolean[] arrived;
    /**
     * serviceTime数组保存各个进程需要用于执行的总时间
     */
    private int[] serviceTime;
    /**
     * remainedTime数组记录各个进程还需要执行的时间
     */
    private int[] remainedTime;
    /**
     * completedTime数组记录各个进程完成时的时刻
     */
    private int[] completedTime;
    /**
     * done记录已经执行完的进程数
     */
    private int done;
    /**
     * q为时间片的大小
     */
    private int q;

    public static void main(String[] args) {
        Experiment3 demo = new Experiment3();
        demo.init();
        demo.solution();
        demo.output();
    }

    private void init() {
        Scanner in = new Scanner(System.in);
        System.out.println("请输入进程数:");
        n = in.nextInt();
        serviceTime = new int[n];
        arrivalTime = new int[n];
        remainedTime = new int[n];
        arrived = new boolean[n];
        completedTime=new int[n];
        int i;
        for (i = 0; i < n; i++) {
            System.out.println("请输入第 " + i + " 个进程的到达时间和需要的执行时间:");
            arrivalTime[i] = in.nextInt();
            serviceTime[i] = in.nextInt();
            remainedTime[i] = serviceTime[i];
            arrived[i] = false;
        }
        System.out.println("请输入时间片的大小:");
        q = in.nextInt();
        currentTime = 0;
        done=0;
        in.close();
    }

    private void solution() {
        for (int i = 0; i < n; i = ++i % n) {
            if(done==n){
                break;
            }
            if(currentTime>=arrivalTime[i]){
                arrived[i]=true;
            }
            if (arrived[i] & remainedTime[i] != 0) {
                System.out.println("时刻 " + currentTime + " ,进程 " + i + " 开始执行");
                if (remainedTime[i] <= q) {
                    currentTime += remainedTime[i];
                    remainedTime[i] = 0;
                    completedTime[i]=currentTime;
                    System.out.println("时刻 " + currentTime + " ,进程 " + i + " 执行完毕");
                    done++;
                }else{
                    remainedTime[i]-=q;
                    currentTime+=q;
                }
            }
        }
    }
    private void output(){
        int i;
        // 进程的周转时间
        int[] turnAroundTime=new int[n];
        // 进程的带权周转时间
        float[] weightedTurnAroundTime=new float[n];
        // 平均周转时间
        float averageTurnAroundTime=0.0f;
        // 带权平均周转时间
        float averageWeightedTurnAroundTime=0.0f;
        Arrays.fill(turnAroundTime,0);
        Arrays.fill(weightedTurnAroundTime,0.0f);
        for(i=0;i<n;i++){
            turnAroundTime[i]=completedTime[i]-arrivalTime[i];
            averageTurnAroundTime+=turnAroundTime[i];
            weightedTurnAroundTime[i]+=turnAroundTime[i]*1.0f/serviceTime[i];
            averageWeightedTurnAroundTime+=weightedTurnAroundTime[i];
        }
        averageTurnAroundTime/=n;
        averageWeightedTurnAroundTime/=n;

        System.out.println("进程号\t到达时间\t执行时间\t完成时间\t周转时间\t带权周转时间");
        for(i=0;i<n;i++){
            System.out.println(i+"\t\t"+arrivalTime[i]+"\t\t"+serviceTime[i]+"\t\t"+completedTime[i]+"\t\t"+turnAroundTime[i]+"\t\t"+weightedTurnAroundTime[i]);
        }
        System.out.println("平均周转时间为: "+averageTurnAroundTime);
        System.out.println("带权平均周转时间为: "+averageWeightedTurnAroundTime);
    }
}
