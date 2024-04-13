#!/bin/bash

docker-compose down -v

rm -rf ./database/mysql/member/write/data/*
rm -rf ./database/mysql/member/read/data/*

rm -rf ./cache/redis/node-1/data/*
rm -rf ./cache/redis/node-2/data/*
rm -rf ./cache/redis/node-3/data/*
rm -rf ./cache/redis/node-4/data/*
rm -rf ./cache/redis/node-5/data/*
rm -rf ./cache/redis/node-6/data/*

docker compose rm