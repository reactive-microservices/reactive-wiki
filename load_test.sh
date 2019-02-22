#!/usr/bin/env bash

wrk -t10 -c100 -d30s --latency http://192.168.99.100:31195/wiki

