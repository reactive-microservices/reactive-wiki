#!/usr/bin/env bash

wrk -t8 -c16 -d30s --latency http://localhost:8080/wiki
