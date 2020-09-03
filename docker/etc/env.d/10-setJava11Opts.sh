#!/bin/bash

# Set java options to allow JVM to use most of the available container memory
# see https://dzone.com/articles/jvm-advent-calendar-docker-and-the-jvm

# Set initial / max percentage
INITIAL_PERCENTAGE=75
MAX_PERCENTAGE=90

if [ -n "${JAVA_INITIAL_RAM_PERCENTAGE}" ]; then
  INITIAL_PERCENTAGE=${JAVA_INITIAL_RAM_PERCENTAGE}
fi

if [ -n "${JAVA_MAX_RAM_PERCENTAGE}" ]; then
  MAX_PERCENTAGE=${JAVA_MAX_RAM_PERCENTAGE}
fi

_JAVA_OPTIONS="${_JAVA_OPTIONS} -XX:-UseContainerSupport -XX:InitialRAMPercentage=${INITIAL_PERCENTAGE} -XX:MaxRAMPercentage=${MAX_PERCENTAGE}"

export _JAVA_OPTIONS
