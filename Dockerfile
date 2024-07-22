FROM tomcat:latest
RUN rm -rf /usr/local/tomcat/webapps/*
ADD target/*.war /usr/local/tomcat/webapps/ROOT.war
ADD server.xml /usr/local/tomcat/conf/server.xml
ADD keystore.p12 /usr/local/tomcat/conf/
EXPOSE 8080
EXPOSE 8443
CMD ["catalina.sh", "run"]
