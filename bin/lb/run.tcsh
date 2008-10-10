
setenv LD_LIBRARY_PATH /usr/local/lib

limit descriptors 4096

while ( 1 == 1 )
 	if ( -f logs/lb.log.5 ) mv logs/lb.log.5 logs/lb.log.6
 	if ( -f logs/lb.log.4 ) mv logs/lb.log.4 logs/lb.log.5
 	if ( -f logs/lb.log.3 ) mv logs/lb.log.3 logs/lb.log.4
 	if ( -f logs/lb.log.2 ) mv logs/lb.log.2 logs/lb.log.3
 	if ( -f logs/lb.log.1 ) mv logs/lb.log.1 logs/lb.log.2
   	if ( -f logs/lb.log ) mv logs/lb.log logs/lb.log.1
	./runLight.bash -verbose:gc -Xloggc:/tmp/lbgclog -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xmx200m ed.net.lb.LB >& logs/lb.log && exit
end
	
