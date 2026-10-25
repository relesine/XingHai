package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinghai.common.PageResult;
import com.xinghai.common.ResultCode;
import com.xinghai.entity.BizProduct;
import com.xinghai.entity.BizShop;
import com.xinghai.exception.BusinessException;
import com.xinghai.mapper.BizProductMapper;
import com.xinghai.mapper.BizShopMapper;
import com.xinghai.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 店铺+商品服务（创作者商家端）
 */
@Service
public class ShopService {

    private final BizShopMapper shopMapper;
    private final BizProductMapper productMapper;

    public ShopService(BizShopMapper shopMapper, BizProductMapper productMapper) {
        this.shopMapper = shopMapper;
        this.productMapper = productMapper;
    }

    /** 创作者开店（一人一店） */
    @Transactional
    public Long createShop(String shopName, String logo, String description) {
        Long userId = UserContext.getUserId();
        if (!UserContext.hasRole("CREATOR")) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        Long count = shopMapper.selectCount(new LambdaQueryWrapper<BizShop>()
                .eq(BizShop::getUserId, userId));
        if (count > 0) {
            throw new BusinessException(ResultCode.SHOP_EXISTS);
        }
        BizShop shop = new BizShop();
        shop.setUserId(userId);
        shop.setShopName(shopName);
        shop.setLogo(logo);
        shop.setDescription(description);
        shop.setStatus(1);
        shopMapper.insert(shop);
        return shop.getId();
    }

    /** 更新店铺 */
    public void updateShop(Long shopId, String shopName, String logo, String description) {
        BizShop shop = getOwnShop(shopId);
        if (shopName != null && !shopName.isBlank()) {
            shop.setShopName(shopName);
        }
        shop.setLogo(logo);
        shop.setDescription(description);
        shopMapper.updateById(shop);
    }

    /** 我的店铺 */
    public Map<String, Object> myShop() {
        BizShop shop = shopMapper.selectOne(new LambdaQueryWrapper<BizShop>()
                .eq(BizShop::getUserId, UserContext.getUserId()));
        if (shop == null) {
            return null;
        }
        return shopToMap(shop);
    }

