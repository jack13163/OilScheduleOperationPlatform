其中包含两个版本的JMetal，即：
1. opt.jmetal
2. opt.easyjmetal

个人比较推荐easyjmetal

如果需要自定义解决问题，只需要仿照opt.easyjmetal.problem中的自定义问题即可；

原油调度的仿真入口位于:
1. opt.easyjmetal.algorithm.CMOEAs_main
2. opt.jmetal.problem.oil.sim.ui.MainMethod

生成的结果都会保存到result文件夹和sqlite数据库中，data文件夹下保存了一些绘图脚本

```
J_α: Energy consumption during transportation;
J_β: Crude oil mixing cost in pipelines;
J_χ: Crude oil mixing cost in charging tanks;
J_δ: Switches of distillation towers;
J_ε: Number of charging tanks to use;
```