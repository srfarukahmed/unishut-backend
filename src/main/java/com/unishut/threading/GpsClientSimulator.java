package com.unishut.threading;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.unishut.dto.BusLocationDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;


/**
 * Simple TCP GPS client simulator.
 *
 * This class is intended for local development/testing.
 *
 * It connects to the UniShut GPS TCP server and sends
 * GPS location data as JSON.
 *
 * Production GPS devices would perform the same basic
 * client-server communication independently.
 */
public class GpsClientSimulator {


    private static final String HOST =
            "localhost";


    private static final int PORT =
            9090;



    private final ObjectMapper objectMapper;



    /**
     * Constructor.
     *
     * Configure Jackson to support Java time classes
     * such as LocalDateTime.
     */
    public GpsClientSimulator() {

        objectMapper =
                new ObjectMapper();


        objectMapper.registerModule(
                new JavaTimeModule()
        );


        objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
    }




    /**
     * Send one GPS location to the TCP server.
     *
     * @param busId bus ID
     * @param tripId trip ID
     * @param latitude latitude
     * @param longitude longitude
     */
    public void sendLocation(
            Long busId,
            Long tripId,
            Double latitude,
            Double longitude
    ) throws IOException {


        BusLocationDTO location =
                new BusLocationDTO(
                        busId,
                        tripId,
                        latitude,
                        longitude,
                        LocalDateTime.now(),
                        null,
                        "GPS_SIMULATOR"
                );



        String json =
                objectMapper.writeValueAsString(
                        location
                );



        try (
                Socket socket =
                        new Socket(
                                HOST,
                                PORT
                        );


                PrintWriter writer =
                        new PrintWriter(
                                socket.getOutputStream(),
                                true,
                                StandardCharsets.UTF_8
                        );


                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )

        ) {


            System.out.println(
                    "Connected to UniShut GPS server."
            );


            System.out.println(
                    "Sending:"
            );


            System.out.println(
                    json
            );



            writer.println(json);



            String response =
                    reader.readLine();



            if (response != null) {

                System.out.println(
                        "GPS Server Response:"
                );


                System.out.println(
                        response
                );

            }

        }

    }




    /**
     * Main method for manual local testing.
     */
    public static void main(
            String[] args
    ) {


        GpsClientSimulator simulator =
                new GpsClientSimulator();



        try {


            simulator.sendLocation(
                    1L,
                    1L,
                    23.8103,
                    90.4125
            );



        } catch (IOException exception) {


            System.err.println(
                    "Unable to connect to UniShut GPS server."
            );


            System.err.println(
                    "Make sure Spring Boot is running "
                            + "and TCP port "
                            + PORT
                            + " is available."
            );


            exception.printStackTrace();

        }

    }

}