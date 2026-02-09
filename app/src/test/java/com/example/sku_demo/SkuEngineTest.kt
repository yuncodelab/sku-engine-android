package com.example.sku_demo

import com.example.sku_demo.data.enum.SpecValueStatus
import com.example.sku_demo.data.model.Sku
import com.example.sku_demo.data.model.Spec
import com.example.sku_demo.data.model.SpecValue
import com.example.sku_demo.engine.SkuEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * SKU Engine 单元测试模板
 *
 * 测试点：
 * 1. 默认选中 SKU
 * 2. 规格选择与取消
 * 3. 是否选全规格
 * 4. 当前选中 SKU 匹配
 * 5. 规格值状态计算（ENABLED/DISABLED/OUT_OF_STOCK/SELECTED）
 */
class SkuEngineTest {

    private lateinit var specList: List<Spec>
    private lateinit var skuList: List<Sku>
    private lateinit var engine: SkuEngine

    @Before
    fun setup() {
        // 构造规格
        specList = listOf(
            Spec(
                specId = "color",
                specName = "颜色",
                values = listOf(
                    SpecValue("red", "红色"),
                    SpecValue("blue", "蓝色")
                )
            ),
            Spec(
                specId = "size",
                specName = "尺码",
                values = listOf(
                    SpecValue("m", "M"),
                    SpecValue("l", "L")
                )
            )
        )

        // 构造 SKU 列表
        skuList = listOf(
            Sku("sku1", price = 100.0, stock = 10, specs = mapOf("color" to "red", "size" to "m")),
            Sku("sku2", price = 110.0, stock = 0, specs = mapOf("color" to "red", "size" to "l")),
            Sku("sku3", price = 120.0, stock = 5, specs = mapOf("color" to "blue", "size" to "m")),
            Sku("sku4", price = 130.0, stock = 0, specs = mapOf("color" to "blue", "size" to "l"))
        )

        engine = SkuEngine(specList, skuList)
    }

    @Test
    fun `test default selection`() {
        val uiList = engine.initSpecStatus()

        // 默认选中第一个有库存 SKU -> sku1
        val selectedSku = engine.getSelectedSku()
        assertNotNull(selectedSku)
        assertEquals("sku1", selectedSku?.skuId)

        // 所有规格值状态正确
        val colorValues = uiList.first { it.specId == "color" }.values
        val sizeValues = uiList.first { it.specId == "size" }.values

        assertEquals(SpecValueStatus.SELECTED, colorValues.first { it.id == "red" }.status)
        assertEquals(SpecValueStatus.ENABLED, colorValues.first { it.id == "blue" }.status)

        assertEquals(SpecValueStatus.SELECTED, sizeValues.first { it.id == "m" }.status)
        assertEquals(SpecValueStatus.OUT_OF_STOCK, sizeValues.first { it.id == "l" }.status)
    }

    @Test
    fun `test select and deselect spec`() {
        engine.initSpecStatus()

        // 默认全选
        var sku = engine.getSelectedSku()
        assertNotNull(sku)
        assertEquals("sku1", sku?.skuId)

        // 点击 size=M → 取消
        engine.select("size", "m")
        sku = engine.getSelectedSku()
        assertNull(sku) // 未全选，正确

        // 选择 color=blue
        engine.select("color", "blue")
        // selectedSpecMap={color=blue}，size未选，全选=false
        sku = engine.getSelectedSku()
        assertNull(sku)

        // 选择 size=m
        engine.select("size", "m")
        // selectedSpecMap={color=blue, size=m}，全选=true
        sku = engine.getSelectedSku()
        assertNotNull(sku)
        assertEquals("sku3", sku?.skuId)
    }

    @Test
    fun `test isAllSpecSelected`() {
        engine.initSpecStatus()

        // 默认全选
        assertTrue(engine.isAllSpecSelected())

        // 取消选择一个规格
        engine.select("size", "m")
        assertFalse(engine.isAllSpecSelected())
    }

    @Test
    fun `test SKU match with stock 0`() {
        // 初始化
        engine.initSpecStatus()

        // 清除默认选择，确保测试可控
        // 先取消默认选中规格
        engine.select("color", "red") // 默认 red -> 取消
        engine.select("size", "m")    // 默认 m -> 取消

        // 现在 selectedSpecMap 为空
        // 选择红色 + L (stock=0)
        engine.select("color", "red")
        engine.select("size", "l")

        val selectedSku = engine.getSelectedSku()
        // 因为现在两种规格都选中，getSelectedSku 返回 sku2（stock=0）
        assertNotNull(selectedSku)
        assertEquals("sku2", selectedSku?.skuId)

        // 检查规格状态
        val uiList = engine.initSpecStatus()
        val sizeLStatus = uiList.first { it.specId == "size" }.values.first { it.id == "l" }.status
        assertEquals(SpecValueStatus.OUT_OF_STOCK, sizeLStatus)
    }
}