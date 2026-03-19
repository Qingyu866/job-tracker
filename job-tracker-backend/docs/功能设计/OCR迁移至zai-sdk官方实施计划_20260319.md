# OCR 迁移至 zai-sdk 官方实施计划

## 📋 项目概述

**目标**: 将现有的手动HTTP调用方式迁移到智谱AI官方SDK `zai-sdk`

**迁移原因**:
- ✅ 使用官方SDK，代码更简洁、更可靠
- ✅ 减少手动HTTP请求处理逻辑
- ✅ 自动处理认证、重试、错误处理等
- ✅ 获得官方维护和更新支持

**涉及范围**: OCR模块

**预计工作量**: 2-3小时

---

## 当前实现分析

### 现有架构

```
OcrController
  ↓
OcrService (识别服务)
  ↓
ZhipuOcrClient (手动HTTP调用)
  ↓
智谱 OCR API
```

### 现有问题

1. **手动HTTP调用**
   - 使用 OkHttp 手动构建请求
   - 手动处理 JSON 序列化/反序列化
   - 手动处理认证头

2. **代码冗余**
   - 自定义 `ZhipuOcrRequest` DTO
   - 自定义 `ZhipuOcrResponse` DTO
   - 手动错误处理逻辑

3. **维护成本高**
   - API更新需要手动同步
   - 错误处理不统一
   - 缺少重试机制

---

## 目标架构

### 新架构

```
OcrController
  ↓
OcrService (识别服务)
  ↓
ZhipuOcrClient (使用 zai-sdk)
  ↓
ZhipuAiClient (官方SDK)
  ↓
智谱 OCR API (glm-ocr模型)
```

### 优化点

1. **使用官方SDK**
   - 自动处理认证
   - 自动重试
   - 统一错误处理

2. **简化代码**
   - 移除自定义 DTO
   - 移除手动HTTP逻辑
   - 减少约150行代码

3. **更可靠的错误处理**
   - SDK内置错误处理
   - 标准化的响应格式

---

## 实施计划

### 阶段 1: 依赖管理 ⏱️ 15分钟

#### 1.1 添加 Maven 依赖

**文件**: `pom.xml`

**位置**: `<dependencies>` 标签内

**添加内容**:
```xml
<!-- 智谱AI官方SDK -->
<dependency>
  <groupId>ai.z.openapi</groupId>
  <artifactId>zai-sdk</artifactId>
  <version>0.3.3</version>
</dependency>
```

**注意事项**:
- ✅ 检查版本冲突
- ✅ 确认传递依赖兼容性
- ✅ 验证 JDK 版本兼容（SDK需要 JDK 8+）

---

### 阶段 2: 配置更新 ⏱️ 10分钟

#### 2.1 更新 application.yml

**文件**: `src/main/resources/application.yml`

**当前配置**:
```yaml
ocr:
  enabled: true
  default-provider: ZHIPU
  zhipu:
    api-url: https://open.bigmodel.cn/api/paas/v4/ocr
    api-key: ${ZHIPU_API_KEY}
    timeout: 30000
```

**修改后**:
```yaml
ocr:
  enabled: true
  default-provider: ZHIPU
  zhipu:
    api-key: ${ZHIPU_API_KEY}  # 只需要API Key
    model: glm-ocr              # 模型名称
    timeout: 30000              # 超时时间（毫秒）
```

**说明**:
- ❌ 移除 `api-url`（SDK内部处理）
- ✅ 添加 `model` 配置（明确指定模型）
- ✅ 保留 `api-key` 和 `timeout`

---

### 阶段 3: 重写 ZhipuOcrClient ⏱️ 60分钟

#### 3.1 新的 ZhipuOcrClient 实现

**文件**: `src/main/java/com/jobtracker/ocr/ZhipuOcrClient.java`

