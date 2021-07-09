package com.hrl.mon.auth.service;

import com.hrl.mon.common.config.tenant.TenantContextHolder;
import com.hrl.mon.auth.entity.SystemUser;
import com.hrl.mon.menu.entity.SysMenu;
import com.hrl.mon.user.entity.SysUser;
import com.hrl.mon.user.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private ISysUserService iSysUserService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        SysUser sysUser = iSysUserService.getByPhone(s);
        TenantContextHolder.setTenantId(sysUser.getTenantId());
        List<SysMenu> sysMenuList = iSysUserService.getUserMenuByUserId(sysUser.getId());
        SystemUser systemUser = new SystemUser();
        systemUser.setUsername(sysUser.getUsername());
        systemUser.setUserId(sysUser.getId());
        systemUser.setOrganId(sysUser.getOrganId());
        systemUser.setTenantId(sysUser.getTenantId());
        systemUser.setUserType(sysUser.getUserType());
        systemUser.setMenuList(sysMenuList);
        return systemUser;
    }
}
