#!/usr/bin/env bash
set -e

cd /home/ubuntu/smashing

aws ecr get-login-password --region ap-northeast-2 \
  | docker login --username AWS --password-stdin 234506497939.dkr.ecr.ap-northeast-2.amazonaws.com

docker compose \
  -f docker/docker-compose.base.yml \
  -f docker/docker-compose.dev.yml \
  --env-file .env.dev \
  pull

docker compose \
  -f docker/docker-compose.base.yml \
  -f docker/docker-compose.dev.yml \
  --env-file .env.dev \
  up -d

docker image prune -f
