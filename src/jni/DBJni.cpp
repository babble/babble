// DBJni.C

#include "headers/ed_db_DBJni.h"

#include <iostream>
#include <assert.h>

using namespace std;

#include "grid/message.h"

SockAddr db("10.0.21.60", MessagingPort::DBPort);

JNIEXPORT jstring JNICALL Java_ed_db_DBJni_msg(JNIEnv *, jclass){
  MessagingPort p;
  p.init(29999);
  
  Message send;
  Message response;
  
  send.setData( dbMsg , "ping" );
  
  cout << "contacting DB..." << endl;
  bool ok = p.call(db, send, response);
  cout << "ok: " << ok << endl;
  cout << "  " << response.data->id << endl;
  cout << "  " << response.data->len << endl;
  cout << "  " << response.data->operation << endl;
  cout << "  " << response.data->reserved << endl;
  cout << "  " << response.data->responseTo << endl;
  cout << "  " << response.data->_data << endl;
  
  return 0;
}

JNIEXPORT void JNICALL Java_ed_db_DBJni_insert(JNIEnv * env , jclass, jobject bb , jint position , jint limit ){
  
  char * start = (char*)env->GetDirectBufferAddress( bb ) + position;
  char * end = (char*)env->GetDirectBufferAddress( bb ) + limit;

  MessagingPort p;
  p.init(29999);
  
  Message send;
  
  send.setData( dbInsert , start , ( end - start ) );

  cout << "contacting DB for insert..." << endl;
  p.say(db, send );

  return;
}


JNIEXPORT int JNICALL Java_ed_db_DBJni_query(JNIEnv * env , jclass, jobject bb , jint position , jint limit , jobject res ){
  
  char * start = (char*)env->GetDirectBufferAddress( bb ) + position;
  char * end = (char*)env->GetDirectBufferAddress( bb ) + limit;

  MessagingPort p;
  p.init(29999);
  
  Message send;
  
  send.setData( dbQuery , start , ( end - start ) );

  Message response;
  
  cout << "contacting DB for query..." << endl;
  bool ok = p.call(db, send, response);
  cout << "ok: " << ok << endl;
  cout << "  " << response.data->id << endl;
  cout << "  " << response.data->len << endl;
  cout << "  " << response.data->operation << endl;
  cout << "  " << response.data->reserved << endl;
  cout << "  " << response.data->responseTo << endl;
  cout << "  " << response.data->_data << endl;

  assert( env->GetDirectBufferCapacity( res ) >= response.data->len );
  
  memcpy( env->GetDirectBufferAddress( res ) , response.data->_data , response.data->len );

  return response.data->dataLen();
}
