package experiment;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Scanner;

/**
 * 操作系统实验二：银行家算法
 *
 * @author Lex
 * @create 2021-12-22-16:45
 */
public class Experiment2 {
    /**
     * 用于保存系统各类资源的最大数目
     */
    private int[] resource;
    /**
     * 用于保存当前系统各类资源的可分配数
     */
    private int[] available;
    /**
     * 用于保存各进程对各类资源的最大需求,第i行表示进程i,第j列表示第j种资源
     */
    private int[][] need;
    /**
     * 用于保存系统当前已分配给各进程的各类资源数,i、j含义同上
     */
    private int[][] allocation;
    /**
     * deque用于保存成功执行的进程序列
     */
    private Deque<Integer> deque;

    public static void main(String[] args) {
        Experiment2 demo = new Experiment2();
        demo.init();
    }

    /**
     * 初始化:获取用户输入的基础数据,申请数组空间
     */
    private void init() {
        Scanner in = new Scanner(System.in);
        int type;
        do {
            System.out.println("请输入合法的资源种类数:");
            type = in.nextInt();
        } while (type < 0);
        int processNum;
        do {
            System.out.println("请输入合法的进程个数:");
            processNum = in.nextInt();
        } while (processNum < 0);
        resource = new int[type];
        available = new int[type];
        need = new int[processNum][];
        allocation = new int[processNum][];
        deque = new ArrayDeque<>(processNum);
        for (int i = 0; i < processNum; i++) {
            need[i] = new int[type];
            allocation[i] = new int[type];
        }
        set();
        solution();
        // 注意,这个Scanner in由于使用了静态流System.in,如果在set函数调用之前close(),
        // 那么即使set函数内部再次申请一个Scanner in=new Scanner(System.in),这个in也是不可用的
        // 因此,要么统一使用同一个Scanner,要么所有的Scanner都在最后关闭
        in.close();
    }

    /**
     * 获取用户的详细设置
     */
    private void set() {
        Scanner in = new Scanner(System.in);
        do {
            System.out.println("请输入合法的 " + resource.length + " 种资源各种的资源总数:");
            for (int i = 0; i < resource.length; i++) {
                resource[i] = in.nextInt();
            }
            System.out.println("请输入合法的 " + need.length + " 个进程关于 " + resource.length + " 种资源的总需求情况:");
            for (int i = 0; i < need.length; i++) {
                System.out.print("第 " + i + " 个进程的资源总需求:");
                for (int j = 0; j < resource.length; j++) {
                    need[i][j] = in.nextInt();
                }
            }
            System.out.println("请输入合法的 " + need.length + " 个进程关于 " + resource.length + " 种资源的已分配情况:");
            for (int i = 0; i < need.length; i++) {
                System.out.print("第 " + i + " 个进程的资源已分配情况:");
                for (int j = 0; j < resource.length; j++) {
                    allocation[i][j] = in.nextInt();
                }
            }
        } while (!check());
    }

    /**
     * check函数用于检查用户的输入是否正确
     */
    private boolean check() {
        // 检查进程已分配的资源数是否超过了声明的最大资源需求数
        for (int i = 0; i < need.length; i++) {
            for (int j = 0; j < resource.length; j++) {
                if (allocation[i][j] > need[i][j]) {
                    System.out.println("分配不合理,不予分配，请重新输入分配方案:");
                    return false;
                }
            }
        }
        // 检查每个进程声明的每种资源最大需求数是否超过了该资源总数
        for (int j = 0; j < resource.length; j++) {
            for (int[] ints : need) {
                if (ints[j] > resource[j]) {
                    System.out.println("分配不合理,不予分配，请重新输入分配方案:");
                    return false;
                }
            }
        }
        System.out.println("分配无系统性异常,将进行预分配和安全性检查");
        return true;
    }

    /**
     * 使用银行家算法判断是否会发生死锁,若不会,则打印进程执行序列,否则返回死锁信息
     */
    private void solution() {
        int i, j;
        // done数组用于保存各个进程是否已经执行完毕
        boolean[] done = new boolean[need.length];
        Arrays.fill(done, false);
        // doneNum用于记录当前已完成的进程数
        int numOfDone = 0;
        // unableTemporarily表示暂时被确定在当前情况下不能被分配的进程数
        int unableTemporarily=0;
        // 获取当前各类资源的剩余情况
        for (j = 0; j < resource.length; j++) {
            available[j] = resource[j];
            for (i = 0; i < need.length; i++) {
                available[j] -= allocation[i][j];
            }
        }
        // 针对每一个进程,判断该进程是否可以被满足
        for (i = 0; numOfDone < need.length; i = ++i % need.length) {
            if (done[i]) {
                continue;
            }
            for (j = 0; j < available.length; j++) {
                // 针对某一个进程,若发现有任意一种资源无法满足,则该进程退出检查
                if (need[i][j] - allocation[i][j] > available[j]) {
                    // break跳出内层循环,继续执行外层的下一次循环操作
                    break;
                }
            }
            // 当前进程的所有资源申请都可以得到满足,那么就满足它,满足之后进程将所有已分配的资源归还
            if (j == available.length) {
                done[i] = true;
                numOfDone++;
                // 如果进行了一次成功的分配,那么资源情况就要更新,之前不能做的或许就可以做了,因此状态重置
                unableTemporarily=0;
                for (j = 0; j < available.length; j++) {
                    available[j] += allocation[i][j];
                }
                deque.add(i);
            }else{
                unableTemporarily++;
            }
            if(unableTemporarily!=0 & unableTemporarily==need.length-numOfDone){
                System.out.println("当前分配方案会发生死锁!");
                return;
            }
        }
        System.out.println("不会产生死锁,其中一种安全序列为:");
        System.out.println(deque);
    }
}
