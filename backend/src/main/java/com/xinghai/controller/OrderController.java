package com.xinghai.controller;

import com.xinghai.common.PageResult;
import com.xinghai.common.Result;
import com.xinghai.dto.OrderCreateDTO;
import com.xinghai.security.RequireRole;
import com.xinghai.service.OrderService;
import com.xinghai.vo.OrderVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单接口（买家下单/我的订单，创作者发货，管理员全量）
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 创建订单（待付款） */
    @PostMapping("/create")
    public Result<Long> create(@Validated @RequestBody OrderCreateDTO dto) {
        return Result.ok("下单成功，请尽快付款", orderService.create(dto));
    }

    /** 付款 */
    @PostMapping("/pay/{orderId}")
    public Result<Void> pay(@PathVariable Long orderId) {
        orderService.pay(orderId);
        return Result.ok();
    }

    /** 发货（店铺主） */
    @PostMapping("/ship/{orderId}")
    @RequireRole("CREATOR")
    public Result<Void> ship(@PathVariable Long orderId) {
        orderService.ship(orderId);
        return Result.ok();
    }

    /** 确认完成 */
    @PostMapping("/complete/{orderId}")
    public Result<Void> complete(@PathVariable Long orderId) {
        orderService.complete(orderId);
        return Result.ok();
    }

    /** 取消（待付款可取消，回补库存） */
    @PostMapping("/cancel/{orderId}")
    public Result<Void> cancel(@PathVariable Long orderId) {
        orderService.cancel(orderId);
        return Result.ok();
    }

    /** 我的订单 */
    @GetMapping("/my")
    public Result<PageResult<OrderVO>> my(@RequestParam(required = false) Integer status,
                                          @RequestParam(defaultValue = "1") long pageNum,
                                          @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(orderService.myOrders(status, pageNum, pageSize));
    }

    /** 店铺订单（创作者） */
    @GetMapping("/shop")
    @RequireRole("CREATOR")
    public Result<PageResult<OrderVO>> shop(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(orderService.shopOrders(status, pageNum, pageSize));
    }

    /** 管理端：全部订单 */
    @GetMapping("/admin/list")
    @RequireRole("ADMIN")
    public Result<PageResult<OrderVO>> admin(@RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "1") long pageNum,
                                             @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(orderService.adminOrders(status, pageNum, pageSize));
    }
}
