package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.business.domain.ConfirmOrder;
import com.jiawa.train.business.domain.ConfirmOrderExample;
import com.jiawa.train.business.mapper.ConfirmOrderMapper;
import com.jiawa.train.business.req.ConfirmOrderQueryReq;
import com.jiawa.train.business.req.ConfirmOrderSaveReq;
import com.jiawa.train.business.resp.ConfirmOrderQueryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * ConfirmOrder服务类，负责处理与ConfirmOrder相关的业务逻辑
 */
public class ConfirmOrderService {

    @Autowired
    private ConfirmOrderMapper confirmOrderMapper;

    /**
     * 保存ConfirmOrder信息
     *
     * @param req ConfirmOrder保存请求对象，包含ConfirmOrder的基本信息
     */
    public void save(ConfirmOrderSaveReq req){
        // 获取当前时间，用于记录ConfirmOrder信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为ConfirmOrder对象，便于后续操作
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增ConfirmOrder
            // 设置ConfirmOrder的会员ID，来源于登录会员上下文
            // 生成ConfirmOrder的唯一ID
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            // 设置ConfirmOrder信息的创建和更新时间为当前时间
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            // 插入ConfirmOrder信息到数据库
            confirmOrderMapper.insert(confirmOrder);
        }else{  // 不为空则更新ConfirmOrder信息
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
        }

    }

    /**
     * 查询ConfirmOrder列表
     *
     * @param req ConfirmOrder查询请求对象，可能包含ConfirmOrder的会员ID等查询条件
     */
    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req){
        // 创建ConfirmOrder示例对象，用于构造查询条件
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        //根据id倒序排序
        confirmOrderExample.setOrderByClause("id desc");
        // 创建查询条件对象
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();


        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的ConfirmOrder信息
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        confirmOrderMapper.deleteByPrimaryKey(id);
    }
}