**完整代码**:
```java
package com.jobtracker.ocr;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.layoutparsing.LayoutParsingCreateParams;
import ai.z.openapi.service.layoutparsing.LayoutParsingResponse;
import com.jobtracker.ocr.dto.OcrOptions;
import com.jobtracker.ocr.dto.OcrResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 智谱 OCR 云服务客户端（使用官方SDK）
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-19
 */
@Slf4j
@Component
public class ZhipuOcrClient {

    @Value("${ocr.zhipu.api-key}")
    private String apiKey;

    @Value("${ocr.zhipu.model:glm-ocr}")
    private String model;

    @Value("${ocr.zhipu.timeout:30000}")
    private int timeout;

    private ZhipuAiClient client;

    /**
     * 初始化客户端（懒加载）
     */
    private ZhipuAiClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = ZhipuAiClient.builder()
                            .ofZHIPU()
                            .apiKey(apiKey)
                            .build();
                    log.info("智谱AI客户端初始化成功，模型: {}", model);
                }
            }
        }
        return client;
    }

    /**
     * 调用 OCR 识别图片（使用官方SDK）
     *
     * @param imageData 图片字节数组
     * @param options    识别选项
     * @return 识别结果
     */
    public OcrResult recognize(byte[] imageData, OcrOptions options) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 上传图片到临时存储（如果需要URL）
            // 注意：zai-sdk 可能需要图片URL，而不是base64
            // 这里需要先确认SDK支持的输入格式

            // TODO: 实现图片上传逻辑
            String imageUrl = uploadImage(imageData);
            if (imageUrl == null) {
                return OcrResult.failed("UPLOAD_FAILED", "图片上传失败");
            }

            log.debug("开始OCR识别，图片URL: {}", imageUrl);

            // 2. 构建请求参数
            LayoutParsingCreateParams params = LayoutParsingCreateParams.builder()
                    .model(model)
                    .file(imageUrl)
                    .build();

            // 3. 调用官方SDK
            LayoutParsingResponse response = getClient().layoutParsing()
                    .layoutParsing(params);

            long processingTime = System.currentTimeMillis() - startTime;

            // 4. 处理响应
            if (response.isSuccess()) {
                String text = extractTextFromResponse(response);
                Double confidence = extractConfidenceFromResponse(response);

                log.info("OCR识别成功，文本长度: {}, 置信度: {}, 耗时: {} ms",
                        text.length(), confidence, processingTime);

                return OcrResult.success(text, confidence, processingTime, "ZHIPU");
            } else {
                log.error("OCR API返回错误: {}", response.getMsg());
                return OcrResult.failed("API_ERROR", response.getMsg());
            }

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("OCR识别异常: {}", e.getMessage(), e);
            return OcrResult.failed("SDK_ERROR", "SDK调用失败: " + e.getMessage());
        }
    }

    /**
     * 上传图片并获取URL
     *
     * 注意：这是临时方案，需要确认SDK是否支持直接base64输入
     * 如果SDK支持base64，可以移除这个方法
     */
    private String uploadImage(byte[] imageData) {
        // TODO: 实现图片上传逻辑
        // 方案1: 上传到OSS（阿里云/七牛云）
        // 方案2: 上传到本地服务器
        // 方案3: 使用SDK内置的base64支持（如果有）

        log.warn("图片上传功能暂未实现，请确认SDK是否支持base64输入");
        return null;
    }

    /**
     * 从响应中提取文本
     */
    private String extractTextFromResponse(LayoutParsingResponse response) {
        if (response.getData() == null) {
            return "";
        }

        // 根据SDK实际响应格式调整
        LayoutParsingResult result = response.getData();
        // TODO: 根据实际SDK响应提取文本
        return result.toString();  // 临时实现
    }

    /**
     * 从响应中提取置信度
     */
    private Double extractConfidenceFromResponse(LayoutParsingResponse response) {
        // TODO: 根据实际SDK响应提取置信度
        return 0.95;  // 临时实现
    }

    /**
     * 健康检查
     */
    public boolean healthCheck() {
        try {
            // 发送一个简单的请求测试连接
            return getClient() != null && apiKey != null && !apiKey.isBlank();
        } catch (Exception e) {
            log.warn("智谱OCR健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查配置是否完整
     */
    public boolean isConfigured() {
        return apiKey != null
                && !apiKey.isBlank()
                && !apiKey.equals("${ocr.zhipu.api-key}");
    }
}
```

#### 3.2 需要确认的问题

**❓ 问题1: SDK 是否支持 base64 输入？**

官方示例使用 URL:
```java
String file = "https://cdn.bigmodel.cn/static/logo/introduction.png";
```

**需要确认**:
- ✅ 是否支持直接传入 base64 编码的图片？
- ✅ 是否支持 byte[] 输入？
- ❓ 如果不支持，需要实现图片上传逻辑

**❓ 问题2: 响应格式是什么？**

官方示例:
```java
if (response.isSuccess()) {
    System.out.println("解析结果: " + response.getData());
}
```

**需要确认**:
- ✅ `response.getData()` 返回的具体类型是什么？
- ✅ 如何提取识别的文本内容？
- ✅ 如何获取置信度信息？

**❓ 问题3: 是否需要 LayoutParsing？**

**需要确认**:
- ✅ `LayoutParsing` 是否就是 OCR 功能？
- ✅ 是否有其他更合适的 API？
- ✅ 是否支持表格识别？

