package org.uma.jmetal.util.jmathplot;


import org.math.plot.Plot2DPanel;
import org.math.plot.Plot3DPanel;
import org.math.plot.PlotPanel;
import org.math.plot.plots.Plot;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Created by stu on 11/3/15.
 */
public class ScatterPlot {

    private String methodName_;
    private String problemName_;
    private SolutionSet population_;

    private org.math.plot.plots.ScatterPlot infeasiblePlot_;
    private org.math.plot.plots.ScatterPlot feasiblePlot_;
    private org.math.plot.plots.ScatterPlot paretoPlot_;

    PlotPanel plot_;
    JFrame frame_;

    public static int autoCount = 0;


    public ScatterPlot(String methodName, String problemName, SolutionSet population){
        methodName_ = methodName;
        problemName_ = problemName;
        population_ = population;
    }

    public void displayPf(String filepath){
        double[][] pf_point = FileUtils.readFromFile(filepath);
        if(pf_point[0].length == 2){
            plot_ = new Plot2DPanel("SOUTH");

        }else if(pf_point[0].length == 3){
            plot_ = new Plot3DPanel("SOUTH");
            plot_.setAxisLabel(2, "f3");
        }else {
            return;
        }
        plot_.setAxisLabel(0, "f1");
        plot_.setAxisLabel(1, "f2");
        plot_.setAdjustBounds(true);


        // load pareto front data
        paretoPlot_ = new org.math.plot.plots.ScatterPlot("Pareto front", Color.green,pf_point);
        plot_.plotCanvas.addPlot(paretoPlot_);


        // initialize individuals in the population
        feasiblePlot_ = new org.math.plot.plots.ScatterPlot("Feasible Individuals", Color.blue, null);
        infeasiblePlot_ = new org.math.plot.plots.ScatterPlot("InfFeasible Individuals", Color.red, null);

        // display the frame_
        frame_ = new JFrame(methodName_ + " : "+ problemName_);
        frame_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame_.setContentPane(plot_);
        frame_.setSize(600,600);
        frame_.setVisible(true);
    }

    synchronized public void displayPop(SolutionSet pop){
        autoCount++;
        // display feasible solution and infeasible solution separately
        SolutionSet feasiblePop = pop.GetFeasible();
        SolutionSet infeasiblePop = pop.GetInfeasible();
        int objNumber = pop.get(0).getNumberOfObjectives();



        // get the feasible points
        int feasibleSize = feasiblePop.size();
        double[][] fobjs = new double[feasibleSize][objNumber];
        for(int i = 0; i < objNumber; i++){
            for(int j = 0; j < feasibleSize; j++){
                fobjs[j][i] = feasiblePop.get(j).getObjective(i);
            }
        }
        feasiblePlot_.setData(fobjs);

        // get the infeasible points
        int infeasibleSize = infeasiblePop.size();
        double[][] infobjs = new double[infeasibleSize][objNumber];
        for(int i = 0; i < objNumber; i++){
            for(int j = 0; j < infeasibleSize; j++){
                infobjs[j][i] = infeasiblePop.get(j).getObjective(i);
            }
        }
        infeasiblePlot_.setData(infobjs);

        // update the individuals in the frame
        try{

            if(plotContains(feasiblePlot_) == -1 && feasibleSize > 0){
                plot_.plotCanvas.addPlot(feasiblePlot_);
            }
            if(plotContains(infeasiblePlot_) == -1 && infeasibleSize > 0){
                plot_.plotCanvas.addPlot(infeasiblePlot_);
            }
            frame_.repaint();
            Thread.sleep(50);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private int plotContains(org.math.plot.plots.ScatterPlot scatterPlot){
        LinkedList<Plot> plots =  plot_.plotCanvas.getPlots();
        for(int i = 0; i < plots.size(); i++){
            if(plots.get(i).getName() == scatterPlot.getName()){
                return i ;
            }
        }
        return -1;
    }
}
