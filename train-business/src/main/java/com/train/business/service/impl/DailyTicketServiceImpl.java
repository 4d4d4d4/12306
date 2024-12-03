package com.train.business.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.train.business.mapper.DailyTrainTicketMapper;
import com.train.common.base.entity.domain.DailyTrainStation;
import com.train.common.base.entity.domain.DailyTrainTicket;
import com.train.common.base.entity.query.*;
import com.train.common.base.entity.resp.TrainTicketResp;
import com.train.common.base.entity.resp.TrainTicketStationIndexResp;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.service.DailyStationService;
import com.train.common.base.service.DailyTicketService;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.utils.IdStrUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日车票接口实现</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/7 下午3:39</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@DubboService(version = "1.0.0", token = "true")
public class DailyTicketServiceImpl implements DailyTicketService {
    private final static Logger log = LoggerFactory.getLogger(DailyTicketServiceImpl.class);
    @Autowired
    private DailyTrainTicketMapper dailyTrainTicketMapper;
    @Autowired
    private IdStrUtils idStrUtils;
    @DubboReference(version = "1.0.0", check = false)
    private DailyStationService dailyStationService;
    @Override
    public void batchDelTicketByDate(Date date) {
        if(date == null){
            return;
        }
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        DailyTrainTicketExample.Criteria criteria = dailyTrainTicketExample.createCriteria();
        criteria.andDateEqualTo(date);
        dailyTrainTicketMapper.deleteByExample(dailyTrainTicketExample);

    }

    @Override
    public void batchInsertList(List<DailyTrainTicket> insertList) {
        if(insertList.isEmpty()){
            return;
        }
        log.info("要添加的数据:【{}】", insertList);
        insertList.forEach(item -> {
            if(item.getId() == null){
                item.setId(idStrUtils.snowFlakeLong());
            }
            dailyTrainTicketMapper.insertSelective(item);
        });

    }

    @Override
    public PaginationResultVo<DailyTrainTicket> selectAllByConditionWithPage(TicketQuery query) {
        if (query == null) {
            throw new BusinessException(ResultStatusEnum.CODE_500);
        }
        PaginationResultVo<DailyTrainTicket> resultVo = new PaginationResultVo<>();
        // 分页查询条件
        Integer pageSize = query.getPageSize();
        Integer currentPage = query.getCurrentPage();
        // 根据分页查询条件判断是否需要分页查询
        if (pageSize != null || currentPage != null) {
            pageSize = pageSize == null || pageSize < 5 ? SimplePage.PAGE_20 : pageSize;
            currentPage = currentPage == null || currentPage < 0 ? 1 : currentPage;
            PageHelper.startPage(currentPage, pageSize);
        }
        // 进行条件查询
        List<DailyTrainTicket> dailyTrainCarriages = dailyTrainTicketMapper.selectByConditionQuery(query);
        // 计算分页查询数据
        PageInfo<DailyTrainTicket> pageInfo = new PageInfo<>(dailyTrainCarriages);
        // 构建返回对象
        resultVo.setResult(dailyTrainCarriages);
        resultVo.setPageSize(pageInfo.getPageSize());
        resultVo.setCurrentPage(pageInfo.getPageNum());
        resultVo.setPageCount(pageInfo.getPages());
        resultVo.setDataCount((int) pageInfo.getTotal());
        return resultVo;
    }

    @Override
    public List<TrainTicketResp> queryTicketByCondition(UTrainTicketQuery query) {
        log.info("开始根据条件:【{}】进行火车票查询", JSON.toJSONString(query));
        if(query == null){
            log.error("查询条件为空，查询失败");
            return List.of();
        }
        String departure = query.getDeparture();
        if(StrUtil.isBlank(departure)){
            log.error("查询条件中缺少起始站条件，查询失败");
            return List.of();
        }
        String destination = query.getDestination();
        if(StrUtil.isBlank(destination)){
            log.error("查询条件中缺少终点站条件，查询失败");
            return List.of();
        }
        Date departureDate = query.getDepartureDate();
        if(departureDate == null){
            log.error("查询条件中缺少出发时间条件，查询失败");
            return List.of();
        }
        Date destinationDate = query.getDestinationDate();
        if(query.getTickFormModel() == 1 && destinationDate == null){
            log.error("查询条件中缺少到达时间条件，查询失败");
            return List.of();
        }
        Integer tickFormModel = query.getTickFormModel();
        List<DailyTrainTicket> dailyTrainTickets = dailyTrainTicketMapper.queryTicketByCondition(query);
        return  consolidatedTicket(dailyTrainTickets, tickFormModel);
    }

