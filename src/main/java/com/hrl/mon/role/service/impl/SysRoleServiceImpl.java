package com.hrl.mon.role.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrl.mon.role.service.ISysRoleService;
import com.hrl.mon.user.entity.SysUserRole;
import com.hrl.mon.user.service.ISysUserRoleService;
import com.hrl.mon.role.entity.SysRole;
import com.hrl.mon.role.mapper.SysRoleMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统角色表 服务实现类
 * </p>
 *
 * @author Liang
 * @since 2021-06-11
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Autowired
    private ISysUserRoleService iSysUserRoleService;

    @Override
    public SysRole getByUserId(String userId) {
        SysUserRole sysUserRole = iSysUserRoleService.getOne(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        return this.getById(sysUserRole.getRoleId());
    }
}
