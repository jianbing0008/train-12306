package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.TrainCarriage;
import com.jiawa.train.business.domain.TrainSeat;
import com.jiawa.train.business.domain.TrainSeatExample;
import com.jiawa.train.business.enums.SeatColEnum;
import com.jiawa.train.business.mapper.TrainSeatMapper;
import com.jiawa.train.business.req.TrainSeatQueryReq;
import com.jiawa.train.business.req.TrainSeatSaveReq;
import com.jiawa.train.business.resp.TrainSeatQueryResp;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
/**
 * TrainSeat服务类，负责处理与TrainSeat相关的业务逻辑
 */
public class TrainSeatService {

    @Autowired
    private TrainSeatMapper trainSeatMapper;
    @Autowired
    private TrainCarriageService trainCarriageService;

    /**
     * 保存TrainSeat信息
     *
     * @param req TrainSeat保存请求对象，包含TrainSeat的基本信息
     */
    public void save(TrainSeatSaveReq req){
        // 获取当前时间，用于记录TrainSeat信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为TrainSeat对象，便于后续操作
        TrainSeat trainSeat = BeanUtil.copyProperties(req, TrainSeat.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增TrainSeat
            // 设置TrainSeat的会员ID，来源于登录会员上下文
            // 生成TrainSeat的唯一ID
            trainSeat.setId(SnowUtil.getSnowflakeNextId());
            // 设置TrainSeat信息的创建和更新时间为当前时间
            trainSeat.setCreateTime(now);
            trainSeat.setUpdateTime(now);
            // 插入TrainSeat信息到数据库
            trainSeatMapper.insert(trainSeat);
        }else{  // 不为空则更新TrainSeat信息
            trainSeat.setUpdateTime(now);
            trainSeatMapper.updateByPrimaryKey(trainSeat);
        }

    }

    /**
     * 查询TrainSeat列表
     *
     * @param req TrainSeat查询请求对象，可能包含TrainSeat的会员ID等查询条件
     */
    public PageResp<TrainSeatQueryResp> queryList(TrainSeatQueryReq req){
        // 创建TrainSeat示例对象，用于构造查询条件
        TrainSeatExample trainSeatExample = new TrainSeatExample();
        //根据id倒序排序
        trainSeatExample.setOrderByClause("train_code asc, carriage_index asc, carriage_seat_index asc");
        // 创建查询条件对象
        TrainSeatExample.Criteria criteria = trainSeatExample.createCriteria();

        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }

        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的TrainSeat信息
        List<TrainSeat> trainSeatList = trainSeatMapper.selectByExample(trainSeatExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<TrainSeat> pageInfo = new PageInfo<>(trainSeatList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<TrainSeatQueryResp> list = BeanUtil.copyToList(trainSeatList, TrainSeatQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<TrainSeatQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainSeatMapper.deleteByPrimaryKey(id);
    }

    /**
     * 生成座位信息:
     * 1. 先删除当前车次下的所有座位记录
     * 2. 根据车厢信息，循环生成座位信息
     * @param trainCode 车次编号
     */
    @Transactional
    public void genTrainSeat(String trainCode) {
        DateTime now = DateTime.now();

        // 清空当前车次下的所有的座位记录
        TrainSeatExample trainSeatExample = new TrainSeatExample();
        TrainSeatExample.Criteria criteria = trainSeatExample.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode);
        trainSeatMapper.deleteByExample(trainSeatExample);

        // 查找当前车次下的所有的车厢
        List<TrainCarriage> carriageList = trainCarriageService.selectByTrainCode(trainCode);
        log.info("当前车次下的车厢数目：{}", carriageList.size());

        //循环生成每个车厢的座位
        for (TrainCarriage trainCarriage : carriageList) {
            // 拿到车厢数据：行数、座位类型(得到列数)
            Integer rowCount = trainCarriage.getRowCount();
            String seatType = trainCarriage.getSeatType();
            int seatIndex = 1;

            // 根据车厢的座位类型，筛选出所有的列，比如车箱类型是一等座，则筛选出columnList={ACDF}
            List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(seatType);
            log.info("车厢[{}], 座位类型[{}], 座位列数：{}", trainCarriage.getIndex(),  seatType, colEnumList.size());

            // 循环行数
            for (int i = 1; i <= rowCount; i++) {
                // 循环列数
                for (SeatColEnum seatColEnum : colEnumList) {
                    // 构造座位数据并保存至数据库
                    TrainSeat trainSeat = new TrainSeat();
                    trainSeat.setId(SnowUtil.getSnowflakeNextId());
                    trainSeat.setTrainCode(trainCode);
                    trainSeat.setCarriageIndex(trainCarriage.getIndex());
                    trainSeat.setRow(StrUtil.fillBefore(String.valueOf(i), '0', 2));
                    trainSeat.setCol(seatColEnum.getCode());
                    trainSeat.setSeatType(seatType);
                    trainSeat.setCarriageSeatIndex(seatIndex++);
                    trainSeat.setCreateTime(now);
                    trainSeat.setUpdateTime(now);

                    trainSeatMapper.insert(trainSeat);
                }
            }
        }
    }


    public List<TrainSeat> selectByTrainCode(String trainCode) {
        TrainSeatExample trainSeatExample = new TrainSeatExample();
        trainSeatExample.setOrderByClause("carriage_index asc, carriage_seat_index asc");
        trainSeatExample.createCriteria().andTrainCodeEqualTo(trainCode);
        return trainSeatMapper.selectByExample(trainSeatExample);
    }
}





