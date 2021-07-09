package com.hrl.mon.organ.service;

import com.hrl.mon.organ.entity.SysOrgan;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 机构管理 服务类
 * </p>
 *
 * @author Liang
 * @since 2021-06-11
 */
public interface ISysOrganService extends IService<SysOrgan> {

     /**
      * 新增租户，默认开启全部权限
      * @param sysOrgan
      */
     void add(SysOrgan sysOrgan);

}
