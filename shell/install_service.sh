#!/bin/bash
bash -c 'cd /opt/greenwich-experiment/shell/ && ln -s greenwich /etc/init.d/greenwich'
bash -c 'update-rc.d greenwich defaults'
