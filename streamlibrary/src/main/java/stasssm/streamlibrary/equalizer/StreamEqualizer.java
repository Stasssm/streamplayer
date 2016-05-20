package stasssm.streamlibrary.equalizer;

import android.media.audiofx.Equalizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stas on 16.04.2016.
 */
public class StreamEqualizer {

    Equalizer equalizer ;

    int min_level = 0;
    int max_level = 100;
    int bandsNumber ;

    public StreamEqualizer() {
       equalizer = new Equalizer(0,80) ;
        bandsNumber =  equalizer.getNumberOfBands() ;
        short r[] = equalizer.getBandLevelRange();
        min_level = r[0];
        max_level = r[1];
    }

    public void changeLevel(int level,int position) {
        int new_level = min_level + (max_level - min_level) * level / 100;
        equalizer.setBandLevel((short) position, (short) new_level);
    }

    // return list of progresses
    public ArrayList<Integer> getLevels ()
    {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < bandsNumber; i++)
        {
            int level;
            if (equalizer != null) {
                level = equalizer.getBandLevel((short) i);
            } else {
                level = 0;
            }
            int pos = 100 * level / (max_level - min_level) + 50;
            list.add(pos);
        }
        return list ;
    }



    public String formatBandLabel (int[] band)
    {
        return milliHzToString(band[0]) + "-" + milliHzToString(band[1]);
    }

    public String milliHzToString (int milliHz)
    {
        if (milliHz < 1000) return "";
        if (milliHz < 1000000)
            return "" + (milliHz / 1000) + "Hz";
        else
            return "" + (milliHz / 1000000) + "kHz";
    }


}
