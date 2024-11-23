package com.jiawa.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.domain.Passenger;
import com.jiawa.train.member.domain.PassengerExample;
import com.jiawa.train.member.mapper.PassengerMapper;
import com.jiawa.train.member.req.PassengerQueryReq;
import com.jiawa.train.member.req.PassengerSaveReq;
import com.jiawa.train.member.resp.PassengerQueryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * 乘客服务类，负责处理与乘客相关的业务逻辑
 */
public class PassengerService {

    @Autowired
    private PassengerMapper passengerMapper;

    /**
     * 保存乘客信息
     *
     * @param req 乘客保存请求对象，包含乘客的基本信息
     */
    public void save(PassengerSaveReq req){
        // 获取当前时间，用于记录乘客信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为乘客对象，便于后续操作
        Passenger passenger = BeanUtil.copyProperties(req, Passenger.class);
        // 设置乘客的会员ID，来源于登录会员上下文
        passenger.setMemberId(LoginMemberContext.getId());
        // 生成乘客的唯一ID
        passenger.setId(SnowUtil.getSnowflakeNextId());
        // 设置乘客信息的创建和更新时间为当前时间
        passenger.setCreateTime(now);
        passenger.setUpdateTime(now);
        // 插入乘客信息到数据库
        passengerMapper.insert(passenger);
    }

    /**
     * 查询乘客列表
     *
     * @param req 乘客查询请求对象，可能包含乘客的会员ID等查询条件
     */
    public List<PassengerQueryResp> queryList(PassengerQueryReq req){
        // 创建乘客示例对象，用于构造查询条件
        PassengerExample passengerExample = new PassengerExample();
        PassengerExample.Criteria criteria = passengerExample.createCriteria();
        // 如果请求对象中的会员ID不为空，则添加会员ID作为查询条件
        if(ObjectUtil.isNotNull(req.getMemberId())){
            criteria.andMemberIdEqualTo(req.getMemberId());
        }
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的乘客信息
        List<Passenger> passengerList = passengerMapper.selectByExample(passengerExample);
        return BeanUtil.copyToList(passengerList, PassengerQueryResp.class);
    }
}
