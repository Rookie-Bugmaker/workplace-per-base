# 员工基本信息 API 接口文档

**模块**: 员工基本信息表（HrEmpBasic）
**Base Path**: `/hrEmpBasic`
**统一响应包装**: `ResponseResult<T>`

## 全局约定

### 统一响应格式 `ResponseResult<T>`

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | HTTP 状态码，200 表示成功 |
| message | String | 响应消息 |
| data | T | 业务数据，泛型 |

**成功示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": { ... }
}
```

**失败示例**:
```json
{
  "code": 500,
  "message": "查询异常: xxx",
  "data": null
}
```

### 通用字段说明

- 所有接口默认需要认证（除标注 `@TenantIgnore` 的接口）
- 分页接口统一使用 `pageNo`（页码，默认 1）和 `pageSize`（条数，默认 10）
- 文件上传接口使用 `multipart/form-data`
- 日期格式统一为 `yyyy-MM-dd`
- 时间格式统一为 `yyyy-MM-dd HH:mm:ss`
- **档案号格式示例**: `013200031192`
- **UUID 格式示例**: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`

---

## 接口列表

### 1. 图片上传

- **接口路径**: `POST /hrEmpBasic/upload/{userCode}`
- **功能说明**: 上传员工头像图片到 MinIO。根据权限决定直接更新还是走审批流：
  - 拥有 `hrsc_super_admin` 或 `hrsc_admin` 角色：直接更新
  - 其他用户：创建变更申请并启动审批流程（保存到 MongoDB）
- **Content-Type**: `multipart/form-data`

**路径参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userCode | String | 是 | 员工档案号，例如 `013200031192` |

**表单参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| imageFile | MultipartFile | 是 | 图片文件 |

**请求示例**:
```
POST /hrEmpBasic/upload/013200031192
Content-Type: multipart/form-data

imageFile: (binary)
```

**响应格式**: JSON，`ResponseResult<String>`

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 200 / 400 / 500 |
| message | String | "直接更新成功!" / "变更申请已提交" / 错误信息 |
| data | String | 直接更新时为空；走审批流时为变更申请 ID（UUID 格式，如 `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`） |

**成功响应示例**:
```json
{
  "code": 200,
  "message": "直接更新成功!",
  "data": null
}
```

**走审批流响应示例**:
```json
{
  "code": 200,
  "message": "变更申请已提交",
  "data": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
}
```

**失败响应示例**:
```json
{
  "code": 400,
  "message": "未传入档案号",
  "data": null
}
```

---

### 2. 图片下载

- **接口路径**: `GET /hrEmpBasic/download`
- **功能说明**: 根据文件 ID 获取 MinIO 中的图片访问 URL
- **请求参数**:

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| fileId | Query | String | 是 | 文件 ID（MinIO 文件标识），例如 UUID 格式 `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |

**请求示例**:
```
GET /hrEmpBasic/download?fileId=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

**响应格式**: JSON，`ResponseResult<String>`

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 200 |
| message | String | "OK" |
| data | String | 图片访问 URL |

