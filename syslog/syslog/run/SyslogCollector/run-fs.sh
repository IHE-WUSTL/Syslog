#!/bin/bash
#*****************************************************
# SyslogCollector run                        R Moulton
# Freestanding (fs) profile
# This directory must be current to run.
#*****************************************************
ROOT=../../
CLASS=gov.nist.syslog.SyslogCollector
CP=${ROOT}dist/SyslogCollector.jar:${ROOT}lib/*
java -cp $CP $CLASS -i SyslogCollector.fs.ini -l log4j.fs.properties &