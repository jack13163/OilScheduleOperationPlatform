from sklearn.cluster import KMeans
import numpy as np
import time
from sklearn.metrics import silhouette_score,calinski_harabasz_score
import matplotlib.pyplot as plt
from sklearn import preprocessing

# 载入数据
X = np.loadtxt("oil.pf", delimiter=' ')
# 过滤
X = X[np.where(X[:,4]==10)]
X = X[0:100,0:4];
min_max_scaler = preprocessing.MinMaxScaler()
X = min_max_scaler.fit_transform(X)

clusters = range(2,31)

# calinski_harabasz_scores
calinski_harabaz_scores = []
print('start time: ',time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())))
for k in clusters:
    y_pred = KMeans(n_clusters = k, verbose = 0, n_jobs = -1, random_state = 1).fit_predict(X)
    score = calinski_harabasz_score(X, y_pred)
    calinski_harabaz_scores.append(score)
print('finish time: ',time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())))
plt.plot(clusters, calinski_harabaz_scores, '*-')
plt.xlabel('k')
plt.ylabel('calinski_harabaz')
plt.show()

# 轮廓系数silhouette_scores
silhouette_scores = []
print('start time: ',time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())))
for k in clusters:
    y_pred = KMeans(n_clusters = k, verbose = 0, n_jobs = -1, random_state=1).fit_predict(X)
    score = silhouette_score(X, y_pred)
    silhouette_scores.append(score)
print('finish time: ',time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())))
plt.plot(clusters, silhouette_scores, '*-')
plt.xlabel('k')
plt.ylabel('silhouette_score')
plt.show()