package com.hrl.mon.role.service;

import com.hrl.mon.role.entity.SysRoleMenu;
import com.hrl.mon.menu.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 角色菜单表 服务类
 * </p>
 *
 * @author Liang
 * @since 2021-06-11
 */
public interface ISysRoleMenuService extends IService<SysRoleMenu> {

    List<SysMenu> getByRoleId(String roleId);

}
