package ai.turbochain.ipex.controller;

import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;

import ai.turbochain.ipex.constant.SysConstant;
import ai.turbochain.ipex.controller.BaseController;
import ai.turbochain.ipex.entity.Announcement;
import ai.turbochain.ipex.entity.QAnnouncement;
import ai.turbochain.ipex.pagination.PageResult;
import ai.turbochain.ipex.service.AnnouncementService;
import ai.turbochain.ipex.util.MessageResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * @author GS
 * @description
 * @date 2018/3/5 15:25
 */
@RestController
@RequestMapping("announcement")
public class AnnouncementController extends BaseController {
    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private RedisTemplate redisTemplate;


    /*@ApiOperation("全部")
    @GetMapping
    public MessageResult all() {
        List<Announcement> announcementList = announcementService.findAll();
        return success(announcementList);
    }*/

    @PostMapping("page")
    public MessageResult page(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        //条件
        ArrayList<Predicate> predicates = new ArrayList<>();
        predicates.add(QAnnouncement.announcement.isShow.eq(true));
        //排序
        ArrayList<OrderSpecifier> orderSpecifiers = new ArrayList<>();
        orderSpecifiers.add(QAnnouncement.announcement.sort.desc());
        //查
        PageResult<Announcement> pageResult = announcementService.queryDsl(pageNo, pageSize, predicates, QAnnouncement.announcement, orderSpecifiers);
        return success(pageResult);
    }

    @GetMapping("{id}")
    public MessageResult detail(@PathVariable("id") Long id) {
        Announcement announcement = announcementService.findById(id);
        Assert.notNull(announcement, "validate id!");
        return success(announcement);
    }

    /**
     * 根据ID获取当前公告及上一条和下一条
     * @param id
     * @return
     */
    @RequestMapping(value = "more",method = RequestMethod.POST)
    public MessageResult moreDetail(@RequestParam("id")Long id ){
        ValueOperations redisOperations = redisTemplate.opsForValue();
        JSONObject result  = (JSONObject) redisOperations.get(SysConstant.NOTICE_DETAIL+id);
        if ( result != null){
            return success(result);
        }else {
            JSONObject resultObj = new JSONObject();
            Announcement announcement = announcementService.findById(id);
            Assert.notNull(announcement, "validate id!");
            resultObj.put("info",announcement);
            resultObj.put("back",announcementService.getBack(id));
            resultObj.put("next",announcementService.getNext(id));
            redisOperations.set(SysConstant.NOTICE_DETAIL+id,resultObj,SysConstant.NOTICE_DETAIL_EXPIRE_TIME, TimeUnit.SECONDS);
            return success(resultObj);
        }
    }



}
