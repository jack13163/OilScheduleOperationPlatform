%% 计算不同算法的c指标
function [ result ] = get_C()
    % 基本路径
    base_path = 'NSGAII/';
    
    % 所有的策略
    filesname = dir(base_path);
    names = {filesname.name};
    algorithms = names(3:end);
    data = cell(1,3);
    
    % 读取以 'FUN*.tsv'为后缀的文件    
    for i=1:length(algorithms)
        name=char(fullfile(base_path,algorithms(i),'FUN0.tsv'));
        data(i) = {csvread(name)};
    end
    
    %% 计算C指标
    for i=1:length(algorithms)
        for j=1:length(algorithms)
            if(i ~= j)
                fprintf('C(%s,%s)=%f\n',char(algorithms(i)),char(algorithms(j)),C(cell2mat(data(i)),cell2mat(data(j))));
            end
        end
    end
end