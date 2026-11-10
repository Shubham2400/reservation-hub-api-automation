# API Exploratory Testing - Defect Findings

**Project:** Reservation Hub API Automation
**Base URL:** `https://restful-booker.herokuapp.com`
**Test date:** 10 November 2026
**Testing tool:** Rest Assured / Java 17
**Environment:** Public Restful Booker demo API

## Testing Notes

Exploratory negative testing was performed against the booking creation endpoint (`POST /booking`) to evaluate input validation and error handling.

The findings below are based on actual API responses observed during test execution.

Expected behaviour represents a QA validation expectation and should be confirmed against the formal API contract and applicable business requirements before being treated as a confirmed product defect.

Temporary bookings created during testing were deleted successfully. The observed cleanup response was HTTP 201.

---

## BUG-01: Malformed booking date is accepted and transformed into an invalid date

**Severity:** High - Data integrity
**Endpoint:** `POST /booking`

### Request Payload

```json
{
  "firstname": "Test",
  "lastname": "User",
  "totalprice": 100,
  "depositpaid": false,
  "bookingdates": {
    "checkin": "not-a-date",
    "checkout": "2026-11-18"
  },
  "additionalneeds": "None"
}
```

### cURL Reproduction

```bash
curl -i -X POST "https://restful-booker.herokuapp.com/booking" \
-H "Content-Type: application/json" \
--data-raw '{"firstname":"Test","lastname":"User","totalprice":100,"depositpaid":false,"bookingdates":{"checkin":"not-a-date","checkout":"2026-11-18"},"additionalneeds":"None"}'
```

### Steps to Reproduce

1. Send a POST request to `/booking` with the payload above.
2. Inspect the HTTP status and response body.
3. Check the returned `bookingdates.checkin` value.

### Expected Behaviour

The API should validate the date format and reject malformed date input with a suitable client-side validation response (4xx), rather than accepting or transforming it.

### Actual Behaviour

- HTTP status: `200 OK`
- Booking was created successfully.
- The supplied check-in date `not-a-date` was transformed into `0NaN-aN-aN`.

### Impact

Invalid date input is accepted and persisted in a corrupted format, potentially affecting booking date calculations and downstream consumers.

---

## BUG-02: Invalid JSON data types are silently accepted and coerced

**Severity:** High - Data integrity
**Endpoint:** `POST /booking`

### Request Payload

```json
{
  "firstname": "Test",
  "lastname": "User",
  "totalprice": "not-a-number",
  "depositpaid": "not-a-boolean",
  "bookingdates": {
    "checkin": "2026-11-14",
    "checkout": "2026-11-18"
  },
  "additionalneeds": "None"
}
```

### cURL Reproduction

```bash
curl -i -X POST "https://restful-booker.herokuapp.com/booking" \
-H "Content-Type: application/json" \
--data-raw '{"firstname":"Test","lastname":"User","totalprice":"not-a-number","depositpaid":"not-a-boolean","bookingdates":{"checkin":"2026-11-14","checkout":"2026-11-18"},"additionalneeds":"None"}'
```

### Steps to Reproduce

1. Send a POST request to `/booking` using the payload above.
2. Inspect the returned booking object.
3. Verify the `totalprice` and `depositpaid` fields.

### Expected Behaviour

The API should validate the field types and reject invalid values with a suitable client-side validation response (4xx).

### Actual Behaviour

- HTTP status: `200 OK`
- Booking was created successfully.
- `totalprice` was returned as `null`.
- `depositpaid` was returned as `true`, despite the supplied value being `"not-a-boolean"`.

### Impact

Invalid input is silently converted into different values instead of being rejected. This can result in incorrect booking information and