    /** 公开店铺信息 + 在售商品 */
    public Map<String, Object> shopDetail(Long shopId) {
        BizShop shop = shopMapper.selectById(shopId);
        if (shop == null || shop.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "店铺不存在或已关闭");
        }
        Map<String, Object> result = shopToMap(shop);
        result.put("products", listProducts(shopId, 1, 100).getList());
        return result;
    }

    /** 上架商品 */
    @Transactional
    public Long addProduct(String productName, String coverUrl, String description,
                           BigDecimal price, Integer stock) {
        BizShop shop = getOwnShopByUser(UserContext.getUserId());
        BizProduct product = new BizProduct();
        product.setShopId(shop.getId());
        product.setProductName(productName);
        product.setCoverUrl(coverUrl);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setSales(0);
        product.setStatus(1);
        productMapper.insert(product);
        return product.getId();
    }

    /** 修改商品 */
    public void updateProduct(Long productId, String productName, String coverUrl,
                              String description, BigDecimal price, Integer stock) {
        BizProduct product = getOwnProduct(productId);
        if (productName != null && !productName.isBlank()) {
            product.setProductName(productName);
        }
        product.setCoverUrl(coverUrl);
        product.setDescription(description);
        if (price != null) {
            product.setPrice(price);
        }
        if (stock != null) {
            product.setStock(stock);
        }
        productMapper.updateById(product);
    }

    /** 上下架 */
    public void changeProductStatus(Long productId, Integer status) {
        BizProduct product = getOwnProduct(productId);
        if (status != 0 && status != 1) {
            throw new BusinessException("状态参数不合法");
        }
        product.setStatus(status);
        productMapper.updateById(product);
    }

    /** 删除商品 */
    public void deleteProduct(Long productId) {
        BizProduct product = getOwnProduct(productId);
        productMapper.deleteById(productId);
    }

    /** 店铺商品列表（公开） */
    public PageResult<BizProduct> listProducts(Long shopId, long pageNum, long pageSize) {
        Page<BizProduct> page = productMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizProduct>()
                        .eq(BizProduct::getShopId, shopId)
                        .eq(BizProduct::getStatus, 1)
                        .orderByDesc(BizProduct::getCreateTime));
        return PageResult.of(page);
    }

    /** 管理端：全部商品（含下架） */
    public PageResult<BizProduct> adminList(Long shopId, long pageNum, long pageSize) {
        Page<BizProduct> page = productMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizProduct>()
                        .eq(shopId != null, BizProduct::getShopId, shopId)
                        .orderByDesc(BizProduct::getCreateTime));
        return PageResult.of(page);
    }

    /** 创建订单时原子扣减库存：UPDATE ... SET stock=stock-qty WHERE stock>=qty（防超卖） */
    @Transactional
    public boolean deductStock(Long productId, int quantity) {
        int rows = productMapper.update(null, new LambdaUpdateWrapper<BizProduct>()
                .eq(BizProduct::getId, productId)
                .ge(BizProduct::getStock, quantity)
                .setSql("stock = stock - " + quantity)
                .setSql("sales = sales + " + quantity));
        return rows > 0;
    }

    /** 取消订单时回补库存 */
    public void restoreStock(Long productId, int quantity) {
        productMapper.update(null, new LambdaUpdateWrapper<BizProduct>()
                .eq(BizProduct::getId, productId)
                .setSql("stock = stock + " + quantity)
                .setSql("sales = GREATEST(sales - " + quantity + ", 0)"));
    }

    private BizShop getOwnShop(Long shopId) {
        BizShop shop = shopMapper.selectById(shopId);
        if (shop == null || !shop.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return shop;
    }

    private BizShop getOwnShopByUser(Long userId) {
        BizShop shop = shopMapper.selectOne(new LambdaQueryWrapper<BizShop>()
                .eq(BizShop::getUserId, userId));
        if (shop == null) {
            throw new BusinessException("请先创建店铺");
        }
        return shop;
    }

    private BizProduct getOwnProduct(Long productId) {
        BizProduct product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }
        BizShop shop = shopMapper.selectById(product.getShopId());
        if (shop == null || !shop.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return product;
    }

    private Map<String, Object> shopToMap(BizShop shop) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", shop.getId());
        m.put("userId", shop.getUserId());
        m.put("shopName", shop.getShopName());
        m.put("logo", shop.getLogo());
        m.put("description", shop.getDescription());
        m.put("status", shop.getStatus());
        m.put("createTime", shop.getCreateTime());
        return m;
    }

    public List<BizShop> allShops() {
        return shopMapper.selectList(new LambdaQueryWrapper<BizShop>().orderByAsc(BizShop::getId));
    }

    /** 营业中的店铺列表（公开，商城首页用，只返回安全字段） */
    public List<Map<String, Object>> publicShops() {
        return shopMapper.selectList(new LambdaQueryWrapper<BizShop>()
                        .eq(BizShop::getStatus, 1)
                        .orderByAsc(BizShop::getId))
                .stream().map(this::shopToMap).collect(Collectors.toList());
    }

    /** 商品全部（公开在售，供商城页） */
    public PageResult<BizProduct> publicProducts(long pageNum, long pageSize) {
        Page<BizProduct> page = productMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizProduct>()
                        .eq(BizProduct::getStatus, 1)
                        .orderByDesc(BizProduct::getSales));
        return PageResult.of(page);
    }

    /** 商品详情（公开） */
    public Map<String, Object> productDetail(Long productId) {
        BizProduct product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在或已下架");
        }
        BizShop shop = shopMapper.selectById(product.getShopId());
        Map<String, Object> m = new HashMap<>();
        m.put("product", product);
        m.put("shop", shop == null ? null : shopToMap(shop));
        return m;
    }
}
