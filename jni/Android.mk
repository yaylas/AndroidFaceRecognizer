LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
include ../OpenCV-2.4.6-android-sdk/sdk/native/jni/OpenCV.mk
LOCAL_SRC_FILES  := DetectionAndRecognition.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl

LOCAL_MODULE     := detection_and_recognition_lib

include $(BUILD_SHARED_LIBRARY)
