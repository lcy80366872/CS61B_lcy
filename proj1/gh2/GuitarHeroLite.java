package gh2;
import deque.ArrayDeque;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

/**
 * A client that uses the synthesizer package to replicate a plucked guitar string sound
 */
public class GuitarHeroLite {
    public static final double CONCERT_A = 440.0;
    public static final double CONCERT_C = CONCERT_A * Math.pow(2, 3.0 / 12.0);

    public static void main(String[] args) {
        /* create two guitar strings, for concert A and C */
        GuitarString stringA = new GuitarString(CONCERT_A);
        GuitarString stringC = new GuitarString(CONCERT_C);
        ArrayDeque<GuitarString> GuitarStringArray=new ArrayDeque<>();
        String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
        for (int i =0; i<keyboard.length();i++){
            double frequency = CONCERT_A*Math.pow(2,(i-24)/12.0);
            GuitarStringArray.addLast(new GuitarString(frequency));
        }
        while (true) {

            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {

                char key = StdDraw.nextKeyTyped();
                if(keyboard.indexOf(key)!=-1) {
                    GuitarString string = GuitarStringArray.get(keyboard.indexOf(key));
                    string.pluck();
                }
            }
            double sample = 0.0;
            for (int i = 0; i < GuitarStringArray.size(); i++) {
                sample += GuitarStringArray.get(i).sample();
            }

            StdAudio.play(sample);

            for (int i = 0; i < GuitarStringArray.size(); i++) {
                GuitarStringArray.get(i).tic();
            }

//            /* compute the superposition of samples */
//            double sample = stringA.sample() + stringC.sample();
//
//            /* play the sample on standard audio */
//            StdAudio.play(sample);
//
//            /* advance the simulation of each guitar string by one step */
//            stringA.tic();
//            stringC.tic();
        }
    }
};


