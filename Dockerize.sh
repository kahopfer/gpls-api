#!/usr/bin/env bash
mvn clean package
docker build -t gpls-api:v0.1.0 .
docker run --restart=always --name gpls-api -p 8080:8080 -d gpls-api:v0.1.0