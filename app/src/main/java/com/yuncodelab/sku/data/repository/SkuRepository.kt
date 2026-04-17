package com.yuncodelab.sku.data.repository

import com.yuncodelab.sku.core.model.result.SpecResponse

/**
 * SKU 数据仓库接口
 *
 * 作用：
 * 1. 对外提供 SKU 相关数据读取能力
 * 2. 屏蔽具体数据来源（Assets / 网络 / 本地缓存等）
 *
 * 设计意义：
 * - 业务层仅依赖 Repository 接口
 * - 方便后期扩展数据源
 */
interface SkuRepository {

    /**
     * 加载 SKU 规格数据
     *
     * @return SpecResponse SKU 初始化数据
     */
    suspend fun loadSpec(): SpecResponse
}