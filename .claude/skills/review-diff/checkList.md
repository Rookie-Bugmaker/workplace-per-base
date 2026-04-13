# Review checklist

## Google Java Style
- 是否出现 wildcard import
- import 是否被换行
- Java 代码是否存在明显超过 100 列且可合理换行的情况
- if / else / for / do / while 是否缺少大括号
- 是否一行写了多条语句
- 块缩进是否明显偏离 2-space 规则

## Diff discipline
- 是否混入与本次提交无关的重构
- 是否把行为变更和纯格式化混在一起
- 是否修改了不必要的文件或区域

## Hardcoding
- 是否引入了账号、密码、token、secret、密钥、证书、连接串等敏感硬编码
- 是否引入了固定 URL、IP、端口、Topic、Queue、Bucket、文件绝对路径等环境或基础设施硬编码
- 是否引入了 tenantId、orgId、userId、customerId、regionCode 等身份或租户相关硬编码
- 是否在分支逻辑中直接写死业务状态码、类型码、字典值、角色名、权限名
- 是否引入了缺少语义命名的 magic number
- 是否把相同字符串字面量在多个地方重复书写而没有抽成常量、枚举或配置
- 是否把原本应来自配置、枚举、字典、数据库或上下文的数据改成了代码字面量

## Correctness
- 是否改变了 null 处理语义
- 是否改变了异常处理语义
- 是否改变了默认值语义
- 是否改变了条件优先级、覆盖顺序或短路逻辑
- 是否引入了边界条件风险

## Review quality
- 每条意见是否定位到具体文件和改动区域
- 是否明确区分 style 问题、hardcoding 问题和 correctness 问题
- 是否避免了泛泛而谈
- 不确定的问题是否说明了不确定原因