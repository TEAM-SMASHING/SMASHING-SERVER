#!/usr/bin/env bash
set -e

cd /home/ubuntu/smashing

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
