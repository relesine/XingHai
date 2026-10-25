package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinghai.common.PageResult;
import com.xinghai.common.ResultCode;
import com.xinghai.dto.OrderCreateDTO;
import com.xinghai.entity.BizOrder;
import com.xinghai.entity.BizProduct;
import com.xinghai.entity.BizShop;
import com.xinghai.entity.SysUser;
import com.xinghai.exception.BusinessException;
import com.xinghai.mapper.BizOrderMapper;
import com.xinghai.mapper.BizProductMapper;
import com.xinghai.mapper.BizShopMapper;
import com.xinghai.mapper.SysUserMapper;
import com.xinghai.security.UserContext;
import com.xinghai.vo.OrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务：下单（库存扣减）、付款、发货、完成、取消、列表
 * <p>订单状态机：0待付款 → 1已付款 → 2已发货 → 3已完成；0 → 4已取消</p>
 */
@Service
public class OrderService {

    private final BizOrderMapper orderMapper;
    private final BizProductMapper productMapper;
    private final BizShopMapper shopMapper;
    private final SysUserMapper userMapper;

    public OrderService(BizOrderMapper orderMapper, BizProductMapper productMapper,
                        BizShopMapper shopMapper, SysUserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.shopMapper = shopMapper;
        this.userMapper = userMapper;
    }