**响应示例**:
```json
{
  "code": 200,
  "message": "OK",
  "data": "https://minio.example.com/bucket/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

---

### 3. 获取员工所属部门

- **接口路径**: `GET /hrEmpBasic/getDept`
- **功能说明**: 根据档案号查询员工所属部门编码。查询失败或部门为空时，默认返回 `SystemConstants.DEFAULT_DEPT_CODE`（流程与数字化部）。
- **请求参数**:

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| userCode | Query | String | 是 | 员工档案号，例如 `013200031192` |

**请求示例**:
```
GET /hrEmpBasic/getDept?userCode=013200031192
```

**响应格式**: JSON，`ResponseResult<String>`

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 200 |
| message | String | "获得员工所属部门成功" 或使用默认部门时的说明 |
| data | String | 部门编码 |

**响应示例**:
```json
{
  "code": 200,
  "message": "获得员工所属部门成功",
  "data": "0132"
}
```

---

### 4. 获取完整数据

- **接口路径**: `POST /hrEmpBasic/getFullData`
- **功能说明**: 根据档案号列表获取员工的完整数据，包含基本信息、任职信息、其他任职信息、专业技术资格、技能等级、职业发展通道、薪酬信息、薪酬标准等 9 个模块的数据。
- **Content-Type**: `application/json`

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| (Body) | List\<String\> | 是 | 档案号列表，例如 `["013200031192", "013200031193"]` |

**请求示例**:
```json
["013200031192", "013200031193"]
```

**响应格式**: JSON，`ResponseResult<List<HrFullDataDTO>>`

**HrFullDataDTO 结构**:

| 字段 | 类型 | 说明 |
|------|------|------|
| hrEmpBasic | HrEmpBasic | 员工基本信息 |
| hrEmpAppointment | HrEmpAppointment | 任职信息 |
| hrEmpOtherAppointment | HrEmpOtherAppointment | 其他任职信息（借调/外派/助勤） |
| hrEmpProQualification | HrEmpProfessionalQualification | 专业技术资格 |
| hrEmpSkillLevel | HrEmpSkillLevel | 技能等级 |
| hrEmpCareerPath | HrEmpCareerPath | 职业发展通道 |
| hrEmpSalaryInfo | HrEmpSalaryInfo | 薪酬信息 |
| hrEmpSalaryStandard | HrEmpSalaryStandard | 薪酬标准 |

**响应示例**:
```json
{
  "code": 200,
  "message": "OK",
  "data": [
    {
      "hrEmpBasic": { "userCode": "013200031192", "name": "张三", ... },
      "hrEmpAppointment": { ... },
      "hrEmpOtherAppointment": { ... },
      "hrEmpProQualification": { ... },
      "hrEmpSkillLevel": { ... },
      "hrEmpCareerPath": { ... },
      "hrEmpSalaryInfo": { ... },
      "hrEmpSalaryStandard": { ... }
    }
  ]
}
```

---

### 5. 获得员工基本信息（列表）

- **接口路径**: `POST /hrEmpBasic/getList`
- **功能说明**: 根据档案号列表获取员工基本信息实体（HrEmpBasic），返回的是数据库原始实体，不包含扩展解析字段。
- **Content-Type**: `application/json`
- **认证要求**: 忽略租户（`@TenantIgnore`）

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| (Body) | List\<String\> | 是 | 档案号列表 |

**请求示例**:
```json
["013200031192", "013200031193"]
```

**响应格式**: JSON，`ResponseResult<List<HrEmpBasic>>`

**响应示例**:
```json
{
  "code": 200,
  "message": "获取员工基本信息表实体成功",
  "data": [
    {
      "id": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
      "userCode": "013200031192",
      "name": "张三",
      "gender": "1",
      "dept": "0132",
      ...
    }
  ]
}
```

**与 `/getDetailList` 的区别**:
- `/getList` 返回 `HrEmpBasic` 实体列表，为数据库原始字段，不包含解析后的中文名称等扩展字段
- `/getDetailList` 返回 `HrEmpBasicDTO` 列表，继承自 `HrEmpBasic`，额外包含学历名称、学位名称、部门名称等解析后的展示字段，且会自动调用 `resolveFieldsList` 进行字段翻译

---

### 6. 获得员工基本信息（详情）

- **接口路径**: `GET /hrEmpBasic/getDetails`
- **功能说明**: 根据单个档案号获取员工详细信息（HrEmpBasicDTO），包含健康信息、户口信息、证件信息、通讯信息等跨表数据，并自动解析码值为中文展示名称。
- **请求参数**:

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| userCode | Query | String | 是 | 员工档案号，例如 `013200031192` |

**请求示例**:
```
GET /hrEmpBasic/getDetails?userCode=013200031192
```

**响应格式**: JSON，`ResponseResult<List<HrEmpBasicDTO>>`

**响应示例**:
```json
{
  "code": 200,
  "message": "获取员工基本信息表实体成功",
  "data": [
    {
      "id": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
      "userCode": "013200031192",
      "name": "张三",
      "educationValue": "本科",
      "degreeValue": "学士",
      "departmentName": "流程与数字化部",
      "mobilePhone": "13800138000",
      ...
    }
  ]
}
```

**与 `/getDetailList` 的区别**:
- `/getDetails` 仅查询单个员工（传入一个 userCode），返回 List 但通常只有一条记录
- `/getDetailList` 批量查询多个员工（传入 userCodeList）

---

### 7. 获得员工基本信息（批量详情）

- **接口路径**: `POST /hrEmpBasic/getDetailList`
- **功能说明**: 根据档案号列表批量获取员工详细信息（HrEmpBasicDTO），并自动解析码值字段为中文展示名称。
- **Content-Type**: `application/json`

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| (Body) | List\<String\> | 是 | 档案号列表 |

**请求示例**:
```json
["013200031192", "013200031193"]
```

**响应格式**: JSON，`ResponseResult<List<HrEmpBasicDTO>>`

**HrEmpBasicDTO 扩展字段**（相对于 HrEmpBasic 新增的字段）:

| 字段 | 类型 | 说明 |
|------|------|------|
| education | String | 最高学历码值 |
| educationValue | String | 最高学历（中文名称） |
| degree | String | 最高学位码值 |
| degreeValue | String | 最高学位（中文名称） |
| major | String | 所学专业 |
| quaId | String | 专业技术资格 |
| skillLevel | String | 技能等级 |
| schoolName | String | 学校 |
| certiType | String | 证件类型 |
| certiId | String | 证件号码 |
| houseHoldType | String | 户口性质 |
| houseHoldLocation | String | 户口所在地 |
| houseHoldLocationDetail | String | 户口详细地址 |
| mobilePhone | String | 移动电话 |
| workEmail | String | 办公邮箱 |
| personalEmail | String | 个人邮箱 |
| address | String | 家庭地址 |
| healthStatus | String | 健康状况 |
| wfId | Long | 流程模板 ID |
| processInstanceId | String | 流程实例 ID |
| files | List\<SysFileDTO\> | 附件文件列表 |
| position | String | 职位 |
| isEditable | String | 是否可编辑 |
| isAdmin | String | 是否超级管理员 |
| departmentName | String | 部门名称 |
| salaryMatrix | String | 薪资矩阵 |
| salaryGrade | String | 岗级 |
| positionSalary | String | 岗位工资 |
| seniorityStandard | Integer | 工龄标准 |
| languageAllowance | String | 外语补偿金 |
| disabilityAllowance | String | 伤残津贴 |
| vehicleAllowance | String | 车辆补偿金 |
| technicianAllowance | String | 技师补偿金 |
| talentAllowance | String | 人才补偿金 |
| basicSalary | String | 基础工资 |
| org | String | 组织/公司编码 |
| orgName | String | 组织/公司名称 |
| seniority | String | 工龄信息 |
| markList | List\<String\> | 变更字段标记列表 |

---

### 8. 分页查询员工信息

- **接口路径**: `POST /hrEmpBasic/page`
- **功能说明**: 分页查询员工基本信息，支持精确条件过滤。返回 `HrEmpBasicDTO` 格式。
- **Content-Type**: `application/json`

**请求参数**:

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| pageNo | Query | int | 否 | 页码，默认 1 |
| pageSize | Query | int | 否 | 条数，默认 10 |

**请求体（HrEmpBasicSearchDTO）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userCode | String | 否 | 档案号（精确匹配），例如 `013200031192` |
| name | String | 否 | 姓名（精确匹配） |
| dept | String | 否 | 部门编码（精确匹配） |
| deptSet | Set\<String\> | 否 | 部门编码集合 |
| position | String | 否 | 职位（精确匹配） |
| isEmployed | String | 否 | 是否在职（0 离职，1 在职） |
| dutyStatus | String | 否 | 在岗状态 |
| category1 | String | 否 | 人员分类 1 |

**请求示例**:
```
POST /hrEmpBasic/page?pageNo=1&pageSize=10
Content-Type: application/json

