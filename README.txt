
Implementation Notes
- Technology: Spring Boot 3, Java 17, in-memory storage (no external DB).
- JSON dates/times use Jackson with explicit formats.
  - LocalDate fields expected pattern: yyyy-MM-dd
  - LocalTime fields expected pattern: HH:mm
- Appointment duration: fixed 60 minutes with 60-minute intervals.

How to Run:
- Install JDK 17.
- Run with IDE: start `org.assignment.CalendarApplication`.
- Or with Maven : `mvn spring-boot:run`.
- App listens on port 8080.

API Summary (4 APIS implemented, 2 users - Owner,Invitee)
  - Owner endpoints path: `/api/owner`
  - Invitee endpoints path: `/api/invitee`

Note: All APIs are implemented assuming multi-owner capability.

1) Set Availability (Owner API)
- POST `/api/owner/availability`
- Request JSON:
  {
    "ownerId": "owner001",
    "date": "2025-01-20",
    "startTime": "10:15",
    "endTime": "17:45"
  }
- Assumptions/Rules:
  - Behavior: start time is rounded up to the next whole hour and end time is rounded down to the previous whole hour.
  - `date` cannot be in the past. If past, request is rejected.
  - If there are no full 60-minute slots within the window after aligning the start and end time response is returned with code 400 and msg.
  - If any appointment already exists for the same `ownerId` and `date`, availability for that date cannot be modified, response is returned with code 409 and msg.
- Success Response JSON:
  {
    "code": 200,
    "message": "Availability set successfully"
  }
  - Failure  Response is returned with appropriate `code`:
    - 400: Validation errors (past date, blank/null fields, date/day format, no full 60-minute slots)
    - 409: Conflict when an appointment already exists for that date
    - 500: Unexpected error

2) List Upcoming Appointments (Owner API)
- GET `/api/owner/appointments?ownerId=owner001`
- Response JSON (example):
  [
    {
      "id": "b9f7a766-7a04-4e61-8c2e-7b5f6d5d7b0a",
      "ownerId": "owner-123",
      "date": "2025-01-20",
      "startTime": "10:00",
      "endTime": "11:00",
      "inviteeName": "abc def",
      "inviteeEmail": "abc@example.com"
    }
  ]

3) Search Available Slots (Invitee API)
- GET `/api/invitee/slots?ownerId=owner001`
- Response JSON:
  [
    {
      "date": "2025-01-20",
      "availableStartTimes": ["11:00", "12:00", "13:00"]
    },
    {
      "date": "2025-01-21",
      "availableStartTimes": ["10:00", "11:00"]
    }
  ]

4) Book Appointment (Invitee API)
- POST `/api/invitee/appointments`
- Assumptions/Rules:
  - id is generated using randomUUID().
- Request JSON:
  {
    "ownerId": "owner001",
    "date": "2025-01-20",
    "startTime": "11:00",
    "inviteeName": "abc def",
    "inviteeEmail": "abc@example.com"
  }
- Response JSON (example):
  {
    "id": "c1d2e3f4-1111-2222-3333-abcdefabcdef",
    "ownerId": "owner001",
    "date": "2025-01-20",
    "startTime": "11:00",
    "endTime": "12:00",
    "inviteeName": "abc def",
    "inviteeEmail": "abc@example.com"
  }

Error Handling
• For most endpoints, invalid inputs yield HTTP 400 and conflicts yield HTTP 409; unexpected errors yield HTTP 500. Error JSON includes: `timestamp`, `status`, `error`, `message`, and `type` (the exception simple class name).
• For Set Availability specifically, the endpoint returns a JSON body with `code` and `message` describing success or failure (HTTP 200), with codes:
  - 200 success, 400 bad request, 409 conflict, 500 failure.

Error scenarios:
- Missing/blank `ownerId`, missing `date`/`startTime`, or invalid ranges cause a bad request exception 400 with respective messages.
- Searching slots when no availability is set for an owner results in an error with message: "No available Slots found, please check the availability for the given owner".
- Booking errors include:
  - No availability for owner/date: "No availabile slots for owner on this date"
  - Selected slot not available: "Selected time slot is not available, please select another time slot"
  - Double booking is prevented by removing a booked slot from availability immediately after a successful booking.

Assumptions
- Supports multiple owners; each API call involves `ownerId`.
- Availability is per-owner, per-date; slots are 60-minute intervals only.
- Restarting the app clears data.

Testing
- Unit tests are written with JUnit 5 and Mockito, covering service logic, controller and util.
- Tests are run with Java 17
