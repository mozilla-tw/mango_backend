#!/bin/bash
CLOUD_PROXY_CREDENTIAL=$1
CLOUD_DB_NAME=$2
CLOUD_DB_USER=$3
CLOUD_DB_PWD=$4

wget https://dl.google.com/cloudsql/cloud_sql_proxy.linux.amd64 -O cloud_sql_proxy
chmod +x cloud_sql_proxy 
./cloud_sql_proxy -instances=rocket-dev01:us-central1:pubsub=tcp:5432  -credential_file=$CLOUD_PROXY_CREDENTIAL &
apt-get -y update 
pip3 install --upgrade pip  
pip3 install -r integrationTest/servicePytest/requirements.txt
pytest --CLOUD_DB_NAME=$CLOUD_DB_NAME --CLOUD_DB_USER=$CLOUD_DB_USER --CLOUD_DB_PWD=$CLOUD_DB_PWD integrationTest/servicePytest/pushServicePytest.py