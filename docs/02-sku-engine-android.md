# 基于 SkuEngine 的 Android SKU 架构设计与实现

> 关键词：SKU Engine、状态驱动 UI、纯业务计算、MVVM、BottomSheetDialogFragment

---

## 一、从“模型正确”到“工程可维护”

在上一篇《从数组套数组到标准 SKU 模型》中，我们已经解决了一个核心问题：

> **SKU 规格本质上是组合匹配问题，而不是 UI 嵌套问题。**

但在 Android 工程中，仅仅“模型正确”还远远不够。

真正的挑战是：

* SKU 逻辑应该放在哪一层？
* UI 如何感知状态变化？
* 如何避免 Adapter / Fragment 里充满判断？
* 如何让 SKU 逻辑可以被单元测试？

这篇文章的目标只有一个：

> **把“正确的 SKU 模型”，变成一套“可维护、可扩展、可测试”的 Android 架构。**

---

## 二、整体架构设计

### 1️⃣ 架构总览

这套 SKU 实现采用了非常典型、但职责边界极清晰的分层：

```
UI（Fragment / Adapter）
        ↓
ViewModel（状态中枢）
        ↓
SkuEngine（纯业务计算）
        ↓
Repository（数据来源）
```

每一层只做一件事，不互相越界。

---

### 2️⃣ 各层职责一览

| 层级         | 职责                 |
| ---------- | ------------------ |
| Repository | 提供规格数据 & SKU 数据    |
| SkuEngine  | 规格状态计算、SKU 匹配      |
| ViewModel  | 管理 UI 状态、桥接 Engine |
| UI         | 根据状态渲染，不写业务逻辑      |

👉 **核心原则**：
SKU 规则永远不写在 UI 层。

---

## 三、数据模型拆分：为什么一定要有 UI Model

### 1️⃣ 原始业务模型（接口模型）

```kotlin
data class Spec(
    val specId: String,
    val specName: String,
    val values: List<SpecValue>
)

data class Sku(
    val skuId: String,
    val price: Double,
    val stock: Int,
    val specs: Map<String, String>
)
```

这类模型的特点：

* 来源于接口 / JSON
* 结构稳定
* **不适合直接驱动 UI**

---

### 2️⃣ UI Model：页面真正关心的结构

```kotlin
data class SpecUI(
    val specId: String,
    val specName: String,
    val values: List<SpecValueUI>
)

data class SpecValueUI(
    val id: String,
    val name: String,
    val status: SpecValueStatus
)

enum class SpecValueStatus {
    ENABLED,
    DISABLED,
    OUT_OF_STOCK,
    SELECTED
}
```

UI Model 的意义在于：

* 把「业务判断结果」变成「显式状态」
* Adapter 不需要理解 SKU 规则
* UI 只关心 **“这个值现在是什么状态”**

---

## 四、SkuEngine：整个 SKU 系统的核心

### 1️⃣ 为什么要单独抽一个 Engine

SkuEngine 是一个**纯 Kotlin 类**，它的目标非常明确：

> **给定规格、SKU 列表和当前选择状态，计算出下一帧 UI 应该长什么样。**

它：

* 不依赖 Android
* 不依赖 LiveData
* 不依赖 View / Context

这使得它：

* 可单元测试
* 可复用
* 可独立演进

---

### 2️⃣ 内部状态：Single Source of Truth

```kotlin
private val selectedSpecMap = mutableMapOf<String, String>()
```

含义非常明确：

* key：specId
* value：valueId

这就是当前 SKU 选择状态的**唯一事实来源**。

---

### 3️⃣ 核心算法：规格值状态计算

每一个规格值，都会经历一次「假设选择」：

```kotlin
val tempSelected = selectedSpecMap.toMutableMap()
tempSelected[specId] = valueId
```

然后用这个组合去匹配 SKU：

```kotlin
val matchedSkus = skuList.filter { sku ->
    tempSelected.all { (sId, vId) ->
        sku.specs[sId] == vId
    }
}
```

最终状态判断规则非常清晰：

* ❌ 没有任何 SKU → `DISABLED`
* ⚠️ 有 SKU 但库存全为 0 → `OUT_OF_STOCK`
* ✅ 至少一个 SKU 有库存 → `ENABLED`
* 🎯 当前选中 → `SELECTED`

👉 **所有复杂度，都被收敛在这一处。**

---

### 4️⃣ 默认选中逻辑也属于业务规则

```kotlin
fun initDefaultSelection() {
    val defaultSku = skuList.firstOrNull { it.stock > 0 }
        ?: skuList.firstOrNull()
        ?: return
}
```

为什么这段逻辑放在 Engine，而不是 ViewModel？

* 它是 SKU 业务规则
* 与 UI 无关
* 单测价值极高

---

## 五、ViewModel：状态中枢，而不是业务层

SkuViewModel 的角色非常克制：

* 初始化 Engine
* 转发用户操作
* 对外暴露 UI 状态

```kotlin
val specUiList: LiveData<List<SpecUI>>
val selectedSku: LiveData<Sku?>
val isAllSpecSelected: LiveData<Boolean>
```

所有 SKU 判断，**ViewModel 一律不参与**。

```kotlin
fun selectSpec(specId: String, valueId: String) {
    _specUiList.value = skuEngine.select(specId, valueId)
    updateSelectedSku()
}
```

---

## 六、UI 层：彻底的状态驱动

### 1️⃣ RecyclerView 结构设计

* 外层 RecyclerView：规格
* 内层 RecyclerView：规格值（FlexboxLayoutManager）

```kotlin
rvSpec.itemAnimator = null
rvSpec.setHasFixedSize(false)
```

避免不必要的动画和错误高度假设。

---

### 2️⃣ Adapter 只根据状态渲染

```kotlin
when (value.status) {
    SELECTED -> ...
    ENABLED -> ...
    OUT_OF_STOCK -> ...
    DISABLED -> ...
}
```

Adapter **不判断组合合法性、不管库存规则**。

---

## 七、BottomSheetDialogFragment 的高度策略

在 SKU 场景下：

* 列表高度不稳定
* 自适应高度测量成本极高

最终选择的是：

> **固定 BottomSheet 高度 + RecyclerView 内部滚动**

```kotlin
val maxHeight = (resources.displayMetrics.heightPixels * 0.6).toInt()
bottomSheet.layoutParams.height = maxHeight
```

这是工程上最稳妥、也最容易维护的方案。

---

## 八、为什么 SkuEngine 非常适合单元测试

SkuEngine 具备单测的所有优点：

* 纯 Kotlin
* 输入确定
* 输出确定
* 无 Android 依赖

可以轻松测试：

* 是否选全规格
* 库存为 0 的 SKU 是否可匹配
* 选择 / 取消选择行为
* 状态是否正确计算

👉 这也是为什么 **业务逻辑一定要从 UI 中抽离**。

---

## 九、总结

这套基于 SkuEngine 的 SKU 架构，核心价值在于：

* ✅ 业务逻辑高度集中
* ✅ UI 完全状态驱动
* ✅ 易测试、易扩展
* ✅ 可复用于任何 SKU 场景

最后一句总结：

> **SKU 的复杂度，不在 UI，而在组合状态计算。
> 把计算放进 Engine，UI 才能真正变简单。**

---
