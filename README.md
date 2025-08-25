Application ID Number: 14241446

Campaign Number: 399505

### Repository Overview:

This repository provides a solution for the Ticket Service coding exercise. An implementation has been provided of
`TicketServiceImpl`, with request validation being performed in `TicketRequestValidator`.

Unit tests can be found in the `test.java.uk.gov.dwp.uc.pairtest` package.

The file `Config.java` has been added to store constants for the maximum number of tickets that can be requested and the
ticket prices. In a production setting a config management system may be used to separate configuration from the code,
or in a Spring application these may live in an `application.properties` file.

### Assumptions:

- As infants will be sitting on an adult's lap, the assumption has been made that there must be at least one adult
  ticket purchase per infant ticket.
- Due to the assumption that the `TicketPaymentService` and `SeatReservationService` will work correctly every time they
  are called, no rollback mechanism has been included for cases such as a payment going through but the seat is unable
  to be reserved due to e.g. no seats being available for the customer by the time the request has been processed. In a
  productionised version of this code, more robust error handling may be required.
- An assumption has been made that the calling client would not need detailed information about any validation failures
  in a request to the `TicketService`. If this were required, `InvalidPurchaseException` could be extended to indicate a
  specific failure reason.
- It has been assumed that the `totalAmountToPay` accepted by the `TicketPaymentService` is in pence rather than pounds,
  as this would allow for price increases of smaller increments without requiring code changes.
- An assumption has been made that if any `TicketTypeRequest` objects are provided to the `TicketService` for an account
  ID with a `noOfTickets` set to zero then the entire request will be rejected.
- The `TicketService` interface can take in any number of `TicketTypeRequest` request objects. The assumption has been
  made that it is valid for multiple `TicketTypeRequest` objects to be passed to the `TicketService` for the same ticket
  type. For example, a request that looked like the following code block is being treated as a valid request for two
  adult tickets. A different implementation of the service may mandate that only one request is present per
  ticket type, or that exactly three `TicketTypeRequest` objects are provided per call to the `TicketService` - one for
  each ticket type.

```
TicketTypeRequest ticketRequest1 = new TicketTypeRequest(Type.ADULT, 1)
TicketTypeRequest ticketRequest2 = new TicketTypeRequest(Type.ADULT, 1)
ticketService.purchaseTickets(12345L, ticketRequest1, ticketRequest2)
```
