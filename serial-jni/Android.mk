#########################################
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= SerialPort.c
LOCAL_MODULE := libSerialPort
LOCAL_LDFLAGS += -fPIC
LOCAL_LDLIBS := -lm -llog
include $(BUILD_SHARED_LIBRARY)