{
  "name": "张三",
  "dept": "0132",
  "isEmployed": "1"
}
```

**响应格式**: JSON，`ResponseResult<IPage<HrEmpBasicDTO>>`

**分页响应结构**:

| 字段 | 类型 | 说明 |
|------|------|------|
| records | List\<HrEmpBasicDTO\> | 当前页数据列表 |
| total | long | 总记录数 |
| size | long | 每页条数 |
| current | long | 当前页码 |
| pages | long | 总页数 |

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
        "userCode": "013200031192",
        "name": "张三",
        "dept": "0132",
        ...
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

**与 `/pageFullForOrder` 的区别**:
- `/page` 使用精确查询，所有条件为等值匹配
- `/pageFullForOrder` 支持模糊查询（name 字段可模糊匹配姓名或档案号），且额外为每条记录补充组织/公司层级信息（org、orgCode、orgName），专门为外部派入、借出业务场景设计

---

### 9. 分页查询（支持模糊查询，外部派入/借出场景）

- **接口路径**: `POST /hrEmpBasic/pageFullForOrder`
- **功能说明**: 分页查询员工信息，支持模糊查询。`name` 参数可模糊匹配姓名或档案号（全数字时自动识别为档案号）。自动补充组织/公司层级信息。
- **Content-Type**: `application/json`

**请求参数**:

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| pageNo | Query | int | 否 | 页码，默认 1 |
| pageSize | Query | int | 否 | 条数，默认 10 |

**请求体（HrEmpBasicSearchDTO）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 姓名或档案号（全数字时自动识别为档案号） |
| userCode | String | 否 | 档案号 |
| dept | String | 否 | 部门 |
| isEmployed | String | 否 | 是否在职 |
| dutyStatus | String | 否 | 在岗状态 |
| category1 | String | 否 | 人员分类 1 |
| deptSet | Set\<String\> | 否 | 部门编码集合 |
| position | String | 否 | 职位 |

**请求示例（模糊查询姓名）**:
```
POST /hrEmpBasic/pageFullForOrder?pageNo=1&pageSize=10
Content-Type: application/json