---

### 阶段 4: 清理冗余代码 ⏱️ 30分钟

#### 4.1 删除自定义 DTO

**文件**:
- `src/main/java/com/jobtracker/ocr/dto/ZhipuOcrRequest.java` ❌ 删除
- `src/main/java/com/jobtracker/ocr/dto/ZhipuOcrResponse.java` ❌ 删除

**原因**: SDK已包含请求/响应定义

#### 4.2 简化 ZhipuOcrClient

**删除的内容**:
- ❌ ObjectMapper 注入（SDK内部处理）
- ❌ OkHttpClient 注入（SDK内部处理）
- ❌ 手动HTTP请求逻辑
- ❌ 手动JSON序列化/反序列化

**保留的内容**:
- ✅ 配置属性注入
- ✅ `recognize()` 方法签名
- ✅ `isConfigured()` 方法
- ✅ `healthCheck()` 方法

---

### 阶段 5: 测试验证 ⏱️ 30分钟

#### 5.1 单元测试

**测试用例**:
```java
@SpringBootTest
class ZhipuOcrClientTest {

    @Autowired
    private ZhipuOcrClient zhipuOcrClient;

    @Test
    void testClientInitialization() {
        assertNotNull(zhipuOcrClient);
        assertTrue(zhipuOcrClient.isConfigured());
    }

    @Test
    void testHealthCheck() {
        assertTrue(zhipuOcrClient.healthCheck());
    }

    @Test
    void testRecognizeWithRealImage() throws IOException {
        // 读取测试图片
        byte[] imageData = Files.readAllBytes(
                Paths.get("src/test/resources/test-image.png")
        );

        OcrResult result = zhipuOcrClient.recognize(
                imageData,
                OcrOptions.defaultOptions()
        );

        assertTrue(result.isSuccess());
        assertNotNull(result.getText());
        assertTrue(result.getConfidence() > 0);
    }
}
```

#### 5.2 集成测试

**测试场景**:
1. ✅ 简历识别（`POST /api/ocr/resume`）
2. ✅ JD识别（`POST /api/ocr/jd`）
3. ✅ 直接识别（`POST /api/ocr/recognize`）
4. ✅ 错误处理（无效图片、网络错误）
5. ✅ 超时处理

#### 5.3 性能测试

**测试指标**:
- ⏱️ 平均响应时间
- ⏱️ P95 响应时间
- ✅ 成功率
- ✅ 并发处理能力

---

### 阶段 6: 文档更新 ⏱️ 15分钟

#### 6.1 更新 API 文档

**文件**: `docs/API文档/完整API参考文档_20260319.md`

**更新内容**:
- ✅ OCR 接口说明
- ✅ 请求/响应示例
- ✅ 错误代码说明

#### 6.2 更新配置文档

**文件**: `README.md` 或 `docs/配置说明.md`

**更新内容**:
```yaml
# OCR 配置
ocr:
  enabled: true
  zhipu:
    api-key: "your-api-key"  # 必填：智谱AI API Key
    model: glm-ocr            # 可选：模型名称，默认 glm-ocr
    timeout: 30000            # 可选：超时时间（毫秒），默认 30秒
```

#### 6.3 创建迁移说明

**文件**: `docs/功能设计/OCR迁移至zai-sdk说明_20260319.md`

**内容**:
- ✅ 迁移原因
- ✅ 主要变更
- ✅ 配置变更
- ✅ API 兼容性说明

---

## 风险评估

### 高风险 🔴

1. **SDK 输入格式不明确**
   - **风险**: SDK 可能只支持 URL，不支持 base64
   - **缓解**: 先验证 SDK 文档，必要时实现图片上传

2. **响应格式不明确**
   - **风险**: 不知道如何从 `response.getData()` 提取文本
   - **缓解**: 查阅 SDK 文档和源码

### 中风险 🟡

3. **版本兼容性**
   - **风险**: zai-sdk 0.3.3 可能存在 bug
   - **缓解**: 查看版本更新日志，必要时升级版本

4. **依赖冲突**
   - **风险**: zai-sdk 的依赖可能与项目冲突
   - **缓解**: 使用 `mvn dependency:tree` 检查

### 低风险 🟢

5. **性能回退**
   - **风险**: 官方SDK可能比手动实现慢
   - **缓解**: 性能测试验证

---

## 回滚方案

### 如果迁移失败

1. **保留旧代码**
   - 将旧的 `ZhipuOcrClient` 重命名为 `ZhipuOcrClientLegacy`
   - 暂不删除，作为备份

