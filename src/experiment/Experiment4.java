package experiment;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

/**
 * 操作系统实验四:模拟SJF和HRN两种作业调度算法
 *
 * @author Lex
 * @create 2021-12-22-16:46
 */
public class Experiment4 {
    /**
     * pcb数组用于保存各个进程
     */
    private ProcessControlBlock[] pcb;
    /**
     * n为进程个数
     */
    private int n;
    /**
     * currentTime记录当前时间
     */
    private int currentTime;
    /**
     * done记录已经执行完的进程数
     */
    private int done;
    /**
     * arrived用于记录当前时刻已到达的进程数
     */
    private int arrived;

    public static void main(String[] args) {
        Experiment4 demo = new Experiment4();
        demo.init();
//        demo.solution("SJF");
//        demo.output("SJF");
//        System.out.println();
        demo.solution("HRN");
        demo.output("HRN");
    }

    private void init() {
        Scanner in = new Scanner(System.in);
        System.out.println("请输入进程数:");
        n = in.nextInt();
        pcb = new ProcessControlBlock[n];
        int i;
        for (i = 0; i < n; i++) {
            System.out.println("请输入第 " + i + " 个进程的到达时间和需要的执行时间:");
            pcb[i] = new ProcessControlBlock(i, in.nextInt(), in.nextInt());
        }
        currentTime = 0;
        done = 0;
        in.close();
    }

    /**
     * 设计缺陷:1、不能处理时间开始时,没有任务到达的情况 2、不能处理当一个任务完成时,没有可执行的任务到达的情况
     */
    private void solution(String mode) {
        arrived = 0;
        while (done != n) {
            choose(mode);
            doIt();
        }
    }

    private void choose(String mode) {
        for (int i = 0; i < n; i++) {
            if (currentTime >= pcb[i].arrivalTime & !pcb[i].arrived) {
                pcb[i].arrived = true;
                arrived++;
            }
        }
        switch (mode) {
            case "SJF":
                // 还有arrived没排序,done的一定已经arrived,目标是把arrived的排在未arrived的前面,把undone的排在done的前面
                // 真正要考察的是:已arrived且undone的进程的serviceTime大小,第一次排序把done的排在后面,前面的都是undone的再根据arrived排序
                Arrays.sort(pcb, (o1, o2) -> Boolean.compare(o1.done, o2.done));
                Arrays.sort(pcb, 0, n - done, (o1, o2) -> Boolean.compare(o2.arrived, o1.arrived));
                // 问题在于,已经把done的排在最后,undone的里面也已经把arrived的排在最前,那么如何确定undone且arrived的进程个数呢?
                // 已知:done的一定arrived,arrived的不一定done,所以需要考察的是前(arrived-done)个进程
                Arrays.sort(pcb, 0, arrived-done, Comparator.comparingInt(o -> o.serviceTime));
                break;
            case "HRN":
                Arrays.sort(pcb, (o1, o2) -> Boolean.compare(o1.done, o2.done));
                Arrays.sort(pcb, 0, n - done, (o1, o2) -> Boolean.compare(o2.arrived, o1.arrived));
                Arrays.sort(pcb, 0, arrived-done, (o1, o2) ->
                    {
                    o1.responseRatio = (currentTime - o1.arrivalTime + o1.serviceTime) * 1.0f / o1.serviceTime;
                    o2.responseRatio = (currentTime - o2.arrivalTime + o2.serviceTime) * 1.0f / o2.serviceTime;
                    return Float.compare(o2.responseRatio, o1.responseRatio);
                    });
                break;
            default:
        }
    }

    private void doIt() {
        pcb[0].startTime = currentTime;
        pcb[0].done = true;
        done++;
        currentTime += pcb[0].serviceTime;
        pcb[0].completedTime = currentTime;
    }

    private void output(String mode) {
        int i;
        // 进程的周转时间
        int[] turnAroundTime = new int[n];
        // 进程的带权周转时间
        float[] weightedTurnAroundTime = new float[n];
        // 平均周转时间
        float averageTurnAroundTime = 0.0f;
        // 带权平均周转时间
        float averageWeightedTurnAroundTime = 0.0f;
        Arrays.fill(turnAroundTime, 0);
        Arrays.fill(weightedTurnAroundTime, 0.0f);
        // 将pcb数组恢复成按进程号升序排序的形式
        Arrays.sort(pcb, Comparator.comparingInt(o -> o.id));
        for (i = 0; i < n; i++) {
            turnAroundTime[i] = pcb[i].completedTime - pcb[i].arrivalTime;
            averageTurnAroundTime += turnAroundTime[i];
            weightedTurnAroundTime[i] += turnAroundTime[i] * 1.0f / pcb[i].serviceTime;
            averageWeightedTurnAroundTime += weightedTurnAroundTime[i];
        }
        averageTurnAroundTime /= n;
        averageWeightedTurnAroundTime /= n;
        switch (mode) {
            case "SJF":
                System.out.println("最短作业优先(SJF)作业算法计算结果:");
                break;
            case "HRN":
                System.out.println("最高响应比优先(HRN)作业算法计算结果:");
                break;
            default:
        }
        System.out.println("进程号\t到达时间\t执行时间\t开始时间\t完成时间\t周转时间\t带权周转时间");
        for (i = 0; i < n; i++) {
            System.out.println(i + "\t\t" + pcb[i].arrivalTime + "\t\t" + pcb[i].serviceTime + "\t\t" + pcb[i].startTime + "\t\t" + pcb[i].completedTime + "\t\t" + turnAroundTime[i] + "\t\t" + weightedTurnAroundTime[i]);
        }
        System.out.println("平均周转时间为: " + averageTurnAroundTime);
        System.out.println("带权平均周转时间为: " + averageWeightedTurnAroundTime);
    }

    static class ProcessControlBlock {
        /**
         * id为进程号
         */
        int id;
        /**
         * done表示当前进程是否已执行
         */
        boolean done = false;
        /**
         * serviceTime表示当前进程要求服务时间
         */
        int serviceTime;
        /**
         * responseRatio表示当前进程的响应比
         */
        float responseRatio;
        /**
         * arrivalTime表示当前进程的到达时间
         */
        int arrivalTime;
        /**
         * completedTime记录当前进程完成时的时刻
         */
        int completedTime;
        /**
         * arrived记录当前进程在当前时间是否已到达
         */
        boolean arrived = false;
        /**
         * startTime用于记录进程开始执行的时间
         */
        private int startTime;

        public ProcessControlBlock(int id, int arrivalTime, int serviceTime) {
            this.id = id;
            this.arrivalTime = arrivalTime;
            this.serviceTime = serviceTime;
        }
    }
}
