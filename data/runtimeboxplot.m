path = 'times.csv';
fid = fopen(path);
% 读取标题
title = textscan(fid, '%s%s%s%s%s%s',1,'delimiter', ',');
c1 = cell2mat(title{1});
c2 = cell2mat(title{2});
c3 = cell2mat(title{3});
c4 = cell2mat(title{4});
c5 = cell2mat(title{5});
c6 = cell2mat(title{6});
% 读取数据
data = textscan(fid, '%d%d%d%d%d%d','delimiter', ',');
data = cell2mat(data);

boxplot(data);

xlim([0, 7]);%只设定x轴的绘制范围
 
set(gca,'XTick',[0:1:7]) %改变x轴坐标间隔显示 这里间隔为2
set(gca,'FontSize',16); %设置坐标轴的数字大小，包括legend文字大小
set(gca,'xticklabel',{'',c1,c2,c3,c4,c5,c6,''});%设置X轴的刻度标签