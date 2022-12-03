#!/bin/bash
bash -c 'rm /etc/init.d/greenwich'
bash -c 'chmod +x /opt/greenwich-experiment/shell/greenwich'
bash -c 'ln -s /opt/greenwich-experiment/shell/greenwich /etc/init.d/greenwich'
bash -c 'update-rc.d greenwich defaults'
