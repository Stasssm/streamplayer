#include <assert.h>
#include <jni.h>
#include <string.h>
#include <errno.h>
#include <android/log.h>

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavformat/avio.h"

#define MAX_AUDIO_FRAME_SIZE 192000

AVCodec*	     codec;
AVCodecContext*	 c;
AVFormatContext* fmt_context;
uint8_t* samples_buffer;
int frameSize;

JNIEXPORT jint JNICALL Java_ru_tentracks_common_Mp3Decoder_initializeDecoder
  (JNIEnv *env, jclass class)
{
//    avcodec_init();   //deprecated, now called from avcodec_register_all();
    av_register_all();
 //   avcodec_register_all();
    __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "avcodec_register_all done");
    
    return 0;
}


JNIEXPORT jint JNICALL Java_ru_tentracks_common_Mp3Decoder_nativeConfigureDecoder
  (JNIEnv *env, jclass class, jstring file)
{
    jboolean 			isfilenameCopy;
    const char * 		url = (*env)->GetStringUTFChars(env, file, &isfilenameCopy);    
    
    frameSize = 0;



    __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "ConfigureDecoder started %s", url);
    
    codec = avcodec_find_decoder(CODEC_ID_MP3);
    
    if (!codec) {
		return -1;
	}

    __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "avcodec_find_decoder done");
    
    c = avcodec_alloc_context3(codec);
    
    if (avcodec_open2(c, codec, NULL) != 0) {
        return -1;
    }

    __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "avcodec_alloc_context3 done");

    //#added
  //  AVDictionary *tmp = NULL;
 //   int ret = init_input(&fmt_context, url, &tmp);

    //#end
//    int status = av_open_input_file(&fmt_context, url, NULL, 0, NULL);    deprecated, shoud use avformat_open_input()
    int status = avformat_open_input(&fmt_context, url, NULL, NULL);
	if (status != 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "error status %d", status);
        char errbuf[128];
        __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "error=%s", strerror(-status));
        return -1;
	}
    
    __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "av_open_input_file done");
    
	//if (av_find_stream_info(fmt_context) < 0) { //deprecated, now should use avformat_find_stream_info
	if(avformat_find_stream_info(fmt_context, NULL) < 0) {
        return -1;
	}
    av_dump_format(fmt_context,0,url,0);
    __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "av_find_stream_info done");
    
    samples_buffer = malloc(MAX_AUDIO_FRAME_SIZE);
    
    if (samples_buffer == NULL) {
		return -1;
	}
    __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "malloc done");
    return 0;
}


JNIEXPORT jint JNICALL Java_ru_tentracks_common_Mp3Decoder_getDecoderChannels
  (JNIEnv *env, jclass class)
{
  return c->channels;
}


JNIEXPORT jint JNICALL Java_ru_tentracks_common_Mp3Decoder_getDecoderSampleRate
  (JNIEnv *env, jclass class)
{
  return c->sample_rate;
}

/*JNIEXPORT jint JNICALL Java_net_sourceforge_lame_Lame_getDecoderDelay
  (JNIEnv *env, jclass class)
{
  return enc_delay;
}


JNIEXPORT jint JNICALL Java_net_sourceforge_lame_Lame_getDecoderPadding
  (JNIEnv *env, jclass class)
{
  return enc_padding;
}


JNIEXPORT jint JNICALL Java_net_sourceforge_lame_Lame_getDecoderTotalFrames
  (JNIEnv *env, jclass class)
{
  return mp3data->totalframes;
}
*/

JNIEXPORT jint JNICALL Java_ru_tentracks_common_Mp3Decoder_getDecoderFrameSize
  (JNIEnv *env, jclass class)
{
  return frameSize;
}

JNIEXPORT jlong JNICALL Java_ru_tentracks_common_Mp3Decoder_getDuration
(JNIEnv *env, jclass class)
{
    return fmt_context->duration;
}

JNIEXPORT jint JNICALL Java_ru_tentracks_common_Mp3Decoder_getDecoderBitrate
  (JNIEnv *env, jclass class)
{
    return fmt_context->bit_rate;
}

JNIEXPORT jint JNICALL Java_ru_tentracks_common_Mp3Decoder_nativeDecodeFrame
  (JNIEnv *env, jclass class, jbyteArray mp3Buffer)
{
    int samples_read;
    int e1;
    AVPacket packet;
	av_init_packet(&packet);
    
    //__android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "DecodeFrame started");
        	                    
    e1 = av_read_frame(fmt_context, &packet);
	if (e1 != 0) {
      __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "av_read_frame returned %d, s=%d,p=%d,d=%d", strerror(-e1), packet.size, packet.pos, packet.duration);
      return e1;
	}    

    //__android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "av_read_frame done %d,%d,%d", packet.size, packet.pos, packet.duration);
    
    if (packet.pos == -1) {
      frameSize += packet.size;
    } else {
      if (frameSize < packet.size + packet.pos) {
        frameSize = packet.size + packet.pos;
      }
    }
    
    int sink_bytes = MAX_AUDIO_FRAME_SIZE;
    
//    samples_read = avcodec_decode_audio3(c, (short *)samples_buffer, &sink_bytes, &packet);   //deprecated, now should use avcodec_decode_audio4

    samples_read = avcodec_decode_audio3(c, (short*)samples_buffer, &sink_bytes, &packet);
    __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "got samples_read= %d and sink_bytes =  %d \r\n", samples_read, sink_bytes);
    //int i = 0;
    //while (i < sink_bytes) {
    //    __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "byte %d - %d \r\n", i, samples_buffer[i]);
    //    i++;
    //}
    
    //i = 0;
    //while (i < packet.size) {
    //    __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "packet %d - %d \r\n", i, packet.data[i]);
    //    i++;
    //}    
    
    //__android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "info %d, %d", sink_bytes, samples_read);    
                
    if (samples_read <= 0) {
      __android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "avcoded_decode_audio3 returned %d with sink_bytes = %d", samples_read, sink_bytes);
      return samples_read;
    } else {
        jshort *bytes = (*env)->GetShortArrayElements(env, mp3Buffer, NULL);
        memcpy(bytes, samples_buffer, sink_bytes);
        (*env)->ReleaseShortArrayElements(env, mp3Buffer, bytes, 0);
        //__android_log_print(ANDROID_LOG_DEBUG, "liblame.so", "data transformed");    
        return sink_bytes;
    }
}

JNIEXPORT jint JNICALL Java_ru_tentracks_common_Mp3Decoder_seek(JNIEnv *env, jclass class, jint pos_sec)
{
    int flag = AVSEEK_FLAG_ANY;
    if (pos_sec < 0)
        flag = AVSEEK_FLAG_BACKWARD;
    
    return av_seek_frame(fmt_context, -1, pos_sec*AV_TIME_BASE, flag);
}

JNIEXPORT jint JNICALL Java_ru_tentracks_common_Mp3Decoder_closeDecoder
  (JNIEnv *env, jclass class)
{
    free(samples_buffer);    
 //   av_close_input_file(fmt_context); deprecated, now use avformat_close_input()
    avformat_close_input(fmt_context);
	avcodec_close(c);
	av_free(c);
    return 0;
}
