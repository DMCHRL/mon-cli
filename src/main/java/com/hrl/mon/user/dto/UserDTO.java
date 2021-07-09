package com.hrl.mon.user.dto;

import lombok.Data;


@Data
public class UserDTO {

    private String username;

    private String company;

    private String phone;
    private String tenantId;
    private String organId;


    private String logo;

}