{
  "name": "张"
}
```

**请求示例（档案号查询，全数字自动识别）**:
```json
{
  "name": "013200031192"
}
```

**响应格式**: JSON，`ResponseResult<IPage<HrEmpBasicDTO>>`

> 响应中的每条记录额外包含 `org`（公司编码）、`orgCode`（公司编码，同 org）、`orgName`（公司名称）字段。

**注意事项**: 代码中 `pageNo` 在方法内部被硬编码重置为 `1`（第 878 行），传入的页码参数实际不生效。

---

### 10. 分页查询（个人信息模糊查询）

- **接口路径**: `POST /hrEmpBasic/pageForPerSelf`
- **功能说明**: 分页查询员工基本信息，使用 `HrEmpBasic` 实体作为查询条件，支持模糊查询。返回 `HrEmpBasic` 实体列表（非 DTO）。
- **Content-Type**: `application/json`

**请求参数**:

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| pageNo | Query | int | 否 | 页码，默认 1 |
| pageSize | Query | int | 否 | 条数，默认 10 |

**请求体（HrEmpBasic 实体）**:

任意 HrEmpBasic 字段均可作为模糊查询条件传入。

**请求示例**:
```
POST /hrEmpBasic/pageForPerSelf?pageNo=1&pageSize=10
Content-Type: application/json

{
  "name": "张",
  "dept": "0132"
}
```

**响应格式**: JSON，`ResponseResult<Page<HrEmpBasic>>`

**与 `/page` 的区别**:
- `/pageForPerSelf` 使用 `HrEmpBasic` 实体进行模糊查询，返回 `HrEmpBasic` 实体
- `/page` 使用 `HrEmpBasicSearchDTO` 进行精确查询，返回 `HrEmpBasicDTO`（含扩展字段）

**与 `/pageFullForOrder` 的区别**:
- `/pageForPerSelf` 返回 `Page<HrEmpBasic>` 实体，不含组织层级等扩展信息
- `/pageFullForOrder` 返回 `IPage<HrEmpBasicDTO>`，额外补充 org/orgCode/orgName

---

### 11. 分页查询异常人员

- **接口路径**: `POST /hrEmpBasic/pageForException`
- **功能说明**: 分页查询异常人员信息，支持模糊查询，自动补充组织/公司信息。
- **Content-Type**: `application/json`

**请求参数**:

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| pageNo | Query | int | 否 | 页码，默认 1 |
| pageSize | Query | int | 否 | 条数，默认 10 |

**请求体（ExceptionPersonQueryDTO）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userCode | String | 否 | 档案号，例如 `013200031192` |
| name | String | 否 | 姓名 |
| dept | String | 否 | 部门编码 |

**请求示例**:
```
POST /hrEmpBasic/pageForException?pageNo=1&pageSize=10
Content-Type: application/json

