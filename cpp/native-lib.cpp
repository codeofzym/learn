#include <jni.h>
#include <android/log.h>
#include <android/native_window_jni.h>
#include <android/native_window.h>
#include <android/bitmap.h>

#define TAG_LOG "JNI_FFMPEG"
#define LOGI( ... ) __android_log_print(ANDROID_LOG_INFO, TAG_LOG, __VA_ARGS__)
#define LOGE( ... ) __android_log_print(ANDROID_LOG_ERROR, TAG_LOG, __VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif
#include <libavformat/avformat.h>
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
#include <libavcodec/avcodec.h>

static  ANativeWindow* window = NULL;
static jint zLeft = 0;
static jint zTop = 0;

static void drawMark(JNIEnv *env, jobject thiz, ANativeWindow_Buffer *buffer) {
    if(window == NULL) {
        LOGE("set_bitmap_bytes window is null");
        return;
    }
    LOGI("left[%d] top[%d]", zLeft, zTop);

    if(buffer == NULL) {
        LOGE("set_bitmap_bytes buffer is null");
        return;
    }
    jclass  zPlayer = env->GetObjectClass(thiz);
    jfieldID id = env->GetFieldID(zPlayer, "mBitmap", "Landroid/graphics/Bitmap;");
    jobject  bitmap = env->GetObjectField(thiz, id);
    int result = 0;
    AndroidBitmapInfo info;
    LOGI("111111");
    result = AndroidBitmap_getInfo(env, bitmap, &info);
    LOGI("222222");
    LOGI("get info result[%d]", result);
    LOGI("get info width[%d] height[%d]", info.width, info.height);
    if(result != 0) {
        LOGE("getinfo error");
        return;
    }

    void *pixs;
    result = AndroidBitmap_lockPixels(env, bitmap, &pixs);
    uint8_t *bits = (uint8_t *)(*buffer).bits;
    for (int i = zTop; i < zTop + info.height; i++) {
        memcpy(bits + i * (*buffer).stride * 4 + zLeft * 4,
               (uint8_t *)pixs + (i - zTop) * info.stride, info.stride);
    }
    AndroidBitmap_unlockPixels(env, bitmap);
}

static jstring get_test_string(JNIEnv *env, jobject obj) {
    return env->NewStringUTF("Hello From Native");
}

static void set_data_source(JNIEnv *env, jobject thiz, jstring path) {
    int result = 0;
    av_register_all();
    //Java字符串输入变量转为C的char*变量
    const char *input = (*env).GetStringUTFChars(path, NULL);
    LOGI("%s", input);
    //获取解码的上下文
    AVFormatContext* avFormatContext = avformat_alloc_context();
    avformat_open_input(&avFormatContext, input, NULL, NULL);
    avformat_find_stream_info(avFormatContext, NULL);

    LOGI("duration[%d]", avFormatContext->duration / AV_TIME_BASE);
    int videoIndex;
    for (int i = 0; i < avFormatContext->nb_streams; i++) {
        if(avFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoIndex = i;
        }
    }
    LOGI("videoIndex[%d]", videoIndex);

//    AVCodecContext *avCodecContext = avFormatContext->streams[videoIndex]->codec;
    AVCodecContext *avCodecContext = avcodec_alloc_context3(NULL);
    result = avcodec_parameters_to_context(avCodecContext, avFormatContext->streams[videoIndex]->codecpar);
    LOGI("avcodec_parameters_to_context[%d]", result);
    LOGI("codec_id[%d]", avCodecContext->codec_id);
    AVCodec *avCodec = avcodec_find_decoder(avCodecContext->codec_id);
    LOGI("AVCodec name[%s]", avCodec->name);

    result = avcodec_open2(avCodecContext, avCodec, NULL);
    LOGI("avcodec_open2 result[%d]", result);
    LOGI("AVCodec name[%s]", avCodec->name);
    LOGI("width[%d] height[%d]", avCodecContext->width, avCodecContext->height);
    int width = avCodecContext->width;
    int height = avCodecContext->height;

    LOGI("ANDROID_API[%d]", __ANDROID_API__);

    if(window == NULL) {
        LOGE("window == NULL");
        return;
    }

    result = ANativeWindow_setBuffersGeometry(window, width, height, WINDOW_FORMAT_RGBA_8888);
    //定义绘图缓冲区
    ANativeWindow_Buffer windowBuffer;
    //定义数据容器 3个
    //R5解码前数据容器Packet编码数据
    AVPacket *packet = av_packet_alloc();
    av_init_packet(packet);
    //R6解码后的数据容器Frame 像素数据 不能直接播放像素数据 还需要转换
    AVFrame *frame = av_frame_alloc();
    //R7转换后的数据容器 此数据用与播放
    AVFrame *rgb_frame = av_frame_alloc();
    //数据格式转换
    //输出Buffer
    int buffer_size = av_image_get_buffer_size(AV_PIX_FMT_RGBA, width, height, 1);

    uint8_t* rgbBuffer = (uint8_t *) av_malloc (buffer_size * sizeof(uint8_t));
    av_image_fill_arrays(rgb_frame->data, rgb_frame->linesize, rgbBuffer, AV_PIX_FMT_RGBA, width, height, 1);
    LOGI("1");
    struct SwsContext *swsContext = sws_getContext(width,height, avCodecContext->pix_fmt,
                                                   width,height, AV_PIX_FMT_RGBA, SWS_BICUBIC, NULL, NULL, NULL);
    //开始读取一帧画面
    while (av_read_frame(avFormatContext, packet) >= 0) {
        //匹配视频流
        if(packet->stream_index == videoIndex) {
            result = avcodec_send_packet(avCodecContext, packet);
            if(result < 0 && result != AVERROR(EAGAIN) && result != AVERROR_EOF) {
                LOGE("send player error[%d]", result);
                return;
            }

            result = avcodec_receive_frame(avCodecContext, frame);
            if(result == AVERROR(EAGAIN)) {
                continue;
            }
            if(result < 0 && result != AVERROR_EOF) {
                LOGE("recevie player error[%d]", result);
                return;
            }
            //数据格式转换
            result = sws_scale(swsContext, (const uint8_t* const*)frame->data, frame->linesize, 0,
                               height, rgb_frame->data, rgb_frame->linesize);
            //绘制画面到surface上
            result = ANativeWindow_lock(window, &windowBuffer, NULL);
            if(result < 0) {
                LOGE("player error[%d]", result);
            } else {
                // 将图像绘制到界面上
                // 注意 : 这里 rgba_frame 一行的像素和 window_buffer 一行的像素长度可能不一致
                // 需要转换好 否则可能花屏
                uint8_t *bits = (uint8_t *)windowBuffer.bits;
                const uint8_t *src = rgb_frame->data[0];
                for (int h = 0; h < height; h++) {
                    memcpy(bits + h * windowBuffer.stride * 4,
                           rgbBuffer + h * rgb_frame->linesize[0],
                           rgb_frame->linesize[0]);
                }
                drawMark(env, thiz, &windowBuffer);
            }
            ANativeWindow_unlockAndPost(window);
        }
    }
    av_packet_unref(packet);
    sws_freeContext(swsContext);
    av_free(rgbBuffer);
    av_frame_free(&rgb_frame);
    av_frame_free(&frame);
    av_packet_free(&packet);
    avcodec_free_context(&avCodecContext);
    avformat_free_context(avFormatContext);

    ANativeWindow_release(window);
}

