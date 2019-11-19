#!/bin/bash
PREFIX=`pwd`/out/armeabi-v7a
#设置ndk目录
NDK=/home/zwx556335/tools/ndk/android-ndk-r20
#ar nm 的prefix
PLATFORM=arm-linux-androideabi
#llvm toolchain路径
TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/linux-x86_64
#sysroot 这个一定要设置成 ndk的llvm 路径下的 sysroot
SYSROOT=$TOOLCHAIN/sysroot
#ASM 路径， 同上必须是llvm 目录下的 asm
ASM=$SYSROOT/usr/include/$PLATFORM
#完整的 cross prefix
CROSS_PREFIX=$TOOLCHAIN/bin/$PLATFORM-
#专门给ndk clang/clang++ 的 cross prefix
ANDROID_CROSS_PREFIX=$TOOLCHAIN/bin/armv7a-linux-androideabi28-

./configure \
--prefix=$PREFIX \
--enable-shared \
--disable-static \
--disable-doc \
--disable-ffmpeg \
--disable-ffplay \
--disable-ffprobe \
--disable-avdevice \
--disable-doc \
--disable-symver \
--cross-prefix=$CROSS_PREFIX \
--cc=${ANDROID_CROSS_PREFIX}clang \
--target-os=android \
--arch=arm \
--enable-cross-compile \
--sysroot=$SYSROOT \
--extra-cflags="-I$ASM -isysroot $SYSROOT -Os -fpic"

