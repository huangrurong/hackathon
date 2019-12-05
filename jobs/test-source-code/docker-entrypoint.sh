#!/bin/bash

# set -x

pm2 status
pm2 logs > /var/log/hackathon.log &
pm2 start /hackathon.yml

