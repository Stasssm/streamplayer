# Compile with android-ndk-r10c
APP_ABI := armeabi armeabi-v7a x86 mips
# OR use this to select the latest clang version:
NDK_TOOLCHAIN_VERSION := clang
# then enable c++11 extentions in source code
APP_CPPFLAGS += -std=c++11
APP_PLATFORM := android-15