#!/bin/bash
#*****************************************************
# SyslogSender run script                    R Moulton
# Production
# This directory must be current to run
#*****************************************************
ROOT=../../
CLASS=gov.nist.syslog.SyslogSender
CP=${ROOT}dist/SyslogCollector.jar:${ROOT}lib/*
java -cp $CP $CLASS &