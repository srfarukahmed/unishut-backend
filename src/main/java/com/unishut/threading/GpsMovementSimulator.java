package com.unishut.threading;


import java.io.IOException;


/**
 * Simulates continuous GPS movement of a bus.
 */
public class GpsMovementSimulator {


    public static void main(String[] args)
            throws InterruptedException {


        GpsClientSimulator simulator =
                new GpsClientSimulator();



        double[][] locations = {

                {23.8103, 90.4125},

                {23.8108, 90.4130},

                {23.8112, 90.4135},

                {23.8118, 90.4140},

                {23.8123, 90.4145}

        };



        for (double[] location : locations) {


            try {


                simulator.sendLocation(
                        1L,
                        1L,
                        location[0],
                        location[1]
                );


                System.out.println(
                        "Sent location: "
                                + location[0]
                                + ", "
                                + location[1]
                );


            } catch (IOException e) {


                System.err.println(
                        "GPS sending failed"
                );

                e.printStackTrace();

            }



            Thread.sleep(5000);

        }



        System.out.println(
                "GPS movement simulation completed."
        );

    }

}