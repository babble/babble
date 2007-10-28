// DBJni.C

#include "headers/ed_db_DBJni.h"

#include <iostream>
#include <assert.h>

using namespace std;

#include "grid/message.h"

/*
#include "stdafx.h"
#include "db.h"
#include "util/mmap.h"
#include "util/hashtab.h"
#include "pdfile.h"
#include "jsobj.h"
#include "query.h"
*/




JNIEXPORT jstring JNICALL Java_ed_db_DBJni_msg(JNIEnv *, jclass){
  MessagingPort p;
  p.init(29999);
  
  SockAddr db("127.0.0.1", MessagingPort::DBPort);
  
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

  for ( char * c = start; c<end; c++){
    cout << (int)c[0] << endl;
  }
  cout << "---\n" ;

  *end = 0;
  
  uint * len = (uint*)(start + 2);
  cout << "len:" << *len << endl;

  cout << ( start + 7 ) << endl;
  cout << ( start + 7 )[0] << endl;
  cout << ( start + 8 )[0] << endl;
  cout << (int)( start + 9 )[0] << endl;

  double * d = (double*)(start+10);
  
  cout << *d << endl;

  MessagingPort p;
  p.init(29999);
  
  SockAddr db("10.0.21.60", MessagingPort::DBPort);
  
  Message send;
  Message response;
  
  send.setData( dbInsert , start , ( end - start ) );

  cout << "contacting DB..." << endl;
  bool ok = p.call(db, send, response);
  cout << "ok: " << ok << endl;
  cout << "  " << response.data->id << endl;
  cout << "  " << response.data->len << endl;
  cout << "  " << response.data->operation << endl;
  cout << "  " << response.data->reserved << endl;
  cout << "  " << response.data->responseTo << endl;
  cout << "  " << response.data->_data << endl;

  return;
}
