// DBJni.C

#include "headers/ed_db_DBJni.h"

#include <iostream>

using namespace std;

JNIEXPORT void JNICALL Java_ed_db_DBJni_test1(JNIEnv *, jclass){
  cout << "jni is working" << endl;
}
