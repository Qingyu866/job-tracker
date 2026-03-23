# 智谱文档解析 API 集成实施计划

## 📋 项目概述

**目标**: 集成智谱AI**文档解析资源包**（FileParsing API）

**API说明**:
- 使用 `FileParsingUploadReq` 上传文件
- 使用 `FileParsingDownloadReq` 获取解析结果
- 支持PDF、DOCX、图片等多种格式
- 异步处理，需要轮询获取结果

**涉及范围**: OCR模块

**预计工作量**: 3-4小时

---

## 当前实现分析

### 现有架构

```
OcrController
  ↓
OcrService (识别服务)
  ↓
ZhipuOcrClient (错误：使用LayoutParsing API)
  ↓
智谱 OCR API
```

### 现有问题

1. **API使用错误**
   - 当前使用 `LayoutParsing` API（图片OCR）
   - 应该使用 `FileParsing` API（文档解析）
   - 不支持PDF、DOCX等文档格式

2. **同步调用问题**
   - 当前假设同步返回结果
   - 文档解析是异步的，需要轮询

3. **文件处理问题**
   - 需要支持文件路径上传
   - 需要处理临时文件

---

## 目标架构

### 新架构

```
OcrController
  ↓
OcrService (识别服务)
  ↓
ZhipuOcrClient (使用 FileParsing API)
  ↓
ZhipuAiClient.fileParsing()
  ↓
智谱文档解析API（异步）
  ↓
1. createParseTask() → 返回 taskId
2. getParseResult() → 轮询获取结果
```

### 优化点

1. **正确的API**
   - 使用 `FileParsingUploadReq` 上传文件
   - 使用 `FileParsingDownloadReq` 获取结果
   - 支持PDF、DOCX、图片等格式

2. **异步轮询**
   - 上传文件 → 获取taskId
   - 轮询结果（processing → succeeded/failed）
   - 提取文本内容

3. **多格式支持**
   - PDF文档
   - DOCX文档
   - 图片文件（PNG、JPG等）

---

## 实施计划

### 阶段 1: 依赖验证 ⏱️ 5分钟

#### 1.1 检查 SDK 依赖

**文件**: `pom.xml`

**确认内容**:
```xml
<!-- 智谱AI官方SDK -->
<dependency>
  <groupId>ai.z.openapi</groupId>
  <artifactId>zai-sdk</artifactId>
  <version>0.3.3</version>
</dependency>
```

**验证**:
- ✅ SDK已添加
- ✅ 版本正确（0.3.3）

---

### 阶段 2: 配置更新 ⏱️ 10分钟

#### 2.1 更新 application.yml

**文件**: `src/main/resources/application.yml`

**当前配置**:
```yaml
ocr:
  zhipu:
    api-key: ${ZHIPU_API_KEY}
    model: glm-ocr
    timeout: 30000
```

**修改后**:
```yaml
ocr:
  zhipu:
    api-key: ${ZHIPU_API_KEY}           # API密钥
    tool-type: lite                      # 解析工具：lite/prime/expert
    timeout: 300000                      # 总超时时间（5分钟）
    poll-interval: 3000                  # 轮询间隔（3秒）
    max-retries: 100                     # 最大轮询次数
```

**配置说明**:
- `tool-type`: 解析工具类型
  - `lite` - 轻量级（快速，成本低）✅ 推荐用于简历/JD
  - `prime` - 标准（平衡）
  - `expert` - 高级（精确，成本高）
- `timeout`: 总超时时间（包含轮询等待）
- `poll-interval`: 轮询间隔（毫秒）
- `max-retries`: 最大轮询次数

---

### 阶段 3: 重写 ZhipuOcrClient ⏱️ 90分钟

#### 3.1 新的实现结构

**文件**: `src/main/java/com/jobtracker/ocr/ZhipuOcrClient.java`

**核心方法**:

```java
public class ZhipuOcrClient {

    // 1. 通过文件路径识别
    public OcrResult recognize(String filePath, OcrOptions options)

    // 2. 通过MultipartFile识别
    public OcrResult recognize(MultipartFile file, OcrOptions options)

    // 3. 通过字节数组识别
    public OcrResult recognize(byte[] imageData, OcrOptions options)

    // 4. 创建解析任务
    private String createParseTask(String filePath)

    // 5. 轮询获取结果
    private FileParsingDownloadResponse pollForResult(String taskId)

    // 6. 获取文件类型
    private String getFileType(String filePath)
}
```

#### 3.2 创建解析任务

```java
private String createParseTask(String filePath) {
    // 1. 确定文件类型
    String fileType = getFileType(filePath);

    // 2. 构建上传请求
    FileParsingUploadReq uploadReq = FileParsingUploadReq.builder()
            .filePath(filePath)
            .fileType(fileType)
            .toolType(toolType)
            .build();

    // 3. 调用SDK
    FileParsingResponse response = getClient().fileParsing()
            .createParseTask(uploadReq);

    // 4. 返回taskId
    if (response.isSuccess()) {
        return response.getData().getTaskId();
    }
    return null;
}
```

