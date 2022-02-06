# 绘制时隙优化图，标出极值点坐标

from scipy.optimize import minimize_scalar
import math
import matplotlib.pyplot as plt
import numpy as np

# 定义时隙-时间函数

def T(f,n):
    if f!=0:
        return ( (-0.8)*f+(0.4+f*1.2)*math.exp(n*1.0/f) )/n
   


n = 100

# 暴力穷举，找极值点
# 根据函数特点，先递减后递增，在n附近取得极值
minf = n/2
mint = T(minf,n)
for f in range( math.ceil(n/2), n*2):
    t = T(f,n)
    if(t < mint):
        mint = t 
        minf = f 

# 选择作图时的时隙上下限 （在极值点附近变化较小，为了让变化更明显，缩小时隙变化范围，减小时间变化的梯度）
if(n>0 and n<= 20):
    floor = 0
    ceil = n*2
elif(n > 20 and n < 100):
    floor = minf-10
    ceil=minf+20
else:
    floor=minf-100
    ceil=minf+100

print("ceil=",ceil,"floor=",floor)
print("minf=",minf,"mint=",mint)

# 作图
# 作函数图象
plt.xlim( (floor, ceil) )
plt.ylim( (mint-0.005, mint+0.005) )
plt.xlabel("frame size")
plt.ylabel("average time per frame (ms)")
X=np.linspace(floor, ceil,200)
Y=[T(f,n) for f in X]
for f in X:
    print("f=",f,"t=",T(f,n))
plt.plot(X,Y)
# 标出极值点坐标
plt.plot(minf ,mint ,'o')
plt.text(minf, mint, (minf,round(mint,4)) )# （时间保留4位小数）
plt.show()


