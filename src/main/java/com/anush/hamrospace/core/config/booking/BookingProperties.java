package com.anush.hamrospace.core.config.booking;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.booking")
public class BookingProperties {

    private BigDecimal feePercentage = BigDecimal.TEN;
}