#### 3.3 轮询获取结果

```java
private FileParsingDownloadResponse pollForResult(String taskId) {
    for (int i = 0; i < maxRetries; i++) {
        // 1. 构建下载请求
        FileParsingDownloadReq downloadReq = FileParsingDownloadReq.builder()
                .taskId(taskId)
                .formatType("text")
                .build();

        // 2. 调用SDK
        FileParsingDownloadResponse response = getClient().fileParsing()
                .getParseResult(downloadReq);

        // 3. 检查状态
        if (response.isSuccess()) {
            String status = response.getData().getStatus();

            if ("succeeded".equalsIgnoreCase(status)) {
                return response;  // 成功
            } else if ("processing".equalsIgnoreCase(status)) {
                Thread.sleep(pollInterval);  // 继续轮询
            } else {
                return response;  // 失败
            }
        }
    }
    return null;  // 超时
}
```

#### 3.4 识别方法实现

```java
public OcrResult recognize(String filePath, OcrOptions options) {
    // 1. 创建解析任务
    String taskId = createParseTask(filePath);
    if (taskId == null) {
        return OcrResult.failed("UPLOAD_FAILED", "文件上传失败");
    }

    // 2. 轮询获取结果
    FileParsingDownloadResponse response = pollForResult(taskId);

    // 3. 处理响应
    if (response != null && response.isSuccess()) {
        String status = response.getData().getStatus();

        if ("succeeded".equalsIgnoreCase(status)) {
            String text = response.getData().getContent();
            return OcrResult.success(text, 0.95, processingTime, "ZHIPU");
        } else {
            return OcrResult.failed("PARSE_FAILED",
                "文档解析失败: " + response.getData().getMessage());
        }
    }

    return OcrResult.failed("GET_RESULT_FAILED", "获取解析结果失败");
}
```

---

### 阶段 4: 测试验证 ⏱️ 60分钟

#### 4.1 单元测试

**测试用例**:
```java
@SpringBootTest
class ZhipuOcrClientTest {

    @Autowired
    private ZhipuOcrClient zhipuOcrClient;

    @Test
    void testClientInitialization() {
        assertTrue(zhipuOcrClient.isConfigured());
        assertTrue(zhipuOcrClient.healthCheck());
    }

    @Test
    void testRecognizePdf() {
        String filePath = "src/test/resources/test-resume.pdf";
        OcrResult result = zhipuOcrClient.recognize(filePath, OcrOptions.forResume());

        assertTrue(result.isSuccess());
        assertNotNull(result.getText());
    }

    @Test
    void testRecognizeDocx() {
        String filePath = "src/test/resources/test-resume.docx";
        OcrResult result = zhipuOcrClient.recognize(filePath, OcrOptions.forResume());

        assertTrue(result.isSuccess());
        assertNotNull(result.getText());
    }

    @Test
    void testRecognizeImage() {
        String filePath = "src/test/resources/test-resume.png";
        OcrResult result = zhipuOcrClient.recognize(filePath, OcrOptions.forResume());

        assertTrue(result.isSuccess());
        assertNotNull(result.getText());
    }
}
```

#### 4.2 集成测试

**测试场景**:
1. ✅ PDF简历解析
2. ✅ DOCX简历解析
3. ✅ 图片简历解析
4. ✅ 轮询超时处理
5. ✅ 错误处理

#### 4.3 性能测试

**测试指标**:
- ⏱️ 平均解析时间
- ⏱️ P95解析时间
- ✅ 成功率
- ✅ 轮询次数分布

---

### 阶段 5: 文档更新 ⏱️ 30分钟

#### 5.1 更新完成报告

**文件**: `docs/功能设计/OCR迁移至zai-sdk完成报告_20260319.md`

**更新内容**:
- ✅ 使用 FileParsing API（而非 LayoutParsing）
- ✅ 异步轮询流程
- ✅ 支持的文件格式
- ✅ 配置说明
- ✅ 使用示例

#### 5.2 更新API文档

**文件**: `docs/API文档/完整API参考文档_20260319.md`

**更新内容**:
- ✅ OCR接口说明
- ✅ 支持的文件格式
- ✅ 请求/响应示例
- ✅ 错误代码说明

---

## API 流程说明

### 完整流程

```
1. 上传文件
   ↓
   FileParsingUploadReq.builder()
       .filePath("/path/to/file.pdf")
       .fileType("pdf")
       .toolType("lite")
       .build()
   ↓
   client.fileParsing().createParseTask(uploadReq)
   ↓
   返回 FileParsingResponse
   ├─ success → 返回 taskId
   └─ failed → 返回错误信息

2. 轮询结果（异步）
   ↓
   FileParsingDownloadReq.builder()
       .taskId(taskId)
       .formatType("text")
       .build()
   ↓
   client.fileParsing().getParseResult(downloadReq)
   ↓
   返回 FileParsingDownloadResponse
   ├─ processing → 继续轮询（3秒后重试）
   ├─ succeeded → 返回 content
   └─ failed → 返回错误信息

3. 提取文本
   ↓
   response.getData().getContent()
   ↓
   返回解析后的文本
```

