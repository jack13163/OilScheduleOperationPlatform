%% 三种策略的30次运行结果最终对比
% 计算最终结果，类似于如下的形式：
% data = [0.150000 0.680000;
%     0.070000 0.060000;
%     0.190000 0.050000];

basePath = 'C:/code/OilScheduleOperationPlatform-master/result/Experiment/PF/%s.%s.rf';

%所有的策略
policies = {'EDF_PS','EDF_TSS','BT'};
%算法
algorithm = 'NSGAII';

%读取文件中的原始数据
rawData = cell(1,3);
for i=1:3
    fileName = sprintf(basePath,policies{i},algorithm);
    rawData(i) = {csvread(fileName)};
end

%计算C指标值
result=zeros(3);
for i=1:3
    for j=1:3
        if i ~= j
            dataSet1 = cell2mat(rawData(i));
            dataSet2 = cell2mat(rawData(j));
            result(i,j) = C(dataSet1,dataSet2);
            fprintf(sprintf('C(%s,%s)=%f',policies{i},policies{j},result(i,j)));
        end
        fprintf('\n');
    end
end

%结果重排序
map = [2, 4, 1; 3, 7, 5; 6, 8, 9];%定义映射关系，即第一行第一列中对应原来数据中的第一行第二列，...
labels = {'C(EDF_PS,EDF_PS)','C(EDF_PS,EDF_TSS)','C(EDF_PS,BT)';
    'C(EDF_TSS,EDF_PS)','C(EDF_TSS,EDF_TSS)','C(EDF_TSS,BT)';
    'C(BT,EDF_PS)','C(BT,EDF_TSS)','C(BT,BT)'};
resultLabels = cell(3);
for i=1:3
   for j=1:3
       row = ceil(map(i,j)/3);
       col = ceil(mod(map(i,j)-1,3)+1);
       %重排序
       resultLabels(i,j) = {labels(row,col)};
       data(i,j) = result(row,col);
   end
end

%确保正确【建议不要删除】
for i=1:3
   for j=1:2
       fprintf('%s=%f\n',cell2mat(resultLabels{i,j}),data(i,j));
   end
end


% 绘制条形图
bar(data(:,1:2));

% 提取每个系列的值
AB=data(:,1);
BA=data(:,2);

% 标记数据到柱状图
offset_vertical = 0.02;   % 根据需要调整
offset_horizon = 0.15;  % 根据需要调整
for i = 1:length(AB)
    if AB(i)>=0
        text(i - offset_horizon,AB(i) + offset_vertical,num2str(AB(i)),'VerticalAlignment','middle','HorizontalAlignment','center');
    else
        text(i - offset_horizon,AB(i) - offset_vertical,num2str(AB(i)),'VerticalAlignment','middle','HorizontalAlignment','center');
    end
end
for i = 1:length(BA)
    if BA(i)>=0
        text(i + offset_horizon,BA(i) + offset_vertical,num2str(BA(i)),'VerticalAlignment','middle','HorizontalAlignment','center','FontName','Times New Roman','FontSize',12);
    else
        text(i + offset_horizon,BA(i) - offset_vertical,num2str(BA(i)),'VerticalAlignment','middle','HorizontalAlignment','center','FontName','Times New Roman','FontSize',12);
    end
end

% 设置图像显示格式
set(gca,'FontName','Times New Roman','FontSize',12,'LineWidth',1.5);%设置坐标轴中字体及大小
legend({'C(A,B)','C(B,A)'},'FontName','Times New Roman','FontSize',12,'LineWidth',1.5);%设置图例中字体及大小
xlabel('','FontName','Times New Roman','FontSize',12);%设置x轴标签字体及大小
ylabel('C metrics','Interpreter','latex','FontName','Times New Roman','FontSize',12);%设置y轴标签字体及大小
ylim([0 1]);
set(gca,'TickLabelInterpreter','latex');
set(gca,'xticklabel', {'$$R(EDF_{PS},EDF_{TSS})$$','$$R(EDF_{PS},BT)$$','$$R(EDF_{TSS},BT)$$'});
set(gcf,'Position',[347,162,750,480]);%设置绘图大小和位置