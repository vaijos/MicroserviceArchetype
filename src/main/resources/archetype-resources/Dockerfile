FROM artifactory.dowjones.io:5000/djin/java8
MAINTAINER Team Perseus <Teamperseus@dowjones.com>

ARG version=0.0.1
ARG iteration=1

# SPLUNK
ENV SPLUNK_HOME /opt/splunk

ENV progDir /var/www/v_running/programs/${artifactId}service
ENV configDir /var/www/v_running/programs/${artifactId}service/config
ENV logsDir /var/www/v_running/logs

#Note :- Change the below environment value to dev/prod accordingly

RUN mkdir -p ${progDir}
RUN mkdir -p ${configDir}
RUN mkdir -p ${logsDir}

# RUN chown -R appuser:appuser /var/www/*
# USER appuser
# SPLUNK
RUN chmod -R 777 $SPLUNK_HOME

#Copy service jar files
COPY target/${artifactId}Service-${version}-jar-with-dependencies.jar  ${progDir}/${artifactId}.jar


#Copy release environment specific configurations - dev/qa/int/prod
COPY scripts/*.sh ${progDir}/
COPY config/ ${configDir}/

RUN chmod 777 ${progDir}/*.sh

WORKDIR ${progDir}

RUN jar xf ${artifactId}.jar newrelic/newrelic.jar

EXPOSE 4567
EXPOSE 8089

# Configure Splunk Agent on the client
COPY config/deploymentclient.conf $SPLUNK_HOME/etc/system/local/deploymentclient.conf
RUN ${SPLUNK_HOME}/bin/splunk edit licenser-groups Forwarder -is_active 1 -auth admin:changeme
RUN ${SPLUNK_HOME}/bin/splunk disable webserver

# make the start script a default command­
CMD [ "./start.sh" ]

#ENTRYPOINT ["./start.sh"]
