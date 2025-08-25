package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {

    @Mock
    private TicketRequestValidator mockTicketRequestValidator;

    @Mock
    private TicketPaymentService mockTicketPaymentService;

    @Mock
    private SeatReservationService mockSeatReservationService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private final Long accountId = 123456789L;
    private final TicketTypeRequest singleAdultTicketRequest = new TicketTypeRequest(Type.ADULT, 1);

    @Test
    void shouldThrowInvalidPurchaseExceptionForInvalidRequest() {
        //GIVEN
        when(mockTicketRequestValidator.isTicketRequestValid(accountId, singleAdultTicketRequest)).thenReturn(false);

        //WHEN/THEN
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId, singleAdultTicketRequest));
    }

    @Test
    void shouldCorrectlyChargeAndReserveSeatForOneAdultTicket() {
        //GIVEN
        when(mockTicketRequestValidator.isTicketRequestValid(accountId, singleAdultTicketRequest)).thenReturn(true);

        //WHEN
        ticketService.purchaseTickets(accountId, singleAdultTicketRequest);

        //THEN
        verify(mockTicketPaymentService).makePayment(accountId, 2500);
        verify(mockSeatReservationService).reserveSeat(accountId, 1);
    }

    private static Stream<Arguments> requestsAndPricesForMultipleTicketTypes() {
        //Adult tickets, child tickets, infant tickets, total ticket price in pence
        return Stream.of(
                Arguments.of(1, 1, 1, 4000),
                Arguments.of(10, 1, 1, 26500),
                Arguments.of(1, 10, 1, 17500),
                Arguments.of(10, 1, 10, 26500)
        );
    }

    @ParameterizedTest(name = "#{index} - Should charge {3}p for {0} adult, {1} child, and {2} infant tickets")
    @MethodSource("requestsAndPricesForMultipleTicketTypes")
    void testChargingForMultipleTickets(final int numberOfAdultTickets, final int numberOfChildTickets, final int numberOfInfantTickets, final int totalTicketCost) {
        //GIVEN
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(Type.ADULT, numberOfAdultTickets);
        TicketTypeRequest childTicketRequest = new TicketTypeRequest(Type.CHILD, numberOfChildTickets);
        TicketTypeRequest infantTicketRequest = new TicketTypeRequest(Type.INFANT, numberOfInfantTickets);
        when(mockTicketRequestValidator.isTicketRequestValid(accountId, adultTicketRequest, childTicketRequest, infantTicketRequest)).thenReturn(true);

        //WHEN
        ticketService.purchaseTickets(accountId, adultTicketRequest, childTicketRequest, infantTicketRequest);

        //THEN
        verify(mockTicketPaymentService).makePayment(accountId, totalTicketCost);
    }

    @Test
    void shouldCorrectlyChargeForMultipleTicketRequestsOfTheSameType() {
        //GIVEN
        when(mockTicketRequestValidator.isTicketRequestValid(accountId, singleAdultTicketRequest, singleAdultTicketRequest, singleAdultTicketRequest)).thenReturn(true);

        //WHEN
        ticketService.purchaseTickets(accountId, singleAdultTicketRequest, singleAdultTicketRequest, singleAdultTicketRequest);

        //THEN
        verify(mockTicketPaymentService).makePayment(accountId, 7500);
        verify(mockSeatReservationService).reserveSeat(accountId, 3);
    }

    private static Stream<Arguments> requestsAndSeatAmountsForMultipleTicketTypes() {
        //Adult tickets, child tickets, infant tickets, number of seats to reserve
        return Stream.of(
                Arguments.of(1, 1, 1, 2),
                Arguments.of(10, 1, 1, 11),
                Arguments.of(1, 12, 1, 13),
                Arguments.of(5, 1, 5, 6)
        );
    }

    @ParameterizedTest(name = "#{index} - Should reserve {3} seats for {0} adult, {1} child, and {2} infant tickets")
    @MethodSource("requestsAndSeatAmountsForMultipleTicketTypes")
    void testReservingSeatsForMultipleTickets(final int numberOfAdultTickets, final int numberOfChildTickets, final int numberOfInfantTickets, final int numberOfSeatsToReserve) {
        //GIVEN
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(Type.ADULT, numberOfAdultTickets);
        TicketTypeRequest childTicketRequest = new TicketTypeRequest(Type.CHILD, numberOfChildTickets);
        TicketTypeRequest infantTicketRequest = new TicketTypeRequest(Type.INFANT, numberOfInfantTickets);
        when(mockTicketRequestValidator.isTicketRequestValid(accountId, adultTicketRequest, childTicketRequest, infantTicketRequest)).thenReturn(true);

        //WHEN
        ticketService.purchaseTickets(accountId, adultTicketRequest, childTicketRequest, infantTicketRequest);

        //THEN
        verify(mockSeatReservationService).reserveSeat(accountId, numberOfSeatsToReserve);
    }

}
