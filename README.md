# blockchain_monitor_funchain
趣链区块信息监控，   
使用influxdb + grafana + 趣链java sdk。

# 使用该项目
1. 在dockerhub上拉取最新docker image    
   docker pull 

2. 在config文件夹下修改配置文件。     
   hpc.properties - 趣链节点的ip  
   
3. 启动docker容器，可以只指定3003端口映射出来。    
   docker run -d \
    --name docker-statsd-influxdb-grafana \
    -p 3003:3003 \
    -p 3004:8083 \
    -p 8086:8086 \
    -p 22022:22 \
    -p 8125:8125/udp \
    blcokchain-monitor_funchain:latest    
    
4. 打开浏览器，在 localhost:3003 地址查看监控内容。   

# Mapped Ports      
·Host        Container       Service    ·
3003        3003            grafana    
3004        8083            influxdb-admin    
8086        8086            influxdb    
8125        8125            statsd    
·22022       22              sshd   ·

# 效果    
![image](https://github.com/pclimbing/blockchain_monitor_funchain/raw/master/images/qu1.png)
![image](https://github.com/pclimbing/blockchain_monitor_funchain/raw/master/images/qu2.png)
