
setenv LD_LIBRARY_PATH /usr/local/lib

limit descriptors 4096

while ( 1 == 1 )
 	if ( -f logs/run.log.5 ) mv logs/run.log.5 logs/run.log.6
 	if ( -f logs/run.log.4 ) mv logs/run.log.4 logs/run.log.5
 	if ( -f logs/run.log.3 ) mv logs/run.log.3 logs/run.log.4
 	if ( -f logs/run.log.2 ) mv logs/run.log.2 logs/run.log.3
 	if ( -f logs/run.log.1 ) mv logs/run.log.1 logs/run.log.2
   	if ( -f logs/run.log ) mv logs/run.log logs/run.log.1
	./runLight.bash -verbose:gc -Xloggc:/tmp/gclog -XX:+PrintGCDetails -XX:+PrintGCTimeStamps $JVM_WWW_EXTRA ed.appserver.AppServer /data/sites/www/www >& logs/run.log
end
	
