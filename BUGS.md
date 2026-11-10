# API Exploratory Testing – Defect Findings

**Project:** Reservation Hub API Automation  
**Base URL:** `https://restful-booker.herokuapp.com`  
**Test date:** 10 November 2026  
**Testing tool:** Rest Assured / Java 17  
**Environment:** Public Restful Booker demo API

## Testing notes

The following findings were observed during exploratory negative testing of the booking creation endpoint (`POST /booking`).

The API responses below are actual observations from the test execution. Expected behaviour is described as a validation expectation and should be confirmed against the product's formal API contract or business requirements.

Temporary bookings created during testing were deleted successfully. The observed delete response was HTTP 201.

---

## BUG-01: Malformed booking date is accepted and transformed into an invalid date

**Severity:** High – Data integrity  
**Endpoint:** `POST /booking`

**Request payload:**

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

**Steps to reproduce:**

1. Send a POST request to `/booking` with the payload above.
2. Inspect the HTTP status and response body.
3. Check the returned `bookingdates.checkin` value.

**Expected behaviour:**

The API should reject a malformed date with a clear client-side validation response (4xx), rather than accepting or modifying it.

**Actual behaviour:**

- HTTP status: `200 OK`
- Booking was created successfully.
- The supplied check-in date `not-a-date` was transformed into `0NaN-aN-aN`.

**Impact:**

Invalid date input is accepted and persisted in a corrupted format, potentially affecting booking date calculations and downstream consumers.

---

## BUG-02: Invalid JSON data types are silently accepted and coerced

**Severity:** High – Data integrity  
**Endpoint:** `POST /booking`

**Request payload:**

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

**Steps to reproduce:**

1. Send a POST request to `/booking` using the payload above.
2. Inspect the returned booking object.

**Expected behaviour:**

The API should validate the field types and reject invalid values with a suitable 4xx response.

**Actual behaviour:**

- HTTP status: `200 OK`
- Booking was created successfully.
- `totalprice` was returned as `null`.
- `depositpaid` was returned as `true`, despite the supplied value being `"not-a-boolean"`.

**Impact:**

Invalid input is silently converted into different values instead of being rejected. This can result in incorrect booking information and unreliable downstream data.

---

## BUG-03: Empty request body causes an Internal Server Error

**Severity:** Medium – Error handling  
**Endpoint:** `POST /booking`

**Request:**

An empty request body was submitted to the booking creation endpoint.

**Steps to reproduce:**

1. Send a POST request to `/booking`.
2. Set the request body to empty.
3. Inspect the response.

**Expected behaviour:**

The API should handle an empty request gracefully and return a suitable client-side validation response (4xx).

**Actual behaviour:**

- HTTP status: `500 Internal Server Error`
- Response body: `Internal Server Error`

**Impact:**

An invalid client request results in a server-side error response rather than a controlled validation response.

---

## BUG-04: Missing booking fields cause an Internal Server Error

**Severity:** Medium – Input validation  
**Endpoint:** `POST /booking`

**Request payload:**

```json
{
  "firstname": "Test"
}
```

**Steps to reproduce:**

1. Send a POST request to `/booking` with only the `firstname` field.
2. Inspect the response.

**Expected behaviour:**

If the booking fields are mandatory under the API contract, the API should reject the incomplete request with a suitable 4xx validation response.

**Actual behaviour:**

- HTTP status: `500 Internal Server Error`
- Response body: `Internal Server Error`

**Impact:**

Incomplete input triggers a server error rather than a controlled validation response.

---

## BUG-05: Booking with checkout date earlier than check-in is accepted

**Severity:** Medium – Business rule validation  
**Endpoint:** `POST /booking`

**Request payload:**

```json
{
  "firstname": "Test",
  "lastname": "User",
  "totalprice": 100,
  "depositpaid": false,
  "bookingdates": {
    "checkin": "2026-11-20",
    "checkout": "2026-11-15"
  },
  "additionalneeds": "None"
}
```

**Steps to reproduce:**

1. Send a POST request to `/booking` with the payload above.
2. Inspect the returned booking dates.

**Expected behaviour:**

If the booking rules require checkout to occur after check-in, the API should reject the invalid date range.

**Actual behaviour:**

- HTTP status: `200 OK`
- Booking was created successfully.
- The response retained check-in as `2026-11-20` and checkout as `2026-11-15`.

**Impact:**

An invalid booking date range can be stored if the API is expected to enforce chronological booking dates.

---

## Additional observations (not reported as confirmed defects)

| Scenario | Actual result | Assessment |
|---|---|---|
| Zero total price | HTTP 200; booking created with price 0 | Not necessarily a defect; zero-price bookings may be valid |
| Negative total price | HTTP 200; booking created with price -100 | Potential business validation issue; confirm the pricing rule |
| Nonexistent booking ID | HTTP 404 | Expected behaviour |
| Cleanup of exploratory bookings | HTTP 201 for all five created bookings in the latest run | Successful cleanup |

## Reproduction and environment notes

- The public demo API may reset its data or behave differently over time.
- Booking IDs are dynamically generated; do not rely on IDs from a previous execution.
- Re-run the supplied payloads to verify current behaviour.
- Delete any successfully created test booking after reproduction.
- Severity is an initial QA assessment and may be revised after confirmation of the API contract.
