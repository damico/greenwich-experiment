#!/bin/bash
/usr/bin/java -jar /opt/greenwich-experiment/java/greenwich/target/greenwich-0.0.1-SNAPSHOT-jar-with-dependencies.jar localhost 2947 &
/usr/bin/python3 /opt/greenwich-experiment/python/lcd_ui.py &
exit 0
