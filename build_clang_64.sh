#!/bin/bash
PREFIX=`pwd`/out/aarch64
#设置ndk目录
NDK=/home/zwx556335/tools/ndk/android-ndk-r20
#ar nm 的prefix
PLATFORM=aarch64-linux-android
#llvm toolchain路径
TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/linux-x86_64
#sysroot 这个一定要设置成 ndk的llvm 路径下的 sysroot
SYSROOT=$TOOLCHAIN/sysroot
#ASM 路径， 同上必须是llvm 目录下的 asm
ASM=$SYSROOT/usr/include/$PLATFORM
ARCH=aarch64
CPU=armv8-a
#完整的 cross prefix
CROSS_PREFIX=$TOOLCHAIN/bin/$PLATFORM-
#专门给ndk clang/clang++ 的 cross prefix
ANDROID_CROSS_PREFIX=$TOOLCHAIN/bin/aarch64-linux-android29-

./configure \
--prefix=$PREFIX \
--disable-x86asm \
--enable-shared \
--disable-static \
--disable-doc \
--arch=$ARCH \
--cpu=$CPU \
--disable-ffmpeg \
--disable-ffplay \
--disable-ffprobe \
--disable-avdevice \
--disable-symver \
--cross-prefix=$CROSS_PREFIX \
--cc=${ANDROID_CROSS_PREFIX}clang \
--target-os=android \
--enable-cross-compile \
--sysroot=$SYSROOT \
--extra-cflags="-I$ASM -isysroot $SYSROOT -Os -fpic"

