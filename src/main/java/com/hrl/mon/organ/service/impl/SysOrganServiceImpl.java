package com.hrl.mon.organ.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.hrl.mon.organ.constant.SysOrganStatusEnum;
import com.hrl.mon.organ.constant.SysOrganTypeEnum;
import com.hrl.mon.organ.entity.SysOrgan;
import com.hrl.mon.organ.mapper.SysOrganMapper;
import com.hrl.mon.organ.service.ISysOrganService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrl.mon.user.dto.UserDTO;
import com.hrl.mon.user.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * <p>
 * 机构管理 服务实现类
 * </p>
 *
 * @author Liang
 * @since 2021-06-11
 */
@Service
public class SysOrganServiceImpl extends ServiceImpl<SysOrganMapper, SysOrgan> implements ISysOrganService {

    @Autowired
    private ISysUserService sysUserService;




    @Override
    @Transactional
    public void add(SysOrgan sysOrgan) {
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        String id = String.valueOf(snowflake.nextId());
        sysOrgan.setTenantId(id);
        sysOrgan.setType(SysOrganTypeEnum.TYPE_COMPANY.getCode());
        sysOrgan.setStatus(SysOrganStatusEnum.NORMAL.getCode());
        this.save(sysOrgan);
        UserDTO userDTO = new UserDTO();
        userDTO.setOrganId(sysOrgan.getId());
        userDTO.setUsername(sysOrgan.getUsername());
        userDTO.setPhone(sysOrgan.getPhone());
        userDTO.setTenantId(id);
        sysUserService.saveUser(userDTO);

    }
}
