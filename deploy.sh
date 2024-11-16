#!/bin/bash
set -e

# 停止旧的容器
echo "Stopping old container..."
docker-compose down || true

# 启动新的容器
echo "Starting new container..."
docker-compose up -d

# 检查容器状态
echo "Verifying container health..."
if ! docker ps --filter "name=nginx-server" --filter "status=running" | grep -q nginx-server; then
  echo "Container 'nginx-server' failed to start."
  docker logs nginx-server
  exit 1
fi
echo "Deployment succeeded. Container is healthy."
