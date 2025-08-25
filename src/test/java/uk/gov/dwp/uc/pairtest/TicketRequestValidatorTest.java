package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class TicketRequestValidatorTest {

    private final Long validAccountId = 123456L;

    private final TicketTypeRequest singleAdultTicketRequest = new TicketTypeRequest(Type.ADULT, 1);

    private TicketRequestValidator ticketRequestValidator;

    @BeforeEach
    void setup() {
        ticketRequestValidator = new TicketRequestValidator();
    }

    @Test
    void shouldAcceptValidRequestForOneTicket() {
        //GIVEN/WHEN/THEN
        assertTrue(ticketRequestValidator.isTicketRequestValid(validAccountId, singleAdultTicketRequest));
    }

    @Test
    void shouldAcceptValidRequestForMaxNumberOfTickets() {
        //GIVEN
        TicketTypeRequest ticketRequest = new TicketTypeRequest(Type.ADULT, 25);

        //WHEN/THEN
        assertTrue(ticketRequestValidator.isTicketRequestValid(validAccountId, ticketRequest));
    }

    private static Stream<Arguments> validTicketRequestsWithAllTicketTypes() {
        //Adult tickets, child tickets, infant tickets
        return Stream.of(
                Arguments.of(1, 5, 1),
                Arguments.of(5, 1, 1),
                Arguments.of(3, 10, 3),
                Arguments.of(23, 1, 1),
                Arguments.of(1, 23, 1)
        );
    }

    @ParameterizedTest(name = "#{index} - Should accept requests asking for {0} adult, {1} child, and {2} infant tickets")
    @MethodSource("validTicketRequestsWithAllTicketTypes")
    void validTicketRequests(final int numberOfAdultTickets, final int numberOfChildTickets, final int numberOfInfantTickets) {
        //GIVEN
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(Type.ADULT, numberOfAdultTickets);
        TicketTypeRequest childTicketRequest = new TicketTypeRequest(Type.CHILD, numberOfChildTickets);
        TicketTypeRequest infantTicketRequest = new TicketTypeRequest(Type.INFANT, numberOfInfantTickets);

        //WHEN/THEN
        assertTrue(ticketRequestValidator.isTicketRequestValid(validAccountId, adultTicketRequest, childTicketRequest, infantTicketRequest));
    }

    @Test
    void shouldRejectNullAccountId() {
        //GIVEN/WHEN/THEN
        assertFalse(ticketRequestValidator.isTicketRequestValid(null, singleAdultTicketRequest));
    }

    @Test
    void shouldRejectNegativeAccountId() {
        //GIVEN/WHEN/THEN
        assertFalse(ticketRequestValidator.isTicketRequestValid(-123456L, singleAdultTicketRequest));
    }

    @Test
    void shouldRejectNoRequests() {
        assertFalse(ticketRequestValidator.isTicketRequestValid(validAccountId));
    }

    @Test
    void shouldRejectNullRequests() {
        //GIVEN/WHEN/THEN
        assertFalse(ticketRequestValidator.isTicketRequestValid(validAccountId, singleAdultTicketRequest, null, singleAdultTicketRequest));
    }

    @Test
    void shouldRejectRequestsWithNullType() {
        //GIVEN
        TicketTypeRequest nullTypeRequest = new TicketTypeRequest(null, 1);

        //WHEN/THEN
        assertFalse(ticketRequestValidator.isTicketRequestValid(validAccountId, singleAdultTicketRequest, nullTypeRequest, singleAdultTicketRequest));
    }

    @ParameterizedTest(name = "#{index} - Should reject requests asking for {0} tickets")
    @ValueSource(ints = {-5, -1, 0, 26, 200})
    void testInvalidNumberOfTickets(final int noOfTickets) {
        //GIVEN
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(Type.ADULT, noOfTickets);

        //WHEN/THEN
        assertFalse(ticketRequestValidator.isTicketRequestValid(validAccountId, ticketTypeRequest));
    }

    @ParameterizedTest(name = "#{index} - Should reject requests asking for {0} tickets alongside a valid request")
    @ValueSource(ints = {-5, -1, 0})
    void testInvalidNumberOfTicketsAlongsideValidRequest(final int noOfTickets) {
        //GIVEN
        TicketTypeRequest invalidTicketTypeRequest = new TicketTypeRequest(Type.ADULT, noOfTickets);

        //WHEN/THEN
        assertFalse(ticketRequestValidator.isTicketRequestValid(validAccountId, singleAdultTicketRequest, invalidTicketTypeRequest));
    }

    @Test
    void shouldRejectChildTicketsWithNoAdultTicket() {
        //GIVEN
        TicketTypeRequest childTicketRequest = new TicketTypeRequest(Type.CHILD, 1);

        //WHEN/THEN
        assertFalse(ticketRequestValidator.isTicketRequestValid(validAccountId, childTicketRequest));
    }

    @Test
    void shouldRejectInfantTicketsWithNoAdultTicket() {
        //GIVEN
        TicketTypeRequest infantTicketRequest = new TicketTypeRequest(Type.INFANT, 1);

        //WHEN/THEN
        assertFalse(ticketRequestValidator.isTicketRequestValid(validAccountId, infantTicketRequest));
    }

    @Test
    void shouldRejectInfantAndChildTicketsWithNoAdultTickets() {
        //GIVEN
        TicketTypeRequest childTicketRequest = new TicketTypeRequest(Type.CHILD, 1);
        TicketTypeRequest infantTicketRequest = new TicketTypeRequest(Type.INFANT, 1);

        //WHEN/THEN
        assertFalse(ticketRequestValidator.isTicketRequestValid(validAccountId, childTicketRequest, infantTicketRequest));
    }

    private static Stream<Arguments> moreInfantsThanAdultsRequests() {
        return Stream.of(
                Arguments.of(2, 1),
                Arguments.of(5, 1),
                Arguments.of(10, 9),
                Arguments.of(13, 12)
        );
    }

    @ParameterizedTest(name = "#{index} - Should reject requests asking for {0} infant tickets and {1} adult ticket(s)")
    @MethodSource("moreInfantsThanAdultsRequests")
    void shouldRejectRequestsWithMoreInfantsThanAdults(final int numberOfInfantTickets, final int numberOfAdultTickets) {
        //GIVEN
        TicketTypeRequest infantTicketRequest = new TicketTypeRequest(Type.INFANT, numberOfInfantTickets);
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(Type.ADULT, numberOfAdultTickets);

        //WHEN/THEN
        assertFalse(ticketRequestValidator.isTicketRequestValid(validAccountId, infantTicketRequest, adultTicketRequest));
    }

    private static Stream<Arguments> tooManyTicketsRequests() {
        return Stream.of(
                Arguments.of(1, 25),
                Arguments.of(25, 1),
                Arguments.of(25, 25),
                Arguments.of(100, 100),
                Arguments.of(10, 16)
        );
    }

    @ParameterizedTest(name = "#{index} - Should reject requests asking for {0} plus {1} tickets")
    @MethodSource("tooManyTicketsRequests")
    void testTooManyTicketsRequested(final int numberInFirstBatch, final int numberInSecondBatch) {
        //GIVEN
        TicketTypeRequest firstTicketRequest = new TicketTypeRequest(Type.ADULT, numberInFirstBatch);
        TicketTypeRequest secondTicketRequest = new TicketTypeRequest(Type.CHILD, numberInSecondBatch);

        //WHEN/THEN
        assertFalse(ticketRequestValidator.isTicketRequestValid(validAccountId, firstTicketRequest, secondTicketRequest));
    }

}