{
  "name": "张",
  "dept": "0132"
}
```

**响应格式**: JSON，`ResponseResult<Page<ExceptionPersonQueryDTO>>`

> 响应中每条记录会自动填充 `deptName`（部门名称）、`org`（公司编码）、`orgName`（公司名称）字段。

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "userCode": "013200031192",
        "name": "张三",
        "dept": "0132",
        "deptName": "流程与数字化部",
        "org": "0132",
        "orgName": "某某公司"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 12. 查询员工列表

- **接口路径**: `POST /hrEmpBasic/query`
- **功能说明**: 根据查询条件获取员工基本信息列表（非分页），使用 `HrEmpBasic` 实体作为查询条件。
- **Content-Type**: `application/json`

**请求体（HrEmpBasic 实体）**:

任意 HrEmpBasic 字段均可作为查询条件（精确匹配）。

**请求示例**:
```json
{
  "dept": "0132",
  "isEmployed": "1"
}
```

**响应格式**: JSON，`ResponseResult<List<HrEmpBasic>>`

**响应示例**:
```json
{
  "code": 200,
  "message": "获取员工基本信息表列表成功",
  "data": [
    {
      "id": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
      "userCode": "013200031192",
      "name": "张三",
      "dept": "0132",
      ...
    }
  ]
}
```

---

### 13. 获取员工基本信息（单条）

- **接口路径**: `GET /hrEmpBasic/get`
- **功能说明**: 根据主键 ID 获取单条员工基本信息记录。
- **请求参数**:

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| id | Query | Long | 是 | 主键 ID |

**请求示例**:
```
GET /hrEmpBasic/get?id=1
```

**响应格式**: JSON，`ResponseResult<HrEmpBasic>`

---

### 14. 创建员工基本信息

- **接口路径**: `POST /hrEmpBasic/create`
- **功能说明**: 创建一条新的员工基本信息记录。
- **Content-Type**: `application/json`
- **事务**: 开启事务

**请求体（HrEmpBasic 实体）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userCode | String | 是 | 工号/档案号，例如 `013200031192` |
| name | String | 否 | 姓名 |
| gender | String | 否 | 性别 |
| nation | String | 否 | 民族 |
| nativePlaceName | String | 否 | 籍贯名称 |
| nationality | String | 否 | 国籍 |
| birthDate | String | 否 | 出生日期 (yyyy-MM-dd) |
| age | Integer | 否 | 年龄 |
| birthPlace | String | 否 | 出生地 |
| politicalValue | String | 否 | 政治面貌 |
| partyJoinDate | String | 否 | 加入党组织时间 (yyyy-MM-dd) |
| workDate | String | 否 | 开始工作日期 (yyyy-MM-dd) |
| entryDate | String | 否 | 入司日期 (yyyy-MM-dd) |
| empSource | String | 否 | 员工来源 |
| maritalStatus | String | 否 | 婚姻状况 |
| identityType | String | 否 | 身份类型（001 干，002 工） |
| empJob | String | 否 | 职务层级 |
| empRank | String | 否 | 职级层级 |
| campusPeriod | String | 否 | 校招届数 |
| hireDate | String | 否 | 入职日期 (yyyy-MM-dd) |
| nativePlaceCode | String | 否 | 籍贯编码 |
| dept | String | 否 | 部门编码 |
| costCenter | String | 否 | 成本中心 |
| isUpdating | String | 否 | 是否存在正在被修改的状态（0 否，1 是） |
| category1 | String | 否 | 人员分类 1 |
| category4 | String | 否 | 人员分类 4 |
| category7 | String | 否 | 人员分类 7 |
| category8 | String | 否 | 人员分类 8 |
| status | String | 否 | 人员在岗状态 |
| enlistmentDate | String | 否 | 入伍时间 (yyyy-MM-dd) |
| formerName | String | 否 | 曾用名 |
| promotionTime | String | 否 | 提中干时间 (yyyy-MM-dd) |
| deathTime | String | 否 | 死亡时间 (yyyy-MM-dd) |
| serviceBreakTime | String | 否 | 工龄间断时间 |
| isEmployed | String | 否 | 是否离职标记（0 离职，1 在职） |
| empType | String | 否 | 人员类别（校社京博工挂他临） |
| fileHeaderId | String | 否 | 附件挂载 ID |
| profilePicture | String | 否 | 用户图片文件 ID |
| attendanceImageId | String | 否 | 考勤打卡图片 ID |
| remark | String | 否 | 备注 |
| orgCode | String | 否 | 所属部门编码 |
| deleted | Integer | 否 | 删除标记 |

**BaseEntity 继承字段**（自动填充，无需传入）:

| 字段 | 类型 | 说明 |
|------|------|------|
| createBy | Long | 创建人 |
| createTime | LocalDateTime | 创建时间 |
| updateBy | Long | 更新人 |
| updateTime | LocalDateTime | 更新时间 |

**请求示例**:
```json
{
  "userCode": "013200031192",
  "name": "张三",
  "gender": "1",
  "dept": "0132",
  "isEmployed": "1"
}
```

**响应格式**: JSON，`ResponseResult<Boolean>`

**响应示例**:
```json
{
  "code": 200,
  "message": "创建员工基本信息表实体成功",
  "data": true
}
```

---

### 15. 暂存员工信息

- **接口路径**: `POST /hrEmpBasic/tempSave`
- **功能说明**: 将员工信息暂存到 MongoDB（集合 `hr_emp_basic_temp_save_data`），用于后续提交或恢复。
- **Content-Type**: `application/json`

**请求体（HrEmpBasicDTO）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userCode | String | 是 | 员工档案号，例如 `013200031192` |
| 其他字段 | - | 否 | 同 HrEmpBasicDTO 扩展字段 |

**请求示例**:
```json
{
  "userCode": "013200031192",
  "name": "张三",
  "mobilePhone": "13800138000",
  "healthStatus": "1"
}
```

**响应格式**: JSON，`ResponseResult<String>`

**响应示例**:
```json
{
  "code": 200,
  "message": "OK",
  "data": null
}
```

---

### 16. 查询暂存/驳回数据

- **接口路径**: `POST /hrEmpBasic/queryData/{type}`
- **功能说明**: 查询暂存或驳回的员工数据。`type` 为 `tempSave` 时查询暂存数据，否则查询提交数据。如果 MongoDB 中没有对应数据，则返回数据库中的最新信息。
- **Content-Type**: `application/json`

**路径参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 是 | 查询类型：`tempSave`（暂存）或其他（驳回/提交数据） |

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userCode | String | 是 | 员工档案号，例如 `013200031192` |
| id | String | 否 | 主键 ID |

**请求示例**:
```
POST /hrEmpBasic/queryData/tempSave
Content-Type: application/json

