
setenv LD_LIBRARY_PATH /usr/local/lib

limit descriptors 4096

while ( 1 == 1 )
	if ( -f log/run.log ) mv log/run.log log/run.log.1
	./runLight.bash ed.appserver.AppServer /data/sites/www >& log/run.log
end
	