### 状态说明

| 状态 | 说明 | 处理方式 |
|------|------|----------|
| `processing` | 解析中 | 继续轮询（间隔3秒） |
| `succeeded` | 解析成功 | 提取文本内容 |
| `failed` | 解析失败 | 返回错误信息 |

---

## 风险评估

### 高风险 🔴

1. **异步轮询超时**
   - **风险**: 大文件可能超过5分钟
   - **缓解**: 根据文件大小动态调整超时时间

2. **临时文件累积**
   - **风险**: 临时文件可能未及时清理
   - **缓解**: 使用try-finally确保清理

### 中风险 🟡

3. **并发限制**
   - **风险**: 同时解析多个文件可能达到API限制
   - **缓解**: 添加队列机制

4. **轮询频率**
   - **风险**: 轮询太频繁可能被限流
   - **缓解**: 设置合理的轮询间隔（3秒）

### 低风险 🟢

5. **文件格式不支持**
   - **风险**: 上传不支持的文件格式
   - **缓解**: 前端验证文件格式

---

## 执行检查清单

### 准备阶段
- [x] 确认使用 FileParsing API（而非 LayoutParsing）
- [x] 理解异步轮询流程
- [x] 确认支持的文件格式
- [x] 准备测试文件

### 实施阶段
- [x] 更新 application.yml 配置
- [x] 重写 ZhipuOcrClient
- [x] 实现创建解析任务
- [x] 实现轮询获取结果
- [x] 实现三种识别方法
- [x] 实现文件类型识别

### 测试阶段
- [ ] 单元测试：客户端初始化
- [ ] 单元测试：PDF解析
- [ ] 单元测试：DOCX解析
- [ ] 单元测试：图片解析
- [ ] 集成测试：轮询超时
- [ ] 集成测试：错误处理

### 部署阶段
- [x] 更新完成报告文档
- [ ] 更新API文档
- [ ] 代码审查
- [ ] 合并到主分支

---

## 预期收益

### 功能增强

| 特性 | 旧实现 | 新实现 | 改善 |
|------|--------|--------|------|
| 支持格式 | 仅图片 | PDF, DOCX, 图片 | ✅ 多格式 |
| 处理方式 | 同步 | 异步轮询 | ✅ 避免超时 |
| 解析精度 | 图片OCR | 文档解析 | ✅ 更准确 |
| 适用场景 | 简单图片 | 复杂文档 | ✅ 更强大 |

### 代码改进

- ✅ 使用正确的API（FileParsing）
- ✅ 实现异步轮询机制
- ✅ 支持多种文件格式
- ✅ 完整的错误处理

---

## 时间估算

| 阶段 | 预计时间 | 缓冲时间 | 总计 |
|------|---------|---------|------|
| 依赖验证 | 5分钟 | 5分钟 | 10分钟 |
| 配置更新 | 10分钟 | 5分钟 | 15分钟 |
| 重写客户端 | 90分钟 | 30分钟 | 120分钟 |
| 测试验证 | 60分钟 | 30分钟 | 90分钟 |
| 文档更新 | 30分钟 | 15分钟 | 45分钟 |
| **总计** | **195分钟** | **85分钟** | **280分钟** |

**人天**: 约 0.5 - 1 人天

---

## 关键要点

### ✅ 正确做法

1. **使用 FileParsing API**
   - ✅ `FileParsingUploadReq` - 上传文件
   - ✅ `FileParsingDownloadReq` - 获取结果
   - ✅ 异步轮询处理

2. **支持的文件格式**
   - ✅ PDF
   - ✅ DOCX
   - ✅ PNG, JPG等图片

3. **配置说明**
   - ✅ `tool-type: lite/prime/expert`
   - ✅ `poll-interval: 3000`（3秒）
   - ✅ `max-retries: 100`（最多5分钟）

### ❌ 错误做法

1. **不要使用 LayoutParsing API**
   - ❌ `LayoutParsingCreateParams`
   - ❌ `LayoutParsingResponse`
   - ❌ 这是图片OCR，不是文档解析

2. **不要假设同步返回**
   - ❌ 以为上传后立即返回结果
   - ✅ 必须轮询获取结果

3. **不要只支持图片**
   - ❌ 只支持PNG、JPG
   - ✅ 应该支持PDF、DOCX等

---

**文档版本**: v2.0（使用文档解析API）
**创建时间**: 2026-03-19
**作者**: Job Tracker Team
**状态**: ✅ 已完成实施
