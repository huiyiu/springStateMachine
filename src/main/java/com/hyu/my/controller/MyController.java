package com.hyu.my.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hyu.my.service.VehicleService;
import com.hyu.my.model.Vehicle;
import com.hyu.my.repo.VehicleRepo;
import com.hyu.my.state.Events;
import com.hyu.my.state.States;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class MyController {

    @Autowired
    StateMachine<States, Events> stateMachine;
    @Autowired
    VehicleService vehicleService;
    @Autowired
    Flyway flyway;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    VehicleRepo vehicleRepo;


    /**
     * 我的
     * 
     * @param request
     * @param titleKey
     * @param response
     * @throws IOException
     */
    @RequestMapping("")
    public void test(HttpServletRequest request, String titleKey, HttpServletResponse response) throws IOException {
        String fileName = URLDecoder.decode(titleKey, "UTF-8");

        if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setContentType(" application/pdf;charset=UTF-8");
        } else {
            response.setContentType(" application/pdf;charset=" + StandardCharsets.ISO_8859_1.name());
            // fileName = new String(fileName.getBytes("UTF-8"),
            // StandardCharsets.ISO_8859_1.name());
        }

        // fileName = new String(fileName.getBytes("UTF-8"), StandardCharsets.ISO_8859_1.name());
        // fileName= new String(fileName.getBytes("ISO-8859-1"), "utf-8"); // 转中文乱码
        fileName = URLEncoder.encode(fileName, "utf-8"); // 符合 RFC 6266 标准
        fileName = fileName.replaceAll("\\+", "%20");

        // 设置响应头
        response.setHeader("Content-disposition",
                "attachment;filename=" + fileName + ".pdf;filename*=utf-8''" + fileName + ".pdf");
        // 兼容不同浏览器的中文乱码问题

        response.setCharacterEncoding("UTF-8");

        // response.setHeader("Content-Disposition", "inline;filename=\"" + fileName + ".pdf" +
        // "\"");

    }

    /**
     * dfds
     * 
     * @return
     */
    @RequestMapping("/demo")
    public String testReponse() {
        String sql = "insert into user(name,age) values(?,?)";
        jdbcTemplate.update(sql,"name1",123);
        return "{\"code\":\"ok\"}";
    }


    @RequestMapping("/send")
    public Events send(Events events,@RequestParam Map<String,Object> ext) {
        MessageBuilder mb = MessageBuilder.withPayload(events);
        ext.forEach((k,v)->{
            mb.setHeader(k,v);
        });
        Message m = mb.build();

        if(stateMachine.isComplete()){
            stateMachine.start();
        }
        stateMachine.sendEvent(m);
        return events;
    }

    @RequestMapping("/save")
    public Vehicle save(Vehicle vehicle) {

        return vehicleService.newVehicle(vehicle);

    }


    @RequestMapping("/send2")
    public Events send2(Events events,@RequestParam Integer vehicleId) {

        vehicleService.change(vehicleId,events);

        return events;
    }

    @RequestMapping("/conclusion")
    public Events send2(Events events,@RequestParam Integer vehicleId,@RequestParam String riskType) {
        if(Arrays.asList("pass","redRisk","suspicious").contains(riskType)){
            Vehicle vehicle = vehicleRepo.findById(vehicleId).orElse(null);
            if(null != vehicle){
                vehicle.setRiskType(riskType);
                vehicleRepo.save(vehicle);
                vehicleService.change(vehicleId,events);
            }
        }

        return events;
    }

}
