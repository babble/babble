/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class ed_db_DBJni */

#ifndef _Included_ed_db_DBJni
#define _Included_ed_db_DBJni
#ifdef __cplusplus
extern "C" {
#endif
#undef ed_db_DBJni_D
#define ed_db_DBJni_D 0L
/* Inaccessible static: _ipToSockAddr */
/* Inaccessible static: EMPTY */
/* Inaccessible static: _defaultIp */
/*
 * Class:     ed_db_DBJni
 * Method:    createSock
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_ed_db_DBJni_createSock
  (JNIEnv *, jclass, jstring);

/*
 * Class:     ed_db_DBJni
 * Method:    msg
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_ed_db_DBJni_msg
  (JNIEnv *, jclass, jlong);

/*
 * Class:     ed_db_DBJni
 * Method:    insert
 * Signature: (JLjava/nio/ByteBuffer;II)V
 */
JNIEXPORT void JNICALL Java_ed_db_DBJni_insert
  (JNIEnv *, jclass, jlong, jobject, jint, jint);

/*
 * Class:     ed_db_DBJni
 * Method:    doDelete
 * Signature: (JLjava/nio/ByteBuffer;II)V
 */
JNIEXPORT void JNICALL Java_ed_db_DBJni_doDelete
  (JNIEnv *, jclass, jlong, jobject, jint, jint);

/*
 * Class:     ed_db_DBJni
 * Method:    doUpdate
 * Signature: (JLjava/nio/ByteBuffer;II)V
 */
JNIEXPORT void JNICALL Java_ed_db_DBJni_doUpdate
  (JNIEnv *, jclass, jlong, jobject, jint, jint);

/*
 * Class:     ed_db_DBJni
 * Method:    query
 * Signature: (JLjava/nio/ByteBuffer;IILjava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_ed_db_DBJni_query
  (JNIEnv *, jclass, jlong, jobject, jint, jint, jobject);

#ifdef __cplusplus
}
#endif
#endif
