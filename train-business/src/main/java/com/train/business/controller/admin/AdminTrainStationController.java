package com.train.business.controller.admin;

import cn.hutool.core.date.DateTime;
import com.train.common.base.entity.domain.DailyTrainStation;
import com.train.common.base.entity.domain.Station;
import com.train.common.base.entity.domain.Train;
import com.train.common.base.entity.domain.TrainStation;
import com.train.common.base.entity.query.DailyTrainStationQuery;
import com.train.common.base.entity.query.StationIndexQuery;
import com.train.common.base.entity.query.TrainStationQuery;
import com.train.common.base.entity.vo.DailyTrainStationVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.StationIndexVo;
import com.train.common.base.service.DailyTrainStationService;
import com.train.common.base.service.TrainStationService;
import com.train.common.resp.Result;
import com.train.common.utils.IdStrUtils;
import com.train.common.utils.PinYinUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 火车车站管理器</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/1 下午10:49</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@RequestMapping("/admin/tStation")
@RestController
public class AdminTrainStationController {
    @DubboReference(version = "1.0.0", check = false)
    private TrainStationService trainStationService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyTrainStationService dTrainStationService;
    @Autowired
    private IdStrUtils idStrUtils;
    @PostMapping("/day/listByCondition")
    public Result listByDCondition(@RequestBody DailyTrainStationQuery query){
        PaginationResultVo<DailyTrainStation> result = dTrainStationService.selectAllDTStation(query);
        return Result.ok().data("result", result);
    }
    @GetMapping("/day/batchDelDTStation")
    public Result batchDTStation(@RequestParam("ids") List<Long> ids){
        return dTrainStationService.batchDelDTStation(ids);
    }
    @PostMapping("/day/addDTStation")
    public Result addDStation(@RequestBody DailyTrainStationVo vo){
        return dTrainStationService.addDTStation(vo);
    }
    @PostMapping("/day/updateDTStation")
    public Result updateDStation(@RequestBody DailyTrainStation dailyTrainStation){
        return dTrainStationService.updateDTStation(dailyTrainStation);
    }
    @PostMapping("/day/getStationByCondition")
    public Result getStationByCondition(@RequestBody StationIndexQuery query){
        List<StationIndexVo> result =  dTrainStationService.getStationByCondition(query);
        return Result.ok().data("result", result);
    }

    @PostMapping("/getStationByCondition")
    public Result getStationByCondition(@RequestBody TrainStationQuery query){
        PaginationResultVo<TrainStation> resultVo = trainStationService.getStationByCondition(query);
        return Result.ok().data("result",resultVo);
    }


    @RequestMapping("/excelUpload")
    public Result excelUpload(@RequestParam("file") MultipartFile file, HttpServletResponse response){
        response.setStatus(500);
        // 校验文件是否为空
        if (file.isEmpty()) {
            return Result.error().message("空文件");
        }

        // 校验文件扩展名
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return Result.error().message("当前只支持Excel文件");
        }

        try {
            InputStream inputStream = file.getInputStream();
            XSSFWorkbook fileSheets = new XSSFWorkbook(inputStream);
            int numberOfSheets = fileSheets.getNumberOfSheets();
            if(numberOfSheets < 1) return Result.error().message("Excel中无内容");
            XSSFSheet sheet = fileSheets.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            List<TrainStation> list = new ArrayList<>();
            for(int i = 1; i<= lastRowNum; i++){
                TrainStation t = new TrainStation();
                XSSFRow currentCell = sheet.getRow(i);
                if(currentCell.getLastCellNum() < 6) continue;
                String trainCode = getCellValueAsString(currentCell.getCell(0));
                String index = getCellValueAsString(currentCell.getCell(1));
                String stationName =getCellValueAsString(currentCell.getCell(2));
                String inTime = getCellValueAsString(currentCell.getCell(3));
                String outTime = getCellValueAsString(currentCell.getCell(4));
                String stopTime = getCellValueAsString(currentCell.getCell(5));
                double km = Double.parseDouble(getCellValueAsString(currentCell.getCell(6)));
                DateTime now = DateTime.now();
//                t.setId(idStrUtils.snowFlakeLong());
                t.setTrainCode(trainCode);
                try {
                    t.setIndex((int)Double.parseDouble(index));
                }catch (Exception e){
                    return Result.error().message("数据格式不正确，请检查站序是否为数字");
                }
                t.setName(stationName);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                Date parseInTime = simpleDateFormat.parse(inTime);
                Date parseOutTime = simpleDateFormat.parse(outTime);
                Date parseStopTime = simpleDateFormat.parse(stopTime);

                t.setInTime(parseInTime);
                t.setOutTime(parseOutTime);
                t.setStopTime(parseStopTime);
                t.setNamePinyin(PinYinUtils.toPinyin(stationName));
                t.setKm(new BigDecimal(km));
                t.setCreateTime(now);
                t.setUpdateTime(now);
                list.add(t);
            }
            Integer insertCounts = trainStationService.insertAll(list);
            response.setStatus(200);
            return Result.ok().message("数据录入完成,插入了" + insertCounts + "条数据");
        } catch (Exception e) {
            return Result.error().message("数据录入异常" + e.getMessage());
        }
    }
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
