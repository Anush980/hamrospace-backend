package com.anush.hamrospace.core.security.constants;

import com.anush.hamrospace.common.constants.ApiPath;

public final class PublicPaths {

    private PublicPaths(){}

    public static final String[] PUBLIC_PATHS={

            ApiPath.VERSION + ApiPath.AUTH + ApiPath.LOGIN,
            ApiPath.VERSION + ApiPath.AUTH + ApiPath.REGISTER,

            ApiPath.VERSION + ApiPath.AUTH + ApiPath.REFRESH_TOKEN,
            ApiPath.VERSION + ApiPath.AUTH + ApiPath.FORGOT_PASSWORD,
            ApiPath.VERSION + ApiPath.AUTH + ApiPath.RESET_PASSWORD,
            ApiPath.VERSION + ApiPath.AUTH + ApiPath.VERIFY_EMAIL,
            ApiPath.VERSION + ApiPath.AUTH + ApiPath.SEND_OTP,
            ApiPath.VERSION + ApiPath.AUTH + ApiPath.VERIFY_OTP,

            ApiPath.ACTUATOR + ApiPath.HEALTH,
            ApiPath.VERSION + ApiPath.HEALTH,

             // Swagger UI — without these every /swagger-ui/** request returns 401
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/web/**",
            "/static/**",

            // Public endpoints
            ApiPath.VERSION + ApiPath.PUBLIC + "/**",

            // Error path — Spring Boot forwards here on exceptions; must be public
            "/error",
    };
    
}
