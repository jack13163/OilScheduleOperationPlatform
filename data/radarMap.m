%雷达图
data = csvread('100-100-NSGAII/Experiment/PF/oilschedule.pf');
varNames = { 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange', 'numberOfTankUsed'};

figure;
glyphplot(data,'glyph','star','varLabels',{ 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange', 'numberOfTankUsed'});
set(gcf,'Position',[600,162,900,850]);%设置绘图大小和位置
box off
axis off

% 数据标准化
ND = normlization(data, 1);

% 确定聚类个数
k = getk(ND,50);
k = 15;

% 聚类
colors =  lines(k);
label = init_methods(ND, k, 2);

figure;
[data,ind] = sortrows([data,label],6);
h=glyphplot(data(:,1:5),'glyph','star','varLabels',{ 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange', 'numberOfTankUsed'},...
    'ObsLabels',num2str(ind));
for i=1:k
    hf = h(data(:,6)==i,1);
    ha = h(data(:,6)==i,2);
    for j=1:length(hf)
        hf(j).Color=colors(i,:);
        ha(j).Color=colors(i,:);
    end
end
set(gcf,'Position',[600,162,900,850]);%设置绘图大小和位置
box off
axis off


%% 确定聚类个数
function k=getk(data,K)
    [n,p]=size(data);
    for i=1:p
       minr=min(data(:,i));
       maxr=max(data(:,i));
       data(:,i)=(data(:,i)-minr)/(maxr-minr);%归一化
    end
    D=zeros(K-1,2);T=0;
    for k=2:K
        T=T+1;
        [lable,c,sumd,d]=kmeans(data,k);
        %data，n*p原始数据向量
        %lable，n*1向量，聚类结果标签；
        %c，k*p向量，k个聚类质心的位置
        %sumd，1*k向量，类间所有点与该类质心点距离之和
        %d，n*k向量，每个点与聚类质心的距离
        %-----求每类数量-----
        sort_num=zeros(k,1);%每类数量
        for i=1:k
            for j=1:n
                if lable(j,1)==i
                    sort_num(i,1)=sort_num(i,1)+1;
                end
            end
        end
        %-----求每类数量-----
        sort_ind=sumd./sort_num;%每类类内平均距离
        sort_ind_ave=mean(sort_ind);%类内平均距离
        %-----求类间平均距离-----
        h=nchoosek(k,2);A=zeros(h,2);t=0;sort_outd=zeros(h,1);
        for i=1:k-1
            for j=i+1:k
                t=t+1;
                A(t,1)=i;
                A(t,2)=j;
            end
        end
        for i=1:h
            for j=1:p
                sort_outd(i,1)=sort_outd(i,1)+(c(A(i,1),j)-c(A(i,2),j))^2;
            end
        end
        sort_outd_ave=mean(sort_outd);%类间平均距离
        %-----求类间平均距离-----
        D(T,1)=k;
        D(T,2)=sort_ind_ave/sort_outd_ave;
    end
    plot(D(:,1),D(:,2));
end

%% 数据预处理
    % 输入：无标签数据，聚类数，选择方法
    % 输出：聚类标签
function label=init_methods(data, K, choose)
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