package experiment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 操作系统实验一：4种页面置换算法的实现
 *
 * @author Lex
 * @create 2021-12-21-18:34
 */
public class Experiment1 {
    /**
     * count用于记录缺页次数，用类的成员变量表示，减少参数传递，统一solution格式
     */
    private int count = 0;
    /**
     * map用于记录页面被访问的次数
     */
    private Map<Integer, Integer> map;

    public static void main(String[] args) {
        Random random = new Random();
        int[] request = new int[20];
        int pre = 0;
        int now;
        for (int i = 0; i < 20; i++) {
            // 产生20个0~5的随机整数，要求相邻两个数不重复
            while ((now = random.nextInt(6)) == pre) {
            }
            request[i] = now;
            pre = now;
        }
        Experiment1 demo = new Experiment1();
        demo.solution(request, "OPT");
        demo.solution(request, "FIFO");
        demo.solution(request, "LRU");
        demo.solution(request, "LFU");
    }

    /**
     * 判断页面是否已被分配
     *
     * @param array  内存分配记录数组
     * @param col    申请次序，相当于array中对应的列号
     * @param target 当前申请的页面
     * @return 当前申请的页面是否已被分配
     */
    private boolean contains(int[][] array, int col, int target) {
        boolean contains = false;
        for (int[] ints : array) {
            if (ints[col] == target) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    /**
     * 对一些数据结构进行初始化操作
     *
     * @param block   存放内存物理块分配的int型二维数组
     * @param display 存放各种打印结果的Object型二维数组
     * @param request 页面访问序列数组
     */
    private void init(int[][] block, Object[][] display, int[] request) {
        // block数组的初始化
        for (int i = 0; i < 3; i++) {
            block[i] = new int[20];
        }
        // 一开始初始化为-1，表示物理块为空闲资源
        for (int i = 0; i < 3; i++) {
            Arrays.fill(block[i], -1);
        }
        // display数组的初始化
        for (int i = 0; i < 5; i++) {
            display[i] = new Object[20];
        }
        // 第一行保存页面访问序列
        for (int i = 0; i < 20; i++) {
            display[0][i] = request[i];
        }

        // 每选择一个不同的置换算法，都将count恢复为0
        count = 0;
        // 对map进行初始化，每种页面都没有被访问过
        map = new HashMap<>(6);
        for (int i = 0; i < 6; i++) {
            map.put(i, 0);
        }
    }

    /**
     * 算法的主体
     *
     * @param request 页面访问序列数组
     * @param mode    算法模式
     */
    public void solution(int[] request, String mode) {
        // 假设系统为该进程分配了3个内存物理块
        // 二维数组block用于记录每次内存物理块的分配情况
        int[][] block = new int[3][];
        // 用于标注本次申请是否会发生缺页中断
        boolean pageMissingInterrupt;
        // 二维数组display用于汇总打印信息
        Object[][] display = new Object[5][];
        // operation用于记录操作，便于最后输出显示
        String operation;
        // 初始化操作
        init(block, display, request);
        // 循环体内执行真正的页面置换操作
        for (int i = 0; i < 20; i++) {
            pageMissingInterrupt = true;
            for (int j = 0; j < 3; j++) {
                if (contains(block, i, request[i])) {
                    pageMissingInterrupt = false;
                    break;
                }
            }
            // 如果产生了缺页中断，则将页面调入
            if (pageMissingInterrupt) {
                // 如果有空的物理块，则直接调入空的物理块
                int emptyLocation;
                if ((emptyLocation = hasEmptyBlock(block, i)) != 3) {
                    block[emptyLocation][i] = request[i];
                    operation = "*";
                    count++;
                }
                // 物理块已满，则根据算法替换掉其中一个页面
                else {
                    int replaceLocation = findReplaceLocation(block, i, request, mode);
                    operation = block[replaceLocation][i] + "";
                    count++;
                    block[replaceLocation][i] = request[i];
                }
            }
            // 若申请的页面已在内存中，则不需要进行置换
            else {
                operation = " ";
            }
            // 无论前面发生了什么，至少本次访问页面成功，将本次访问的页面的访问数+1
            map.put(request[i], map.get(request[i]) + 1);

            display[1][i] = operation;
            for (int j = 0; j < 3; j++) {
                if (block[j][i] == -1) {
                    display[j + 2][i] = "空";
                }
                else {
                    display[j + 2][i] = block[j][i];
                }
            }
            update(block, i);
        }
        outputResult(display, mode);
    }

    /**
     * 输出算法处理的最终结果
     *
     * @param display 用于保存输出结果的Object型二维数组
     * @param mode    算法模式
     */
    private void outputResult(Object[][] display, String mode) {
        switch (mode) {
            case "OPT":
                System.out.println("最佳置换算法(OPT)测试结果：");
                break;
            case "FIFO":
                System.out.println("先进先出置换算法(FIFO)测试结果：");
                break;
            case "LRU":
                System.out.println("最近最久未使用置换算法(LRU)测试结果：");
                break;
            case "LFU":
                System.out.println("最少使用置换算法(LFU)测试结果：");
                break;
            default:
        }
        String[] output = {"页面走向\t\t", "缺页及置换\t", "物理块1\t\t", "物理块2\t\t", "物理块3\t\t"};
        for (int i = 0; i < 5; i++) {
            System.out.print(output[i]);
            for (int j = 0; j < 20; j++) {
                System.out.print(display[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println("缺页中断次数：" + count + " 次, 系统进行了 " + (count - 3) + " 次置换," + "缺页率：" + count / 20.0f + "\n");

    }

    /**
     * 检测可能存在的空闲物理块号
     *
     * @param block    内存物理块分配情况
     * @param sequence 本次申请在申请队列中的次序
     * @return 本次分配表中的第一个空闲物理块，若无空闲，则会返回一个可被检测出越界的值
     */
    private int hasEmptyBlock(int[][] block, int sequence) {
        int j;
        for (j = 0; j < 3; j++) {
            if (block[j][sequence] == -1) {
                break;
            }
        }
        return j;
    }

    /**
     * 根据算法名进行置换页面的求解
     *
     * @param block    内存物理块的分配情况
     * @param sequence 本次访问页面在访问序列中的次序
     * @param request  页面访问序列
     * @param mode     算法模式
     * @return 即将被置换出去的页面在物理块中的位置
     */
    private int findReplaceLocation(int[][] block, int sequence, int[] request, String mode) {
        // judgePage表示当前物理块中被考查的页面
        int judgePage;
        // result保存最终决定被替换掉的页号在物理块中的位置
        int result = 0;
        switch (mode) {
            case "OPT":
                // interval表示物理块中的某个页面还有多久会被再次调度
                int interval;
                // max用于保存迄今为止需要再次被调度所花的最长时间
                int max = -1;
                for (int j = 0; j < 3; j++) {
                    judgePage = block[j][sequence];
                    interval = Arrays.stream(request).boxed().collect(Collectors.toList()).subList(sequence, 20).indexOf(judgePage);
                    // 后续子序列中不会再出现该页面，因此置为Max
                    if (interval == -1) {
                        interval = Integer.MAX_VALUE;
                    }
                    if (interval > max) {
                        result = j;
                        max = interval;
                    }
                }
                break;
            case "FIFO":
                result = count % 3;
                break;
            case "LRU":
                int lastVisit;
                int earliestLastVisit = Integer.MAX_VALUE;
                for (int j = 0; j < 3; j++) {
                    lastVisit = Arrays.stream(request).boxed().collect(Collectors.toList()).subList(0, sequence).lastIndexOf(block[j][sequence]);
                    if (lastVisit < earliestLastVisit) {
                        earliestLastVisit = lastVisit;
                        result = j;
                    }
                }
                break;
            case "LFU":
                // minVisit表示物理块中被访问次数最少的页面的访问次数
                int minVisit = Integer.MAX_VALUE;
                for (int j = 0; j < 3; j++) {
                    if (map.get(block[j][sequence]) < minVisit) {
                        minVisit = map.get(block[j][sequence]);
                        result = j;
                    }
                }
                break;
            default:
                // do nothing
        }
        return result;
    }

    /**
     * 更新block数组的信息，即将当前次序后面的物理块分配情况都更新成前面更改过的情况
     *
     * @param block    存放内存物理块分配情况的int型二维数组
     * @param sequence 申请访问的页面序列数组
     */
    private void update(int[][] block, int sequence) {
        for (int i = 0; i < 3; i++) {
            // 本质上就是手动数组复制，这里由IDEA自动生成面向对象的写法
            if (20 - (sequence + 1) >= 0) {
                System.arraycopy(block[i], sequence + 1 - 1, block[i], sequence + 1, 20 - (sequence + 1));
            }
        }
    }
}
