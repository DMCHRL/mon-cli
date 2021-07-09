package com.hrl.mon.auth.entity;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hrl.mon.menu.entity.SysMenu;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SystemUser implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    //用户Id
    private String userId;

    private String username;

    //租户Id
    private String tenantId;

    //0 主账号,1 子账号,2 G端
    private String userType;

    private List<SysMenu> menuList;

    private String organId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return menuList.stream().filter(t -> StringUtils.isNotBlank(t.getPermission())).map(t -> new SimpleGrantedAuthority(t.getPermission())).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    //账户是否未过期
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //账户是否未锁定
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    //凭证是否未过期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //用户是否可用
    @Override
    public boolean isEnabled() {
        return true;
    }

    public List<SysMenu> getMenuList() {
        return menuList;
    }
}
