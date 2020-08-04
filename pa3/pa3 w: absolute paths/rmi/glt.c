#include <jni.h>
#include "CmdAgentImpl.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

/*
	@Author Josh Loftus
	Implementation of the command GetLocalTime, uses jni
	and jobjects to reference java functions / object attributes and returns 
	a GetLocalTime object with updated values for valid and time.
*/
JNIEXPORT jobject JNICALL Java_CmdAgentImpl_C_1GetLocalTime(JNIEnv *env, jobject obj, jobject glc) {
	const char *_class = "GetLocalTime";
	const char *field_time = "time";
	jclass jcls = (*env)->FindClass(env, _class);
	jobject jobj = (*env)->AllocObject(env, jcls);
	jfieldID fid = (*env)->GetFieldID(env, jcls, field_time, "I");
	int t = time(NULL);
	(*env)->SetIntField(env, jobj, fid, t);

	const char *field_valid = "valid";
	jfieldID fid2 = (*env)->GetFieldID(env, jcls, field_valid, "I");
	int v = 1;
	(*env)->SetIntField(env, jobj, fid2, v);
	return jobj;
}

