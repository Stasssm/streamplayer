package stasssm.streamlibrary.visualizer;

/**
 * Created by Stas on 16.04.2016.
 */
// Data class to explicitly indicate that these bytes are raw audio data
public class AudioData
{
    public AudioData(byte[] bytes)
    {
        this.bytes = bytes;
    }

    public byte[] bytes;
}