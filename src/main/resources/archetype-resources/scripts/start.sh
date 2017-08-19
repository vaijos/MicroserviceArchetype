#!/bin/bash

cd /var/www/v_running/programs/${artifactId}service

LOGDIR="/var/www/v_running/logs"

echo "Starting Splunk Service..."

nohup sh -x ./splunk_monitor.sh > ${LOGDIR}/monitor_nohup.log 2>&1 &

CONFIG="./config/$ENV"

#logback configuration
LOGBACK="-Dlogback.configurationFile=${CONFIG}/logback.xml"

#NewRelic configuration
NEWRELICENV=''
case $ENV in
    int)
        NEWRELICENV='integration'
        ;;
    stag)
        NEWRELICENV='staging'
        ;;
    prod)
        NEWRELICENV='production'
        ;;
    *)
        NEWRELICENV='development'
       ;;
esac
NEW_RELIC="-javaagent:./newrelic/newrelic.jar -Dnewrelic.config.file=${CONFIG}/newrelic.yml -Dnewrelic.environment=${NEWRELICENV}"

#JVM Settings
JVM_ARGS="${LOGBACK} ${NEW_RELIC}"

echo ${JVM_ARGS}
java ${JVM_ARGS} -jar ${artifactId}.jar -e $ENV -d $DOCKERIMG -i $INSTANCEID
echo "Starting ${artifactId} Service..."