{
  "userCode": "013200031192",
  "id": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
}
```

**响应格式**: JSON，`ResponseResult<HrEmpBasicDTO>`

---

### 17. 提交员工信息变更申请

- **接口路径**: `POST /hrEmpBasic/submit-change`
- **功能说明**: 提交员工信息变更申请。根据权限决定直接更新还是走审批流：
  - 拥有 `hrsc_super_admin` 角色，或员工为非在职状态（`isEmployed=0`）且拥有 `hrsc_emp_non_inc_info_mgr` 角色：直接更新
  - 其他用户：创建变更申请、保存到 MongoDB、启动审批流程
- **Content-Type**: `application/json`

**请求体（HrEmpBasicDTO）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userCode | String | 是 | 员工档案号，例如 `013200031192` |
| wfId | Long | 否 | 流程模板 ID |
| 其他字段 | - | 否 | 同 HrEmpBasicDTO 扩展字段 |

> 注意：请求体中的 `dept` 和 `orgCode` 字段会被强制置空（防止误修改）。

**请求示例**:
```json
{
  "userCode": "013200031192",
  "name": "张三",
  "mobilePhone": "13900139000",
  "healthStatus": "1",
  "wfId": 123
}
```

**响应格式**: JSON，`ResponseResult<String>`

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 200 / 404 / 500 |
| message | String | "直接更新成功!" / "变更申请已提交" / 错误信息 |
| data | String | 变更申请 ID（UUID 格式，如 `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`） |

**成功响应示例（走审批流）**:
```json
{
  "code": 200,
  "message": "变更申请已提交",
  "data": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
}
```

**失败响应示例**:
```json
{
  "code": 404,
  "message": "员工信息不存在",
  "data": null
}
```

---

### 18. 获取变更详情

- **接口路径**: `GET /hrEmpBasic/change-detail`
- **功能说明**: 根据变更申请 ID 从 MongoDB 获取变更详情，包含原始数据和审批流中的数据对比。
- **请求参数**:

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| requestId | Query | String | 是 | 变更申请 ID，UUID 格式，如 `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6` |

**请求示例**:
```
GET /hrEmpBasic/change-detail?requestId=basic_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

**响应格式**: JSON，`ResponseResult<Map<String, Object>>`

**响应 data 结构**:

