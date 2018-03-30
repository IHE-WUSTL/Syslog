#!/bin/bash
#*****************************************************
# SyslogSender run script                    R Moulton
# This directory must be current to run
#*****************************************************
ROOT=../../
CLASS=gov.nist.syslog.SyslogSender
CP=${ROOT}dist/SyslogCollector.jar:${ROOT}lib/*
java -cp $CP $CLASS -l log4j.dev.properties -s 127.0.0.1 -p 3201 -x TLS &