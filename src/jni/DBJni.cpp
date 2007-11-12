// DBJni.C

#include "headers/ed_db_DBJni.h"

#include <iostream>
#include <assert.h>

using namespace std;

#include "grid/message.h"

#define CHECK_SA assert( sa ); SockAddr db = ((SockAddr*)sa)[0];

void setData( Message * m , int type , JNIEnv * env , jobject bb , int position , int limit ){
  
  char * start = (char*)env->GetDirectBufferAddress( bb ) + position;
  char * end = (char*)env->GetDirectBufferAddress( bb ) + limit;

  m->setData( type , start , ( end - start ) );
}

JNIEXPORT jlong JNICALL Java_ed_db_DBJni_createSock(JNIEnv * env , jclass, jstring hostJ ){
  const char * host = env->GetStringUTFChars( hostJ , 0 );
  SockAddr * s = new SockAddr( host , MessagingPort::DBPort );
  env->ReleaseStringUTFChars( hostJ , host );
  return (jlong)s;
}

JNIEXPORT jstring JNICALL Java_ed_db_DBJni_msg(JNIEnv *, jclass , jlong sa ){
  CHECK_SA;
  MessagingPort p;
  p.init(29999);
  
  Message send;
  Message response;
  
  send.setData( dbMsg , "ping" );
  
  bool ok = p.call( db , send, response);
  
  return 0;
}

JNIEXPORT void JNICALL Java_ed_db_DBJni_insert(JNIEnv * env , jclass, jlong sa  , jobject bb , jint position , jint limit ){
  CHECK_SA;
  
  MessagingPort p;
  p.init(29999);
  
  Message send;
  setData( & send , dbInsert , env , bb , position , limit );  

  p.say(db, send );

  return;
}

JNIEXPORT void JNICALL Java_ed_db_DBJni_doDelete(JNIEnv * env , jclass, jlong sa  , jobject bb , jint position , jint limit ){
  CHECK_SA;
  
  MessagingPort p;
  p.init(29999);
  
  Message send;
  setData( & send , dbDelete , env , bb , position , limit );  

  p.say(db, send );

  return;
}

JNIEXPORT void JNICALL Java_ed_db_DBJni_doUpdate(JNIEnv * env , jclass, jlong sa  , jobject bb , jint position , jint limit ){
  CHECK_SA;
  
  MessagingPort p;
  p.init(29999);
  
  Message send;
  setData( & send , dbUpdate , env , bb , position , limit );  

  p.say(db, send );

  return;
}


JNIEXPORT jint JNICALL Java_ed_db_DBJni_query(JNIEnv * env , jclass, jlong sa , jobject bb , jint position , jint limit , jobject res ){
  CHECK_SA;
  
  MessagingPort p;
  p.init(29999);
  
  Message send;
  setData( & send , dbQuery , env , bb , position , limit );

  Message response;
  
  bool ok = p.call(db, send, response);

  assert( env->GetDirectBufferCapacity( res ) >= response.data->len );
  
  memcpy( env->GetDirectBufferAddress( res ) , response.data->_data , response.data->len );
  return response.data->dataLen();
}


JNIEXPORT jint JNICALL Java_ed_db_DBJni_getMore(JNIEnv * env , jclass, jlong sa , jobject bb , jint position , jint limit , jobject res ){
  CHECK_SA;
  
  MessagingPort p;
  p.init(29999);
  
  Message send;
  setData( & send , dbGetMore , env , bb , position , limit );

  Message response;
  
  bool ok = p.call(db, send, response);

  assert( env->GetDirectBufferCapacity( res ) >= response.data->len );
  
  memcpy( env->GetDirectBufferAddress( res ) , response.data->_data , response.data->len );
  return response.data->dataLen();
}

