package com.hrl.mon.role.service;

import com.hrl.mon.role.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 系统角色表 服务类
 * </p>
 *
 * @author Liang
 * @since 2021-06-11
 */
public interface ISysRoleService extends IService<SysRole> {
    SysRole getByUserId(String userId);
}
