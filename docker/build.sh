#!/bin/bash

docker-compose down -v
rm -rf ./database/mysql/member/write/data/*
rm -rf ./database/mysql/member/read/data/*
docker-compose build
docker-compose up -d

until docker exec database-mysql-member-write sh -c 'export MYSQL_PWD=1234; mysql -u root -e ";"'
do
    echo "Waiting for database-mysql-member-write database connection..."
    sleep 4
done

priv_stmt='CREATE USER "member_replication"@"%" IDENTIFIED BY "1234"; GRANT REPLICATION SLAVE ON *.* TO "member_replication"@"%"; FLUSH PRIVILEGES;'
docker exec database-mysql-member-write sh -c "export MYSQL_PWD=1234; mysql -u root -e '$priv_stmt'"

until docker-compose exec database-mysql-member-read sh -c 'export MYSQL_PWD=1234; mysql -u root -e ";"'
do
    echo "Waiting for database-mysql-member-read database connection..."
    sleep 4
done

MS_STATUS=`docker exec database-mysql-member-write sh -c 'export MYSQL_PWD=1234; mysql -u root -e "SHOW MASTER STATUS"'`
CURRENT_LOG=`echo $MS_STATUS | awk '{print $6}'`
CURRENT_POS=`echo $MS_STATUS | awk '{print $7}'`

start_slave_stmt="CHANGE MASTER TO MASTER_HOST='database-mysql-member-write',MASTER_USER='member_replication',MASTER_PASSWORD='1234',MASTER_LOG_FILE='$CURRENT_LOG',MASTER_LOG_POS=$CURRENT_POS; START SLAVE;"
start_slave_cmd='export MYSQL_PWD=1234; mysql -u root -e "'
start_slave_cmd+="$start_slave_stmt"
start_slave_cmd+='"'
docker exec database-mysql-member-read sh -c "$start_slave_cmd"

docker exec database-mysql-member-read sh -c "export MYSQL_PWD=1234; mysql -u root -e 'SHOW SLAVE STATUS \G'"

docker exec cache-redis-member-node-1 sh -c 'redis-cli --cluster create cache-redis-member-node-1:6379 cache-redis-member-node-2:6379 cache-redis-member-node-3:6379 cache-redis-member-node-4:6379 cache-redis-member-node-5:6379 cache-redis-member-node-6:6379 --cluster-replicas 1 --cluster-yes'

docker exec cache-redis-member-node-1 sh -c 'redis-cli cluster nodes'