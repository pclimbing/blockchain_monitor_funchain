
docker run -d \
  --name funchain-monitor-influxdb-grafana \
  -p 4003:3003 \
  -p 4004:8083 \
  -p 8086:8086 \
  -p 22022:22 \
  -p 8125:8125/udp \
  blockchain-monitor-funchain:latest

java -jar chainReader.jar
