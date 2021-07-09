package com.hrl.mon.role.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrl.mon.menu.entity.SysMenu;
import com.hrl.mon.menu.service.ISysMenuService;
import com.hrl.mon.role.entity.SysRoleMenu;
import com.hrl.mon.role.mapper.SysRoleMenuMapper;
import com.hrl.mon.role.service.ISysRoleMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色菜单表 服务实现类
 * </p>
 *
 * @author Liang
 * @since 2021-06-11
 */
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements ISysRoleMenuService {

    @Autowired
    private ISysMenuService iSysMenuService;

    @Override
    public List<SysMenu> getByRoleId(String roleId) {
        List<String> menuIdList = this.list(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        return iSysMenuService.listByIds(menuIdList);
    }
}
