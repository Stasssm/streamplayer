LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CFLAGS := -g
LOCAL_MODULE := mp3decoder
LOCAL_SHARED_LIBRARIES := libavcodec libavcore libavdevice libavfilter libavformat libavutil libswscale
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../ffmpeg/ffmpeg/$(TARGET_ARCH_ABI)/include
LOCAL_SRC_FILES := mp3decoder.c
#LOCAL_LDLIBS    := -L$(LOCAL_PATH)/../bffmpeg -lavcodec -lavcore -lavdevice -lavfilter -lavformat -lavutil -lswscale -llog -lz -lGLESv1_CM
# for native audio
LOCAL_LDLIBS += -lOpenSLES
# for logging
LOCAL_LDLIBS += -llog
include $(BUILD_SHARED_LIBRARY)