    /**
     * 创建订单：
     * <p>1. 校验商品在售；2. 原子扣减库存（stock>=qty 才扣，防超卖）；
     * 3. 写入订单（初始=待付款）；4. 幂等控制：同一用户+商品+待付款订单不重复创建</p>
     */
    @Transactional
    public Long create(OrderCreateDTO dto) {
        BizProduct product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在或已下架");
        }
        // 防重复提交：存在同用户同商品待付款订单则直接返回（幂等）
        Long exist = orderMapper.selectCount(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getUserId, UserContext.getUserId())
                .eq(BizOrder::getProductId, dto.getProductId())
                .eq(BizOrder::getStatus, 0));
        if (exist > 0) {
            throw new BusinessException(ResultCode.ORDER_REPEAT);
        }
        // 原子扣减库存（返回影响行数=1 表示扣减成功）
        int rows = productMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<BizProduct>()
                .eq(BizProduct::getId, product.getId())
                .ge(BizProduct::getStock, dto.getQuantity())
                .setSql("stock = stock - " + dto.getQuantity())
                .setSql("sales = sales + " + dto.getQuantity()));
        if (rows == 0) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }

        BizOrder order = new BizOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(UserContext.getUserId());
        order.setShopId(product.getShopId());
        order.setProductId(product.getId());
        order.setProductName(product.getProductName());
        order.setProductCover(product.getCoverUrl());
        order.setPrice(product.getPrice());
        order.setQuantity(dto.getQuantity());
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
        order.setStatus(0);
        order.setAddress(dto.getAddress());
        orderMapper.insert(order);
        return order.getId();
    }

    /** 付款：0→1 */
    @Transactional
    public void pay(Long orderId) {
        BizOrder order = getOwnOrder(orderId);
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    /** 发货：1→2（仅店铺主可操作） */
    @Transactional
    public void ship(Long orderId) {
        BizOrder order = getOrder(orderId);
        checkShopOwner(order.getShopId());
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    /**
     * 确认完成：2→3（买家本人 或 该订单所属店铺主 或 管理员）
     * <p>安全：必须校验操作人身份，防止水平越权（仅凭订单号确认他人订单）。</p>
     */
    @Transactional
    public void complete(Long orderId) {
        BizOrder order = getOrder(orderId);
        Long current = UserContext.getUserId();
        boolean isBuyer = order.getUserId().equals(current);
        boolean isAdmin = UserContext.hasRole("ADMIN");
        boolean isShopOwner = false;
        BizShop shop = shopMapper.selectById(order.getShopId());
        if (shop != null) {
            isShopOwner = shop.getUserId().equals(current);
        }
        if (!isBuyer && !isShopOwner && !isAdmin) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(3);
        orderMapper.updateById(order);
    }

    /** 取消：0→4（仅待付款可取消，回补库存） */
    @Transactional
    public void cancel(Long orderId) {
        BizOrder order = getOwnOrder(orderId);
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(4);
        orderMapper.updateById(order);
        productMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<BizProduct>()
                .eq(BizProduct::getId, order.getProductId())
                .setSql("stock = stock + " + order.getQuantity())
                .setSql("sales = GREATEST(sales - " + order.getQuantity() + ", 0)"));
    }

    /** 我的订单 */
    public PageResult<OrderVO> myOrders(Integer status, long pageNum, long pageSize) {
        Page<BizOrder> page = orderMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizOrder>()
                        .eq(BizOrder::getUserId, UserContext.getUserId())
                        .eq(status != null, BizOrder::getStatus, status)
                        .orderByDesc(BizOrder::getCreateTime));
        return toPageResult(page);
    }

    /** 店铺订单（创作者商家端） */
    public PageResult<OrderVO> shopOrders(Integer status, long pageNum, long pageSize) {
        BizShop shop = shopMapper.selectOne(new LambdaQueryWrapper<BizShop>()
                .eq(BizShop::getUserId, UserContext.getUserId()));
        if (shop == null) {
            return PageResult.of(new Page<>(pageNum, pageSize));
        }
        Page<BizOrder> page = orderMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizOrder>()
                        .eq(BizOrder::getShopId, shop.getId())
                        .eq(status != null, BizOrder::getStatus, status)
                        .orderByDesc(BizOrder::getCreateTime));
        return toPageResult(page);
    }

    /** 管理端：全部订单 */
    public PageResult<OrderVO> adminOrders(Integer status, long pageNum, long pageSize) {
        Page<BizOrder> page = orderMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizOrder>()
                        .eq(status != null, BizOrder::getStatus, status)
                        .orderByDesc(BizOrder::getCreateTime));
        return toPageResult(page);
    }

    /** 订单号：XH + 时间戳 + 3位随机（唯一索引兜底） */
    private String generateOrderNo() {
        return "XH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + String.format("%03d", (int) (Math.random() * 1000));
    }

    private BizOrder getOrder(Long orderId) {
        BizOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        return order;
    }

    private BizOrder getOwnOrder(Long orderId) {
        BizOrder order = getOrder(orderId);
        if (!order.getUserId().equals(UserContext.getUserId()) && !UserContext.hasRole("ADMIN")) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return order;
    }

    private void checkShopOwner(Long shopId) {
        BizShop shop = shopMapper.selectById(shopId);
        if (shop == null || !shop.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    /** 单条订单转 VO（复用批量逻辑，避免 N+1） */
    private OrderVO toVO(BizOrder o, Map<Long, SysUser> buyerMap) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setProductId(o.getProductId());
        vo.setProductName(o.getProductName());
        vo.setProductCover(o.getProductCover());
        vo.setPrice(o.getPrice());
        vo.setQuantity(o.getQuantity());
        vo.setTotalAmount(o.getTotalAmount());
        vo.setStatus(o.getStatus());
        vo.setAddress(o.getAddress());
        vo.setCreateTime(o.getCreateTime());
        vo.setPayTime(o.getPayTime());
        vo.setShipTime(o.getShipTime());
        SysUser buyer = buyerMap.get(o.getUserId());
        if (buyer != null) {
            vo.setBuyerName(buyer.getNickname());
        }
        return vo;
    }

    /** 批量转 VO：一次性查出本页所有买家，避免「每条订单查一次用户」的 N+1 */
    private List<OrderVO> toVOList(List<BizOrder> orders) {
        List<OrderVO> result = new ArrayList<>();
        if (orders == null || orders.isEmpty()) {
            return result;
        }
        List<Long> buyerIds = orders.stream()
                .map(BizOrder::getUserId).distinct().collect(Collectors.toList());
        Map<Long, SysUser> buyerMap = userMapper.selectBatchIds(buyerIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
        for (BizOrder o : orders) {
            result.add(toVO(o, buyerMap));
        }
        return result;
    }

    /** 分页结果转换（批量补买家昵称） */
    private PageResult<OrderVO> toPageResult(Page<BizOrder> page) {
        PageResult<OrderVO> result = new PageResult<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setList(toVOList(page.getRecords()));
        return result;
    }
}
