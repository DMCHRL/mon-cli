package com.hrl.mon.user.vo;

import com.hrl.mon.menu.dto.MenuTree;
import lombok.Data;

import java.util.List;

@Data
public class UserVO {

    private String username;

    private String company;

    private String userType;

    private String logo;

    private String roleName;

    private List<MenuTree> menuTreeList;
}
