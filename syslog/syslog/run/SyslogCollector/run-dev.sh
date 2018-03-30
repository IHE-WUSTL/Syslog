#!/bin/bash
#*****************************************************
# SyslogCollector run                        R Moulton
# Development (dev) profile
# This directory must be current to run.
#*****************************************************
ROOT=../../
CLASS=gov.nist.syslog.SyslogCollector
CP=${ROOT}dist/SyslogCollector.jar:${ROOT}lib/*
java -cp $CP $CLASS -i SyslogCollector.dev.ini -l log4j.dev.properties &