| 字段 | 类型 | 说明 |
|------|------|------|
| originalData | Map | 原始数据（变更前的快照） |
| changedData | Map | 审批流中的数据（变更申请内容） |
| requestId | String | 变更申请 ID |
| createTime | String | 创建时间 |

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "originalData": { "userCode": "013200031192", "name": "张三", ... },
    "changedData": { "userCode": "013200031192", "name": "张三", "mobilePhone": "13900139000", ... },
    "requestId": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
    "createTime": "2025-01-01T10:00:00"
  }
}
```

---

### 19. 更新员工基本信息

- **接口路径**: `POST /hrEmpBasic/update`
- **功能说明**: 更新员工基本信息。该接口会跨表更新健康信息（HrEmpHealth）、户口信息（HrEmpHouseHold）、证件信息（HrEmpCertificate）、通讯信息（HrEmpCommunication）以及基本信息（HrEmpBasic）。如果 `id` 为空则先保存再更新。
- **Content-Type**: `application/json`

**请求体（HrEmpBasicDTO）**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 否 | 主键 ID，为空时自动创建 |
| userCode | String | 是 | 员工档案号，例如 `013200031192` |
| healthStatus | String | 否 | 健康状况（跨表保存至 HrEmpHealth） |
| houseHoldType | String | 否 | 户口性质（跨表保存至 HrEmpHouseHold） |
| houseHoldLocation | String | 否 | 户口所在地（跨表保存至 HrEmpHouseHold） |
| houseHoldLocationDetail | String | 否 | 户口详细地址（跨表保存至 HrEmpHouseHold） |
| certiType | String | 否 | 证件类型（跨表保存至 HrEmpCertificate） |
| certiId | String | 否 | 证件号码（跨表保存至 HrEmpCertificate） |
| mobilePhone | String | 否 | 移动电话（跨表保存至 HrEmpCommunication） |
| workEmail | String | 否 | 办公邮箱（跨表保存至 HrEmpCommunication） |
| personalEmail | String | 否 | 个人邮箱（跨表保存至 HrEmpCommunication） |
| address | String | 否 | 家庭地址（跨表保存至 HrEmpCommunication） |
| 其他字段 | - | 否 | 同 HrEmpBasic / HrEmpBasicDTO 字段 |

**请求示例**:
```json
{
  "id": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
  "userCode": "013200031192",
  "name": "张三",
  "healthStatus": "1",
  "houseHoldType": "城镇户口",
  "mobilePhone": "13800138000",
  "workEmail": "zhangsan@example.com"
}
```

**响应格式**: JSON，`ResponseResult<String>`

**响应示例**:
```json
{
  "code": 200,
  "message": "创建员工基本信息表实体成功",
  "data": null
}
```

---

### 20. 删除员工基本信息

- **接口路径**: `GET /hrEmpBasic/delete`
- **功能说明**: 根据主键 ID 删除员工基本信息记录（逻辑删除，`deleted` 设为 1）。
- **请求参数**:

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| id | Query | Long | 是 | 主键 ID |

**请求示例**:
```
GET /hrEmpBasic/delete?id=1
```

**响应格式**: JSON，`ResponseResult<Boolean>`

**响应示例**:
```json
{
  "code": 200,
  "message": "删除员工基本信息表实体成功",
  "data": true
}
```

---

### 21. 导出员工基本信息报表

- **接口路径**: `POST /hrEmpBasic/export`
- **功能说明**: 根据查询条件导出员工基本信息为 Excel/CSV/XLS 文件。
- **Content-Type**: `application/json`（查询条件） + 文件流（响应）

**请求参数**:

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| type | Query | String | 否 | 文件格式，可选 `xlsx`（默认）/ `xls` / `csv` |

**请求体（HrEmpBasic 实体）**:

任意 HrEmpBasic 字段均可作为查询条件，仅导出匹配的记录。

**请求示例**:
```
POST /hrEmpBasic/export?type=xlsx
Content-Type: application/json

{
  "dept": "0132",
  "isEmployed": "1"
}
```

**响应格式**: 文件流（Excel 或 CSV）

**响应头**:
- `Content-Type`: 根据 type 参数动态设置
- `Content-Disposition`: `attachment;filename=员工基本信息表报表-{timestamp}.{type}`

---

### 22. 导入员工基本信息

- **接口路径**: `POST /hrEmpBasic/import`
- **功能说明**: 通过 Excel 文件批量导入员工基本信息。
- **Content-Type**: `multipart/form-data`

**表单参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | Excel 文件，第一行为表头 |

**请求示例**:
```
POST /hrEmpBasic/import
Content-Type: multipart/form-data

file: (binary Excel file)
```

**响应格式**: JSON，`ResponseResult<Boolean>`

**响应示例**:
```json
{
  "code": 200,
  "message": "导入员工基本信息表报表成功",
  "data": null
}
```

---

### 23. 导入政治面貌

- **接口路径**: `POST /hrEmpBasic/importPoliticalStatus`
- **功能说明**: 通过 Excel 文件批量导入员工政治面貌信息。
- **Content-Type**: `multipart/form-data`
- **事务**: 开启事务

**表单参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | Excel 文件，表头第一行为列名，包含"档案号"和"政治面貌"列 |

**请求示例**:
```
POST /hrEmpBasic/importPoliticalStatus
Content-Type: multipart/form-data

