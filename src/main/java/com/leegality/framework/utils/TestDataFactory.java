
package com.leegality.framework.utils;

import com.leegality.framework.models.Booking;
import com.leegality.framework.models.BookingDates;

import java.time.LocalDate;

public class TestDataFactory {

    public static Booking createBooking() {

        LocalDate checkin = LocalDate.now().plusDays(7);
        LocalDate checkout = LocalDate.now().plusDays(11);

        BookingDates dates = new BookingDates(
                checkin.toString(),
                checkout.toString()
        );

        return new Booking(
                "Jim",
                "Brown",
                111,
                true,
                dates,
                "Breakfast"
        );
    }
}
