package fr.bouboule.unitiumplugin2;

import java.text.DecimalFormat;

public class Lag
        implements Runnable
{
    public static int TICK_COUNT= 0;
    public static long[] TICKS= new long[600];

    public static String getTPS()
    {
        return getTPS(100);
    }

    public static String getTPS(int ticks)
    {
        DecimalFormat df = new DecimalFormat("0.00");


        if (TICK_COUNT< ticks) {
            return "20.00";
        }
        int target = (TICK_COUNT- 1 - ticks) % TICKS.length;
        long elapsed = System.currentTimeMillis() - TICKS[target];

        return df.format(ticks / (elapsed / 1000.0D));
    }


    public void run()
    {
        TICKS[(TICK_COUNT% TICKS.length)] = System.currentTimeMillis();

        TICK_COUNT+= 1;
    }
}