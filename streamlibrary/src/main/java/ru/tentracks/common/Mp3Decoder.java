package ru.tentracks.common;

/**
 * Created by Стас on 18.08.2015.
 */
public class Mp3Decoder {

    static {
        System.loadLibrary("avutil");
        System.loadLibrary("avcore");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("avdevice");
        System.loadLibrary("swscale");
        System.loadLibrary("avfilter");
        System.loadLibrary("mp3decoder");
    }

    public static native int initializeDecoder();

    public static native int getDecoderSampleRate();

    public static native int getDecoderChannels();

    //public static native int getDecoderPadding();

    //public static native int getDecoderTotalFrames();

    public static native int getDecoderFrameSize();

    public static native int getDecoderBitrate();

    public static native long getDuration();

    public static native int seek(int pos_sec);

    public static int configureDecoder(String filename) {
        return nativeConfigureDecoder(filename);
    }

    private static native int nativeConfigureDecoder(String filename);

    public static int decodeFrame(short[] pcmRight) {
        return nativeDecodeFrame(pcmRight);
    }

    private static native int nativeDecodeFrame(short[] inputBuffer);

    public static native int closeDecoder();
}