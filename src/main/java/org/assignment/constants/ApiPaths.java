package org.assignment.constants;

public final class ApiPaths {
    private ApiPaths() {}

    public static final String API_BASE = "/api";

    public static final String OWNER = API_BASE + "/owner";
    public static final String INVITEE = API_BASE + "/invitee";

    // Owner endpoints
    public static final String SET_AVAILABILITY = "/availability"; // POST
    public static final String LIST_APPOINTMENTS = "/appointments"; // GET

    // Invitee endpoints
    public static final String SEARCH_SLOTS = "/slots"; // GET with date param
    public static final String BOOK_APPOINTMENT = "/appointments"; // POST
}


