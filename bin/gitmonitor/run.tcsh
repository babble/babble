
setenv LD_LIBRARY_PATH /usr/local/lib

limit descriptors 4096

while ( 1 == 1 )
	./runLight.bash ed.cloud.GitMonitor >& logs/gitrun.log
end
	