2. **快速回滚**
   ```java
   // 在 OcrService 中添加切换开关
   @Value("${ocr.use-legacy-client:false}")
   private boolean useLegacyClient;

   public OcrResult recognize(byte[] imageData, OcrOptions options) {
       if (useLegacyClient) {
           return zhipuOcrClientLegacy.recognize(imageData, options);
       }
       return zhipuOcrClient.recognize(imageData, options);
   }
   ```

3. **配置回滚**
   ```yaml
   ocr:
     use-legacy-client: true  # 启用旧客户端
   ```

---

## 执行检查清单

### 准备阶段
- [ ] 查阅 zai-sdk 官方文档
- [ ] 确认 SDK 支持的输入格式（base64/URL/byte[]）
- [ ] 确认响应格式和字段提取方法
- [ ] 检查项目依赖兼容性

### 实施阶段
- [ ] 添加 zai-sdk 依赖到 pom.xml
- [ ] 更新 application.yml 配置
- [ ] 备份现有 ZhipuOcrClient（重命名为 Legacy）
- [ ] 实现新的 ZhipuOcrClient（使用 SDK）
- [ ] 处理图片上传逻辑（如果需要）
- [ ] 删除冗余的自定义 DTO

### 测试阶段
- [ ] 单元测试：客户端初始化
- [ ] 单元测试：健康检查
- [ ] 集成测试：简历识别
- [ ] 集成测试：JD识别
- [ ] 集成测试：错误处理
- [ ] 性能测试：响应时间
- [ ] 性能测试：并发处理

### 部署阶段
- [ ] 更新 API 文档
- [ ] 更新配置文档
- [ ] 创建迁移说明
- [ ] 代码审查
- [ ] 合并到主分支

---

## 待确认事项

### ❓ 关键问题

1. **SDK 输入格式**
   - [ ] 确认是否支持 base64 输入
   - [ ] 确认是否支持 byte[] 输入
   - [ ] 如果只支持 URL，是否需要先上传图片？

2. **SDK 响应格式**
   - [ ] `LayoutParsingResponse.getData()` 返回类型是什么？
   - [ ] 如何提取识别的文本内容？
   - [ ] 如何获取置信度信息？

3. **LayoutParsing vs OCR**
   - [ ] LayoutParsing 是否就是 OCR 功能？
   - [ ] 是否有其他更合适的 API？
   - [ ] 是否支持表格识别？

### 📚 需要查阅的文档

1. **zai-sdk 官方文档**
   - [ ] GitHub README
   - [ ] API 参考文档
   - [ ] 示例代码

2. **智谱AI OCR 文档**
   - [ ] API 接口说明
   - [ ] 请求/响应格式
   - [ ] 错误代码说明

---

## 预期收益

### 代码质量提升

| 指标 | 当前 | 目标 | 改善 |
|------|------|------|------|
| 代码行数 | ~200行 | ~100行 | ↓ 50% |
| 依赖数量 | 3个 | 1个 | ↓ 67% |
| 维护成本 | 高 | 低 | ↓ 显著 |

### 功能增强

- ✅ 自动重试机制
- ✅ 更好的错误处理
- ✅ 官方支持和更新
- ✅ 统一的API风格

### 性能优化

- ✅ 减少JSON序列化开销
- ✅ 更高效的连接管理
- ✅ 可能的性能提升（待验证）

---

## 时间估算

| 阶段 | 预计时间 | 缓冲时间 | 总计 |
|------|---------|---------|------|
| 依赖管理 | 15分钟 | 10分钟 | 25分钟 |
| 配置更新 | 10分钟 | 5分钟 | 15分钟 |
| 重写客户端 | 60分钟 | 30分钟 | 90分钟 |
| 清理代码 | 30分钟 | 15分钟 | 45分钟 |
| 测试验证 | 30分钟 | 30分钟 | 60分钟 |
| 文档更新 | 15分钟 | 10分钟 | 25分钟 |
| **总计** | **160分钟** | **100分钟** | **260分钟** |

**人天**: 约 0.5 - 1 人天

---

## 下一步行动

### 立即执行

1. **查阅 SDK 文档** ⏰ 30分钟
   - 下载 zai-sdk 源码
   - 查看示例代码
   - 确认输入/输出格式

2. **验证假设** ⏰ 30分钟
   - 创建测试项目
   - 测试 SDK 基本功能
   - 确认图片输入方式

3. **等待用户审批** ⏸️
   - 审阅本计划
   - 确认技术方案
   - 批准开始实施

---

**文档版本**: v1.0
**创建时间**: 2026-03-19
**作者**: Job Tracker Team
**状态**: ⏸️ 等待审批
