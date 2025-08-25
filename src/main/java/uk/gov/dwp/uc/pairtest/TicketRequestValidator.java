package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

import java.util.Arrays;
import java.util.Objects;

import static uk.gov.dwp.uc.pairtest.config.Config.MAXIMUM_NUMBER_OF_TICKETS;

public class TicketRequestValidator {

    public boolean isTicketRequestValid(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        if (accountId == null || accountId < 0) {
            return false;
        }

        if (ticketTypeRequests.length == 0
                || anyRequestsAreNull(ticketTypeRequests)
                || anyRequestsHaveNullTicketType(ticketTypeRequests)
                || anyRequestsAreForNoTickets(ticketTypeRequests)) {
            return false;
        }

        int numberOfRequestedTickets = getTotalNumberOfRequestedTickets(ticketTypeRequests);
        if (numberOfRequestedTickets > MAXIMUM_NUMBER_OF_TICKETS) {
            return false;
        }

        if (ticketTypeIsPresent(Type.CHILD, ticketTypeRequests) && !ticketTypeIsPresent(Type.ADULT, ticketTypeRequests)) {
            return false;
        }

        int numberOfRequestedInfantTickets = getTotalNumberOfRequestedTicketsForType(Type.INFANT, ticketTypeRequests);
        int numberOfRequestedAdultTickets = getTotalNumberOfRequestedTicketsForType(Type.ADULT, ticketTypeRequests);
        if (numberOfRequestedInfantTickets > numberOfRequestedAdultTickets) {
            return false;
        }

        return true;
    }

    private boolean anyRequestsAreNull(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .anyMatch(Objects::isNull);
    }

    private boolean anyRequestsHaveNullTicketType(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .map(TicketTypeRequest::getTicketType)
                .anyMatch(Objects::isNull);
    }

    private boolean anyRequestsAreForNoTickets(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .anyMatch(req -> req.getNoOfTickets() < 1);
    }

    private int getTotalNumberOfRequestedTickets(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

    private boolean ticketTypeIsPresent(Type ticketType, TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .anyMatch(req -> req.getTicketType().equals(ticketType));
    }

    private int getTotalNumberOfRequestedTicketsForType(Type ticketType, TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(req -> req.getTicketType().equals(ticketType))
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }
}
