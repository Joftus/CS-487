#include <jni.h>
#include "CmdAgentImpl.h"
#include <stdio.h>
#include <stdlib.h>
#include <sys/utsname.h>
#include <string.h>

int uname(struct utsname *buf);

/*
	@Author Josh Loftus
	*Note* might return incomplete value, I was unsure how to convert
		version: 19.* to an int like assignment specs required. I decided
		to just use the most significant value and return the first 2 values
		returned by utsname (i.e. returns "19")
	Implementation of the command GetVersion, uses jni
	and jobjects to reference java functions / object attributes and returns 
	a GetVersion object with updated values for valid and version.
*/
JNIEXPORT jobject JNICALL Java_CmdAgentImpl_C_1GetVersion(JNIEnv *env, jobject obj, jobject obj2) {
	const char *_class = "GetVersion";
	const char *field_version = "version";
	struct utsname buffer;

	jclass jcls = (*env)->FindClass(env, _class);
	jobject jobj= (*env)->AllocObject(env, jcls);
	jfieldID fid = (*env)->GetFieldID(env, jcls, field_version, "I");
	uname(&buffer);
	char* ver_str = (char*) malloc((sizeof(char) * 2) + 1);
	ver_str[0] = buffer.release[0];
	ver_str[1] = buffer.release[1];
	int version = atoi(ver_str);
	(*env)->SetIntField(env, jobj, fid, version);

	const char *field_valid = "valid";
	jfieldID fid2 = (*env)->GetFieldID(env, jcls, field_valid, "I");
	int v = 1;
	(*env)->SetIntField(env, jobj, fid2, v);
	return jobj;
}

