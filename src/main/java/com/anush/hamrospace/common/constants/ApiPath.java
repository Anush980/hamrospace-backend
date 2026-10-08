package com.anush.hamrospace.common.constants;

public class ApiPath {

    public static final String VERSION = "/v1";

    // Base modules
    public static final String AUTH = "/auth";
    public static final String ADMIN = "/admin";
    public static final String USERS = "/users";
    public static final String PUBLIC = "/public";
    public static final String PROFILE= "/profile";

    // Common params
    public static final String ID = "/{id}";

    // Auth actions
    public static final String REGISTER = "/register";
    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";

    public static final String REFRESH_TOKEN = "/refresh-token";

    // Password flow
    public static final String FORGOT_PASSWORD = "/forgot-password";
    public static final String RESET_PASSWORD = "/reset-password";
    public static final String CHANGE_PASSWORD = "/change-password";

    // Email verification and OTP
    public static final String VERIFY_EMAIL = "/verify-email";
    public static final String RESEND_VERIFICATION = "/resend-verification";
    public static final String SEND_OTP = "/send-otp";
    public static final String VERIFY_OTP = "/verify-otp";

    // System
    public static final String ACTUATOR = "/actuator";
    public static final String HEALTH = "/health";

    // Security
    public static final String TOKEN_TYPE = "Bearer";
}