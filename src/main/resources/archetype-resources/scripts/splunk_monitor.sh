#!/bin/bash

# serve as health check for docker container
# 0 -- healthy
# 1 -- unhealthy
# 2 -- reserved, do not use.

if [[ $LOG_BASE = "" ]]; then
    LOG_BASE="/var/www/v_running/logs"
fi
if [[ $APP_BASE = "" ]]; then
    APP_BASE="/var/www/v_running/programs/${artifactId}service"
fi

LOGFILE="${LOG_BASE}/monitor.log.`date +'%Y%m%d'`"

function log()
{
    echo "["`date +'%m/%d/%Y %H:%M:%S'`"] "$@ | tee -a $LOGFILE
}

# REMOVED FOR TESTING if [[ $PROFILE = "prod" ]]; then
    SPLUNK_HOME="/opt/splunk"
    # set hostname in inputs.conf and server.conf

    CONFFILE=$SPLUNK_HOME/etc/system/local/server.conf
    mv ${CONFFILE} ${CONFFILE}.orig
    sed -e "s/^serverName = .*$/serverName = `hostname`/" ${CONFFILE}.orig > ${CONFFILE}
    echo "${CONFFILE} modified"

    CONFFILE=$SPLUNK_HOME/etc/system/local/inputs.conf
    mv ${CONFFILE} ${CONFFILE}.orig
    sed -e "s/^host = .*$/host = `hostname`/" ${CONFFILE}.orig > ${CONFFILE}
    echo "${CONFFILE} modified"
# fi

SLEEP_TIME=1m

while (true); do
	# check if splunkd is running
    SPLUNK_CHECKS=2
# REMOVED FOR TESTING    if [[ $PROFILE = "prod" ]]; then
        chkcnt=0
        while [[ $chkcnt -lt ${SPLUNK_CHECKS} ]]; do
            isRunning=`${SPLUNK_HOME}/bin/splunk status | grep "splunkd is running"`
            if [[ -z "${isRunning}" ]]; then
                log "splunkd is not running, attempting startup of splunkd"
                cp -fp ${APP_BASE}/conf/pushedconf-retrieval.tar.gz ${SPLUNK_HOME}/etc/apps/.
                cd ${SPLUNK_HOME}/etc/apps
                if [[ ! -d inputs_prod_pib_profile_retrieval ]]; then
                    tar xvfz pushedconf-retrieval.tar.gz
                fi
                cd -
                ${SPLUNK_HOME}/bin/splunk set deploy-poll splunkdeploy.services.dowjones.net:8089 -auth admin:changeme
                ${SPLUNK_HOME}/bin/splunk start --accept-license
				sleep 30
				let chkcnt+=1
			else
				log "splunkd is running"
				break;
			fi
		done
		isRunning=`${SPLUNK_HOME}/bin/splunk status | grep "splunkd is running"`
		if [[ $chkcnt -eq ${SPLUNK_CHECKS} && -z "${isRunning}" ]]; then
            log "splunk not starting up. Exitting $0."
            exit 1
        fi
#     else
#         log "$0 exitting - non-prod environment \$PROFILE=${PROFILE}"
#         exit 99 # supervisord will restart for exit code 1. Will ignore this exit code
#     fi
    sleep ${SLEEP_TIME}
done