static void draw_surface() {
    if(window == NULL) {
        LOGE("draw_surface error window is null");
        return;
    }
    int result = 0;
    ANativeWindow_Buffer buffer;
    result = ANativeWindow_lock(window, &buffer, NULL);
    const int bufSize = 20*20;
    char buf[bufSize] = {0};
    memset(buf, 0x77, bufSize);
    LOGI("width[%d] height[%d] stride[%d]", buffer.width, buffer.height, buffer.stride);
    for (int i = 0; i < bufSize; i++) {
        memcpy((uint8_t *)buffer.bits + i * buffer.stride * 4, buf, bufSize);
    }
    ANativeWindow_unlockAndPost(window);


}

static void set_surface(JNIEnv *env, jobject thiz, jobject jsurface) {
    window = ANativeWindow_fromSurface(env, jsurface);
//    draw_surface();
}

static void start(JNIEnv *env, jobject thiz) {

}

static void set_mark_bitmap(JNIEnv *env, jobject thiz, jint left, jint top, jobject bitmap) {
    zLeft = left;
    zTop = top;
//    ANativeWindow_Buffer buffer;
//    ANativeWindow_lock(window, &buffer, NULL);
//    drawMark(env, thiz, &buffer);
//    ANativeWindow_unlockAndPost(window);
}

#ifdef __cplusplus
};
#endif

static JNINativeMethod sMethod[] = {
    //Java中的Native方法名
    {"getTestString", "()Ljava/lang/String;", (void*)get_test_string},
    {"setDataSourceNative", "(Ljava/lang/String;)V",(void *)set_data_source},
    {"setSurfaceNative", "(Landroid/view/Surface;)V",(void *)set_surface},
    {"setMarkBitmapNative", "(IILandroid/graphics/Bitmap;)V",(void *)set_mark_bitmap},
    {"startNative", "()V",(void *)start},
};

static int registerNativesMethods(JNIEnv* env, const char* className, JNINativeMethod* method,
        int methodNums) {
    jclass clazz = NULL;
    //找到定义native方法的Java类
    clazz = env->FindClass(className);
    if(clazz == NULL) {
        return JNI_FALSE;
    }

    //调用JNI的注册方法，将Java中的方法和C/C++方法对应上
    if(env->RegisterNatives(clazz, method, methodNums) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;

}

static int registerNatives(JNIEnv* env) {
    const char* className = "com.zym.learn.ffmpeg.ZMediaPlayer";
    return registerNativesMethods(env, className, sMethod, sizeof(sMethod)/ sizeof(sMethod[0]));
}

/**
*回调函数，Java调用System.loadLibrary方法后执行
*/
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    //判断虚拟机状态是否OK
    if(vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    //注册函数调用
    if(!registerNatives(env)) {
        return -1;
    }
    return JNI_VERSION_1_6;
}
