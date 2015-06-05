package com.benbarron.objectsocket;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Test {

    public static void main(String[] args) {
        OffsetDateTime dateTime = OffsetDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(dateTime.atZoneSameInstant(ZoneId.of("Z")).toLocalDateTime());
        dateTime.

        System.out.println(dateTime);
        System.out.println(dateTime.toLocalDateTime());
        System.out.println();

    }
}
