package com.anush.hamrospace.user.dto.request;

import com.anush.hamrospace.user.enums.AddressType;
import com.anush.hamrospace.user.enums.PreferredLanguage;
import com.anush.hamrospace.user.enums.UiTheme;


public record ProfileRequest(

        String fullName,

        String phoneNumber,

        AddressType addressType,

        String address,

        String profileImageUrl,

        String bio,

        PreferredLanguage preferredLanguage,

        UiTheme uiTheme
    ) {

}
