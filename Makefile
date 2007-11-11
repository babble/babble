
all: jni

INCS=-I src/p/boost -I/sw/include

include src/p/db/makefile

DB_CORE_OBJ = $(foreach o,$(OBJS),src/p/db/$o)

build:
	-mkdir build

clean: 
	-rm $(DB_CORE_OBJ)
	-rm src/jni/DBJni.o

jni: build $(DB_CORE_OBJ) src/jni/DBJni.o
	g++ -shared $(DB_CORE_OBJ) $(LIBS) src/jni/DBJni.o -o build/libdb.so

jnimac: build $(DB_CORE_OBJ) src/jni/DBJni.o
	g++ -dynamiclib -single_module $(DB_CORE_OBJ) $(LIBS) src/jni/DBJni.o -o build/libdb.jnilib
