#include <jni.h>
#include "CmdAgentImpl.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/utsname.h>

int uname(struct utsname *buf);

/*
	@Author Josh Loftus
	Implementation of the command GetLocalOS, uses jni
	and jobjects to reference java functions / object attributes and returns 
	a GetLocalOS object with updated values for valid and os.
*/
JNIEXPORT jobject JNICALL Java_CmdAgentImpl_C_1GetLocalOS(JNIEnv *env, jobject obj, jobject obj2) {
	const char *_class = "GetLocalOS";
	const char *field_os = "os";
	struct utsname buffer;

	uname(&buffer);	
	jclass jcls = (*env)->FindClass(env, _class);
	jobject jobj= (*env)->AllocObject(env, jcls);
	jfieldID fid = (*env)->GetFieldID(env, jcls, field_os, "Ljava/lang/String;");
	jstring jstr = (*env)->GetObjectField(env, jobj, fid);
	
	char *sysname = (char*) malloc(sizeof(char) * 16 + 1);
	strcpy(sysname, buffer.sysname);
	jstr = (*env)->NewStringUTF(env, sysname);
	(*env)->SetObjectField(env, jobj, fid, jstr);

	const char *field_valid = "valid";
	jfieldID fid2 = (*env)->GetFieldID(env, jcls, field_valid, "I");
	int v = 1;
	(*env)->SetIntField(env, jobj, fid2, v);
	return jobj;
}