     @Override
    public DailyTrainTicket selectDTrainByUniqueIndex(Date date, String trainCode, String start, String end) {
        log.info("开始根据唯一索引查询每日车票数据。date:{}, trainCode:{}, start:{}, end:{}",date,trainCode,start,end);
        if(StrUtil.isBlank(trainCode) || StrUtil.isBlank(start) || StrUtil.isBlank(end) || date == null){
            log.info("根据唯一索引查询每日车票数据失败，有关键数据为空");
            return null;
        }
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        DailyTrainTicketExample.Criteria criteria = dailyTrainTicketExample.createCriteria();
        criteria.andDateEqualTo(date).andTrainCodeEqualTo(trainCode).andStartEqualTo(start).andEndEqualTo(end);
         List<DailyTrainTicket> dailyTrainTickets = dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);
         if(dailyTrainTickets.size() != 1){
             log.info("根据唯一键查找出多个数据！：【{}】", JSON.toJSONString(dailyTrainTickets));
         }
         return dailyTrainTickets.get(0);
    }

    @Override
    public void updateRecord(DailyTrainTicket dailyTrainTicket) {
        dailyTrainTicketMapper.updateByPrimaryKey(dailyTrainTicket);
    }

    @Override
    public void updateTicketResidueCount(String trainCode, Date date, String seatType, Integer minStartIndex, Integer maxStartIndex, Integer minEndIndex, Integer maxEndIndex) {
        log.info("批量修改车票余量：【{}、{}、{}】",trainCode,date,seatType);
        dailyTrainTicketMapper.updateTicketResidueCount(trainCode,date,seatType,minStartIndex,maxStartIndex,minEndIndex,maxEndIndex);
    }

    @Override
    public int delDTicketBeforeNow(Date date) {
        if(date == null || date.after(DateTime.now())){
            return 0;
        }
        DailyTrainTicketExample example = new DailyTrainTicketExample();
        DailyTrainTicketExample.Criteria criteria = example.createCriteria();
        criteria.andDateLessThan(date);
        int count = dailyTrainTicketMapper.deleteByExample(example);
        return count;
    }

    /**
     * 整合票，根据模式如果单程则只填充票行程，如果往返则删除票返程票并添加到往返票中的返程参数中
     * @param list
     * @param model
     * @return
     */
    public List<TrainTicketResp> consolidatedTicket(List<DailyTrainTicket> list, Integer model){
        if(list.isEmpty()){
            return List.of();
        }
        List<TrainTicketResp> returnResult = new ArrayList<>();

        // 构造一个单程票集合
        for (DailyTrainTicket item : list) {
            TrainTicketResp trainTicketResp = new TrainTicketResp();
            BeanUtils.copyProperties(item, trainTicketResp);
            fillStationIndex(item, trainTicketResp);
            returnResult.add(trainTicketResp);
        }

        if(model == 1){
            HashMap<String, TrainTicketResp> map = new HashMap<>();
            // 遍历票 此时拿到的票当作返程票
            returnResult.forEach((item) ->
                    map.put(item.getStart() + "_" + item.getEnd(), item));
            List<TrainTicketResp> tempList = new ArrayList<>(returnResult);
            for (TrainTicketResp trainTicketResp : returnResult) {
                String key = trainTicketResp.getEnd() + "_" + trainTicketResp.getStart();
                // 尝试获取当前的单程票
                TrainTicketResp t = map.get(key);
                // 获取到前往票，则当前的票加入获取票的返程票中并在临时集合中删除
                if(t != null){
                    t.setGoBack(trainTicketResp);
                    t.setIsOneWay(false);
                    tempList.remove(trainTicketResp);
                }
            }
            returnResult = tempList;
        }

        return returnResult;
    }


    /**
     *
     * @param item  每日火车车票对象
     * @param trainTicketResp 要填充的对象
     */
    private void fillStationIndex(DailyTrainTicket item, TrainTicketResp trainTicketResp) {
        Integer startIndex = item.getStartIndex();
        Integer endIndex = item.getEndIndex();
        String trainCode = item.getTrainCode();
        Date currentDate = item.getDate();

        List<DailyTrainStation> list = dailyStationService.selectByIndexQuery(trainCode, currentDate,startIndex, endIndex);
        List<TrainTicketStationIndexResp> stationsIndexList = list.stream().map((e) -> {
            TrainTicketStationIndexResp trainTicketStationIndexResp = new TrainTicketStationIndexResp();
            trainTicketStationIndexResp.setIndex(e.getIndex());
            trainTicketStationIndexResp.setName(e.getName());
            trainTicketStationIndexResp.setInTime(e.getInTime());
            trainTicketStationIndexResp.setOutTime(e.getOutTime());
            trainTicketStationIndexResp.setStopTime(e.getStopTime());
            return trainTicketStationIndexResp;
        }).sorted(Comparator.comparing(TrainTicketStationIndexResp::getIndex)).toList();

        trainTicketResp.setStationIndex(stationsIndexList);
    }

}
