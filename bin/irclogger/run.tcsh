
setenv LD_LIBRARY_PATH /usr/local/lib

limit descriptors 4096

while ( 1 == 1 )
	./bin/scripts/irclogger foo irc.freenode.net tengen_logger #10gen
end
	
