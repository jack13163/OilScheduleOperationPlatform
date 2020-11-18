package opt.easyjmetal.algorithm.moeas.impl.MOGWO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UF1 {//测试函数
//    public static void main(String[] args) {
//        List<Double> pra= Stream.of(1.1,2.1,3.1,4.1,5.1,6.1,7.1,8.1,9.1,9.1).collect(Collectors.toList());
//        fitness(pra).forEach(System.out::println);
//    }
    public static List<Double> fitness(List<Double> x){
        double f1,f2;
        List<Double> fitness=new ArrayList<>();
        List<Double> new_x=sin_tmp(x,0);//第二个维度之后的更新
        List<Double> odd=getOdd(new_x);
        List<Double> Even=getEven(new_x);
        f1=x.get(0)+2.0*(odd.stream().collect(Collectors.summingDouble(e->e)))/odd.size();
        f2=1.0-Math.sqrt(x.get(0))+2.0*(Even.stream().collect(Collectors.summingDouble(e->e)))/Even.size();
        fitness.add(f1);
        fitness.add(f2);
        return fitness;
    }
    public static List<Double> sin_tmp(List<Double> x,double x0){//计算第二维之后的tmp
        List<Double> tmp=new ArrayList<>();
        tmp.add(x0);
        for(int i=1;i<x.size();i++){
            double temp=Math.pow(x.get(i)-Math.sin(6*Math.PI*x.get(0)+(i+1)*Math.PI/x.size()),2);
            tmp.add(temp);
        }
        return tmp;
    }
    public static List<Double> getEven(List<Double> x){//获得偶数索引
        List<Double> even=new ArrayList<>();

        for(int i=1;i<x.size();i=i+2){
            even.add(x.get(i));
        }
        return even;

    }
    public static List<Double> getOdd(List<Double> x){//获得奇数索引值
        List<Double> odd=new ArrayList<>();
        for(int i=2;i<x.size();i=i+2){
            odd.add(x.get(i));
        }
        return odd;
    }
}
