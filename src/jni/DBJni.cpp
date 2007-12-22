// DBJni.C

#include "headers/ed_db_DBJni.h"

#include <iostream>
#include <assert.h>
#include <map>

using namespace std;

#include "grid/message.h"
#include "util/goodies.h"

#define CHECK_SA assert( sa ); SockAddr db = ((SockAddr*)sa)[0]; MessagingPort& p = getPort(sa);

map<SockAddr,MessagingPort*> &ports = *(new map<SockAddr,MessagingPort*>);

class DBJniShutdown { 
public:
  ~DBJniShutdown() {
    for( map<SockAddr,MessagingPort*>::iterator i = ports.begin(); i != ports.end(); i++ )
      i->second->shutdown();
  }
} dbjnishutdown;

inline MessagingPort& getPort(jlong sa) { 
  assert(sa);
  SockAddr *addr = (SockAddr*) sa;
  MessagingPort *& mp = ports[*addr];
  if( mp == 0 ) {
    mp = new MessagingPort();
    mp->connect(*addr);
  }
  return *mp;
}

void setData( Message * m , int type , JNIEnv * env , jobject bb , int position , int limit ){
  
  char * start = (char*)env->GetDirectBufferAddress( bb ) + position;
  char * end = (char*)env->GetDirectBufferAddress( bb ) + limit;

  m->setData( type , start , ( end - start ) );
}

JNIEXPORT jlong JNICALL Java_ed_db_DBJni_createSock(JNIEnv * env , jclass, jstring hostJ ){
  const char * host = env->GetStringUTFChars( hostJ , 0 );
  SockAddr * s = new SockAddr( host , DBPort /* + 1  temp dwight dev mode */ );
  env->ReleaseStringUTFChars( hostJ , host );
  return (jlong)s;
}

JNIEXPORT jstring JNICALL Java_ed_db_DBJni_msg(JNIEnv *, jclass , jlong sa ){
  CHECK_SA;
  
  Message send;
  Message response;
  
  send.setData( dbMsg , "ping" );
  
  bool ok = p.call( db , send, response);
  
  return 0;
}

JNIEXPORT void JNICALL Java_ed_db_DBJni_native_insert(JNIEnv * env , jclass, jlong sa  , jobject bb , jint position , jint limit ){
  CHECK_SA
  
  Message send;
  setData( & send , dbInsert , env , bb , position , limit );  

  p.say(db, send );

  return;
}

JNIEXPORT void JNICALL Java_ed_db_DBJni_native_delete(JNIEnv * env , jclass, jlong sa  , jobject bb , jint position , jint limit ){
  CHECK_SA
  
  Message send;
  setData( & send , dbDelete , env , bb , position , limit );  

  p.say(db, send );

  return;
}

JNIEXPORT void JNICALL Java_ed_db_DBJni_native_update(JNIEnv * env , jclass, jlong sa  , jobject bb , jint position , jint limit ){
  CHECK_SA
  
  Message send;
  setData( & send , dbUpdate , env , bb , position , limit );  

  p.say(db, send );

  return;
}


JNIEXPORT jint JNICALL Java_ed_db_DBJni_native_query(JNIEnv * env , jclass, jlong sa , jobject bb , jint position , jint limit , jobject res ){
  CHECK_SA
  
  Message send;
  setData( & send , dbQuery , env , bb , position , limit );

  Message response;
  
  bool ok = p.call(db, send, response);

  assert( env->GetDirectBufferCapacity( res ) >= response.data->len );
  
  memcpy( env->GetDirectBufferAddress( res ) , response.data->_data , response.data->len );
  return response.data->dataLen();
}


JNIEXPORT jint JNICALL Java_ed_db_DBJni_native_getMore(JNIEnv * env , jclass, jlong sa , jobject bb , jint position , jint limit , jobject res ){
  CHECK_SA
  
  Message send;
  setData( & send , dbGetMore , env , bb , position , limit );

  Message response;
  
  bool ok = p.call(db, send, response);

  assert( env->GetDirectBufferCapacity( res ) >= response.data->len );
  
  memcpy( env->GetDirectBufferAddress( res ) , response.data->_data , response.data->len );
  return response.data->dataLen();
}

