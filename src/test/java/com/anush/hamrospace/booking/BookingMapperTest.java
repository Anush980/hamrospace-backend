package com.anush.hamrospace.booking;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.anush.hamrospace.booking.dto.request.BookingCreateRequest;
import com.anush.hamrospace.booking.dto.response.BookingDetailedResponse;
import com.anush.hamrospace.booking.dto.response.BookingSummaryResponse;
import com.anush.hamrospace.booking.entity.Booking;
import com.anush.hamrospace.booking.enums.BookingFeeStatus;
import com.anush.hamrospace.booking.enums.BookingStatus;
import com.anush.hamrospace.booking.mapper.BookingMapper;
import com.anush.hamrospace.booking.mapper.BookingMapperImpl;
import com.anush.hamrospace.common.mapper.MapStructMapperHelper;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.user.entity.Profile;
import com.anush.hamrospace.user.entity.User;

class BookingMapperTest {

    private BookingMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BookingMapperImpl();
        ReflectionTestUtils.setField(mapper, "mapStructMapperHelper", new MapStructMapperHelper());
    }

    @Test
    void toDetailedResponseMapsListingAndGuestFields() {
        UUID bookingId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID guestId = UUID.randomUUID();
        Instant moveInDate = Instant.now().plusSeconds(86_400);

        Listing listing = new Listing();
        listing.setId(listingId);
        listing.setTitle("Sunny apartment");
        listing.setAddress("Kathmandu");
        listing.setSlug("sunny-apartment-abcd1234");

        Profile profile = new Profile();
        profile.setFullName("Guest Name");
        User guest = new User();
        guest.setId(guestId);
        guest.setEmail("guest@example.com");
        guest.setProfile(profile);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setListing(listing);
        booking.setGuest(guest);
        booking.setMoveInDate(moveInDate);
        booking.setMonthlyRent(new BigDecimal("1000.00"));
        booking.setBookingFeeAmount(new BigDecimal("50.00"));
        booking.setNumberOfGuests(2);
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingFeeStatus(BookingFeeStatus.PENDING);
        booking.setNotes("Quiet, please");

        BookingDetailedResponse response = mapper.toDetailedResponse(booking);

        assertThat(response.id()).isEqualTo(bookingId);
        assertThat(response.listingId()).isEqualTo(listingId);
        assertThat(response.listingTitle()).isEqualTo("Sunny apartment");
        assertThat(response.listingAddress()).isEqualTo("Kathmandu");
        assertThat(response.guestId()).isEqualTo(guestId);
        assertThat(response.guestEmail()).isEqualTo("guest@example.com");
        assertThat(response.guestName()).isEqualTo("Guest Name");
        assertThat(response.numberOfGuests()).isEqualTo(2);
        assertThat(response.moveInDate()).isEqualTo(moveInDate);
        assertThat(response.monthlyRent()).isEqualByComparingTo("1000.00");
        assertThat(response.bookingFeeAmount()).isEqualByComparingTo("50.00");
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.notes()).isEqualTo("Quiet, please");
    }

    @Test
    void toSummaryResponseMapsListingSlug() {
        Listing listing = new Listing();
        listing.setId(UUID.randomUUID());
        listing.setTitle("Sunny apartment");
        listing.setSlug("sunny-apartment-abcd1234");

        Booking booking = new Booking();
        booking.setListing(listing);
        booking.setStatus(BookingStatus.CONFIRMED);

        BookingSummaryResponse response = mapper.toSummaryResponse(booking);

        assertThat(response.listingId()).isEqualTo(listing.getId());
        assertThat(response.listingTitle()).isEqualTo("Sunny apartment");
        assertThat(response.listingSlug()).isEqualTo("sunny-apartment-abcd1234");
        assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void toEntityDoesNotCopyServiceManagedMoneyAndGuestCounts() {
        UUID listingId = UUID.randomUUID();
        Instant moveInDate = Instant.now().plusSeconds(86_400);
        BookingCreateRequest request = new BookingCreateRequest(
                listingId, moveInDate, 7, "Please call on arrival");

        Booking booking = mapper.toEntity(request);

        assertThat(booking.getMoveInDate()).isEqualTo(moveInDate);
        assertThat(booking.getNotes()).isEqualTo("Please call on arrival");
        assertThat(booking.getMonthlyRent()).isNull();
        assertThat(booking.getBookingFeeAmount()).isNull();
        // The mapper intentionally ignores numberOfGuests; Booking's field default is 1
        // and BookingServiceImpl overwrites it from the request / listing capacity.
        assertThat(booking.getNumberOfGuests()).isEqualTo(1);
    }
}
