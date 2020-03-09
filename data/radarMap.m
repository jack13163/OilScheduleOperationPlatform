%雷达图
data = importdata('oil.pf');
data = data(data(:,5) == 10,:);
data = data(1:100,1:4);
varNames = { 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange'};

subplot(1,2,1);
glyphplot(data,'glyph','star','varLabels',{ 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange'});
box on
axis off

% 数据标准化
ND = normlization(data, 2);

% 确定聚类个数
k = 9;

% 聚类
colors =  lines(k);
label = init_methods(ND, k, 2);

[data,ind] = sortrows([data,label],5);
subplot(1,2,2);
h=glyphplot(data(:,1:4),'glyph','star','varLabels',{ 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange'},...
    'ObsLabels',num2str(ind));
for i=1:k
    hf = h(data(:,5)==i,1);
    ha = h(data(:,5)==i,2);
    for j=1:length(hf)
        hf(j).Color=colors(i,:);
        ha(j).Color=colors(i,:);
    end
end
box on
axis off

set(gcf,'Position',[600,162,900,400]);%设置绘图大小和位置

%% 数据预处理
    % 输入：无标签数据，聚类数，选择方法
    % 输出：聚类标签
function label = init_methods(data, K, choose)
    if choose==1
        %随机初始化，随机选K行作为聚类中心，并用欧氏距离计算其他点到其聚类，将数据集分为K类，输出每个样例的类标签
        [X_num, ~]=size(data);
        rand_array=randperm(X_num);    %产生1~X_num之间整数的随机排列   
        para_miu=data(rand_array(1:K), :);  %随机排列取前K个数，在X矩阵中取这K行作为初始聚类中心
        %欧氏距离，计算（X-para_miu）^2=X^2+para_miu^2-2*X*para_miu'，矩阵大小为X_num*K
        distant=repmat(sum(data.*data,2),1,K)+repmat(sum(para_miu.*para_miu,2)',X_num,1)-2*data*para_miu';
        %返回distant每行最小值所在的下标
        [~,label]=min(distant,[],2);
    elseif choose==2
        %用kmeans进行初始化聚类，将数据集聚为K类，输出每个样例的类标签
        label=kmeans(data, K);
    elseif choose==3
        %用FCM算法进行初始化
        options=[NaN, NaN, NaN, 0];
        [~, responsivity]=fcm(data, K, options);   %用FCM算法求出隶属度矩阵
        [~, label]=max(responsivity', [], 2);
    end
end

%% 数据归一化
function data = normlization(data, choose)
    if choose==0
        % 不归一化
        data = data;
    elseif choose==1
        % Z-score归一化
        data = bsxfun(@minus, data, mean(data));
        data = bsxfun(@rdivide, data, std(data));
    elseif choose==2
        % 最大-最小归一化处理
        [data_num,~]=size(data);
        data=(data-ones(data_num,1)*min(data))./(ones(data_num,1)*(max(data)-min(data)));
    end
end