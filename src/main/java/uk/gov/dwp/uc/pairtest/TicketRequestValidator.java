package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

import java.util.Arrays;

import static uk.gov.dwp.uc.pairtest.config.Config.MAXIMUM_NUMBER_OF_TICKETS;

public class TicketRequestValidator {

    public boolean isTicketRequestValid(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        if (accountId == null || accountId < 0) {
            return false;
        }

        if (ticketTypeRequests.length == 0
                || anyRequestsAreBadlyFormed(ticketTypeRequests)) {
            return false;
        }

        int numberOfRequestedInfantTickets = getNumberOfRequestedTicketsForType(Type.INFANT, ticketTypeRequests);
        int numberOfRequestedAdultTickets = getNumberOfRequestedTicketsForType(Type.ADULT, ticketTypeRequests);

        if (numberOfRequestedAdultTickets == 0
                || numberOfRequestedInfantTickets > numberOfRequestedAdultTickets) {
            return false;
        }

        int numberOfRequestedTickets = getTotalNumberOfRequestedTickets(ticketTypeRequests);
        return numberOfRequestedTickets <= MAXIMUM_NUMBER_OF_TICKETS;
    }

    private boolean anyRequestsAreBadlyFormed(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .anyMatch(TicketRequestValidator::isSingleRequestBadlyFormed);
    }

    private static boolean isSingleRequestBadlyFormed(TicketTypeRequest ticketTypeRequest) {
        return ticketTypeRequest == null
                || ticketTypeRequest.getTicketType() == null
                || ticketTypeRequest.getNoOfTickets() < 1;
    }

    private int getNumberOfRequestedTicketsForType(Type ticketType, TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(req -> req.getTicketType().equals(ticketType))
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

    private int getTotalNumberOfRequestedTickets(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }
}
