# blockchain_monitor_funchain
趣链区块信息监控，   
使用influxdb + grafana + 趣链java sdk。

# 使用该项目
1. 在dockerhub上拉取最新docker image    
`docker pull fenneld/blockchain-monitor-funchain` 

2. 创建一个文件夹，增加配置文件hpc.properties     
```
#初始化HyperchainAPI的

#Hyperchain Nodes IP Ports
node={"nodes":["10.15.190.85:3011","10.15.190.85:3012", "10.15.190.85:3013", "10.15.190.85:3014"]}

#重发次数
resendTime = 10

#第一次轮训时间间隔 unit /ms
firstPollingInterval = 1000

#发送一次,第一次轮训的次数
firstPollingTimes = 10

#第二次轮训时间间隔 unit /ms
secondPollingInterval = 1000

#发送一次,第二次轮训的次数
secondPollingTimes = 10

#Send Tcert during the request or not
SendTcert = false

#if sendTcert is true , you should add follow path.
ecertPath = src/test/resources/certs/ecert.cert
ecertPriPath = src/test/resources/certs/ecert.priv
uniquePrivPath = src/test/resources/certs/unique.priv
uniquePubPath = src/test/resources/certs/unique.pub/
```    

3. 增加文件docker-compose.yaml    
```
version: '2'

services:
  monitoring:
    container_name: funchain-monitor
    image: fenneld/blockchain-monitor-funchain
    volumes:
      - $HPC_CONFIG:/root/hpc.properties
    ports:
      - "4003:3003"
      - "4004:8083"
      - "8086:8086"
      - "22022:22"
      - "8125:8125/udp"
```    
    
4. 启动docker容器    
    `HPC-CONFIG=./hpc.properties docker-compose up -d`
5. 打开浏览器，在 localhost:3003 地址查看监控内容。   

# Mapped Ports      
Host | Container | Service
-----|-----------|----
4003 | 3003      | grafana
4004 | 8003      | influxdb-admin
8086 | 8086      | influxdb
8125 | 8125      | statsd
22022| 22        | sshd


# 效果    
![image](https://github.com/pclimbing/blockchain_monitor_funchain/raw/master/images/qu1.png)
![image](https://github.com/pclimbing/blockchain_monitor_funchain/raw/master/images/qu2.png)
