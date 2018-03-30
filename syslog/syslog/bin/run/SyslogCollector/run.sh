#!/bin/bash
#*****************************************************
# SyslogCollector run                        R Moulton
# Production profile
# This directory must be current to run.
#*****************************************************
ROOT=../../
CLASS=gov.nist.syslog.SyslogCollector
CP=${ROOT}dist/SyslogCollector.jar:${ROOT}lib/*
java -cp $CP $CLASS &