
setenv LD_LIBRARY_PATH /usr/local/lib

limit descriptors 4096

while ( 1 == 1 )
 	if ( -f logs/manager.log.5 ) mv logs/manager.log.5 logs/manager.log.6
 	if ( -f logs/manager.log.4 ) mv logs/manager.log.4 logs/manager.log.5
 	if ( -f logs/manager.log.3 ) mv logs/manager.log.3 logs/manager.log.4
 	if ( -f logs/manager.log.2 ) mv logs/manager.log.2 logs/manager.log.3
 	if ( -f logs/manager.log.1 ) mv logs/manager.log.1 logs/manager.log.2
   	if ( -f logs/manager.log ) mv logs/manager.log logs/manager.log.1
	./runLight.bash -verbose:gc -Xloggc:/tmp/managergclog -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xmx200m ed.manager.Manager >& logs/manager.log 
end
	
