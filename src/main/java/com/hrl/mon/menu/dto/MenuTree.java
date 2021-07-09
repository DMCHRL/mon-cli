package com.hrl.mon.menu.dto;

import com.hrl.mon.common.entity.TreeNode;
import com.hrl.mon.menu.entity.SysMenu;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class MenuTree extends TreeNode {
    private String name;
    private String permission;
    private String path;
    private String parentId;
    private String icon;
    private String component;
    private Integer sort;
    private String keepAlive;
    private String type;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String delFlag;
    public MenuTree() {
    }

    public MenuTree(String id, String name, String parentId) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
    }

    public MenuTree(String id, String name, MenuTree parent) {
        this.id = id;
        this.parentId = parent.getId();
        this.name = name;
    }

    public MenuTree(SysMenu sysMenu) {
        this.id = sysMenu.getId();
        this.parentId = sysMenu.getParentId();
        this.icon = sysMenu.getIcon();
        this.name = sysMenu.getName();
        this.path = sysMenu.getPath();
        this.component = sysMenu.getComponent();
        this.type = sysMenu.getType();
        this.sort = sysMenu.getSort();
        this.keepAlive = sysMenu.getKeepAlive();
        this.permission = sysMenu.getPermission();
        this.createTime = sysMenu.getCreateTime();
        this.updateTime = sysMenu.getUpdateTime();
    }
}