file: (binary Excel file)
```

**响应格式**: JSON，`ResponseResult<String>`

**响应示例**:
```json
{
  "code": 200,
  "message": "OK",
  "data": null
}
```

---

### 24. 导出政治面貌导入模板

- **接口路径**: `POST /hrEmpBasic/export/politicalStatus/template`
- **功能说明**: 导出政治面貌导入模板 Excel，包含"档案号"和"政治面貌"两列空表头。
- **请求参数**:

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| type | Query | String | 否 | 文件格式，可选 `xlsx`（默认）/ `xls` / `csv` |

**请求示例**:
```
POST /hrEmpBasic/export/politicalStatus/template?type=xlsx
```

**响应格式**: 文件流（Excel 或 CSV）

**响应头**:
- `Content-Type`: 根据 type 参数动态设置
- `Content-Disposition`: `attachment;filename=政治面貌导入模板报表-{timestamp}.{type}`

---

### 25. 获取在岗状态字典

- **接口路径**: `GET /hrEmpBasic/getDutyStatusDict`
- **功能说明**: 获取所有在岗状态字典，包含在岗状态和人员分类 8 中的"助"、"派"、"借"三个状态。
- **请求参数**: 无

**请求示例**:
```
GET /hrEmpBasic/getDutyStatusDict
```

**响应格式**: JSON，`ResponseResult<List<MapVO<String, String>>>`

**MapVO 结构**:

| 字段 | 类型 | 说明 |
|------|------|------|
| key | String | 字典码值 |
| value | String | 字典中文名称 |

**响应示例**:
```json
{
  "code": 200,
  "message": "获取字典成功",
  "data": [
    { "key": "101", "value": "在岗" },
    { "key": "201", "value": "待岗" },
    { "key": "202", "value": "859内退人员" }
  ]
}
```

---

## 接口对比总结

### 查询类接口对比

| 接口 | 方法 | 返回类型 | 查询方式 | 是否分页 | 扩展字段 | 主要用途                   |
|------|------|----------|----------|----------|----------|------------------------|
| `/getList` | POST | `List<HrEmpBasic>` | 档案号列表精确查询 | 否 | 无 | 批量获取原始实体               |
| `/getDetails` | GET | `List<HrEmpBasicDTO>` | 单个档案号查询 | 否 | 有 | 单个员工详情                 |
| `/getDetailList` | POST | `List<HrEmpBasicDTO>` | 档案号列表精确查询 | 否 | 有 | 批量获取详情                 |
| `/get` | GET | `HrEmpBasic` | 主键 ID 精确查询 | 否 | 无 | 单条记录查询                 |
| `/getFullData` | POST | `List<HrFullDataDTO>` | 档案号列表精确查询 | 否 | 9 模块全量数据 | 获取员工完整信息，人事令前端带入详细信息使用 |
| `/page` | POST | `IPage<HrEmpBasicDTO>` | 精确条件过滤 | 是 | 有 | 常规分页查询                 |
| `/pageFullForOrder` | POST | `IPage<HrEmpBasicDTO>` | 模糊查询 | 是 | 有 + 组织层级 | 人事令前端选人使用              |
| `/pageForPerSelf` | POST | `Page<HrEmpBasic>` | 模糊查询 | 是 | 无 | 个人信息查询                 |
| `/pageForException` | POST | `Page<ExceptionPersonQueryDTO>` | 模糊查询 | 是 | 有 | 外部派入，借出接口              |
| `/query` | POST | `List<HrEmpBasic>` | 精确条件过滤 | 否 | 无 | 非分页列表查询                |

### 导入/导出类接口对比

| 接口 | 方法 | 输入 | 输出 | 说明 |
|------|------|------|------|------|
| `/import` | POST | Excel | Boolean | 导入员工基本信息 |
| `/export` | POST | 查询条件 + type | 文件流 | 导出员工基本信息 |
| `/importPoliticalStatus` | POST | Excel | String | 导入政治面貌 |
| `/export/politicalStatus/template` | POST | type | 文件流 | 导出政治面貌模板 |

### 变更管理类接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/upload/{userCode}` | POST | 上传员工头像 |
| `/download` | GET | 下载图片 URL |
| `/tempSave` | POST | 暂存员工信息到 MongoDB |
| `/queryData/{type}` | POST | 查询暂存/驳回数据 |
| `/submit-change` | POST | 提交变更申请（直接更新或走审批流） |
| `/change-detail` | GET | 获取变更申请详情 |
