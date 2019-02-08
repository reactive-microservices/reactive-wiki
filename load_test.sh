#!/usr/bin/env bash

wrk -t10 -c100 -d30s --latency http://localhost:8080/wiki
