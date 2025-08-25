package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;

import static uk.gov.dwp.uc.pairtest.config.Config.ADULT_TICKET_PRICE_PENCE;
import static uk.gov.dwp.uc.pairtest.config.Config.CHILD_TICKET_PRICE_PENCE;

public class TicketServiceImpl implements TicketService {

    private final TicketRequestValidator ticketRequestValidator;
    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    TicketServiceImpl(TicketRequestValidator ticketRequestValidator, TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketRequestValidator = ticketRequestValidator;
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (!ticketRequestValidator.isTicketRequestValid(accountId, ticketTypeRequests)) {
            throw new InvalidPurchaseException();
        }

        int totalTicketPricePence = calculateTicketPrices(ticketTypeRequests);
        ticketPaymentService.makePayment(accountId, totalTicketPricePence);

        int numberOfSeatsToReserve = calculateNumberOfSeatsToReserve(ticketTypeRequests);
        seatReservationService.reserveSeat(accountId, numberOfSeatsToReserve);
    }

    private int calculateTicketPrices(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .mapToInt(this::calculateTicketPricePerRequest)
                .sum();
    }

    private int calculateTicketPricePerRequest(TicketTypeRequest ticketTypeRequest) {
        Type ticketType = ticketTypeRequest.getTicketType();

        return switch (ticketType) {
            case Type.ADULT -> ticketTypeRequest.getNoOfTickets() * ADULT_TICKET_PRICE_PENCE;
            case Type.CHILD -> ticketTypeRequest.getNoOfTickets() * CHILD_TICKET_PRICE_PENCE;
            case Type.INFANT -> 0;
        };
    }

    private int calculateNumberOfSeatsToReserve(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(req -> !Type.INFANT.equals(req.getTicketType()))
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

}
