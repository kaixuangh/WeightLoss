# WeightLoss 后端接口文档

## 基础信息

- **Base URL**: `https://api.example.com/v1`
- **数据格式**: JSON
- **认证方式**: Bearer Token (JWT)

---

## 1. 用户认证

### 1.1 注册

**POST** `/auth/register`

**Request Body**:
```json
{
  "username": "user123",
  "password": "password123",
  "confirmPassword": "password123"
}
```

**Response** `200`:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": "10001",
    "username": "user123",
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 604800
  }
}
```

---

### 1.2 登录

**POST** `/auth/login`

**Request Body**:
```json
{
  "username": "user123",
  "password": "password123"
}
```

**Response** `200`:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": "10001",
    "username": "user123",
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 604800
  }
}
```

---

### 1.3 刷新 Token

**POST** `/auth/refresh`

**Headers**: `Authorization: Bearer <token>`

**Response** `200`:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 604800
  }
}
```

---

### 1.4 退出登录

**POST** `/auth/logout`

**Headers**: `Authorization: Bearer <token>`

**Response** `200`:
```json
{
  "code": 0,
  "message": "success"
}
```

---

### 1.5 修改密码

**PUT** `/auth/password`

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "oldPassword": "password123",
  "newPassword": "newpassword123"
}
```

**Response** `200`:
```json
{
  "code": 0,
  "message": "success"
}
```

---

## 2. 用户设置

### 2.1 获取用户设置

**GET** `/user/settings`

**Headers**: `Authorization: Bearer <token>`

**Response** `200`:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "height": 175.0,
    "targetWeight": 65.0,
    "weightUnit": "KG",
    "reminderEnabled": true,
    "reminderTime": "08:00"
  }
}
```

---

### 2.2 更新用户设置

**PUT** `/user/settings`

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "height": 175.0,
  "targetWeight": 65.0,
  "weightUnit": "KG",
  "reminderEnabled": true,
  "reminderTime": "08:00"
}
```

> 所有字段均为可选，只传需要更新的字段

**Response** `200`:
```json
{
  "code": 0,
  "message": "success"
}
```

---

## 3. 体重记录

### 3.1 添加/更新体重记录

**POST** `/weight/record`

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "date": "2026-01-28",
  "weight": 76.0
}
```

> 同一天重复提交会覆盖之前的记录

**Response** `200`:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "1001",
    "date": "2026-01-28",
    "weight": 76.0,
    "createdAt": "2026-01-28T17:23:14+08:00",
    "updatedAt": "2026-01-28T17:23:14+08:00"
  }
}
```

---

### 3.2 获取体重记录列表

**GET** `/weight/records`

**Headers**: `Authorization: Bearer <token>`

**Query Parameters**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| startDate | string | 否 | 开始日期 (YYYY-MM-DD) |
| endDate | string | 否 | 结束日期 (YYYY-MM-DD) |
| days | int | 否 | 最近天数 (默认30) |

**示例**: `/weight/records?days=30`

**Response** `200`:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [
      {
        "id": "1001",
        "date": "2026-01-28",
        "weight": 76.0
      },
      {
        "id": "1000",
        "date": "2026-01-27",
        "weight": 76.5
      }
    ],
    "statistics": {
      "maxWeight": 76.5,
      "minWeight": 76.0,
      "avgWeight": 76.25,
      "latestWeight": 76.0,
      "change": -0.5
    }
  }
}
```

---

### 3.3 获取单条体重记录

**GET** `/weight/record/{date}`

**Headers**: `Authorization: Bearer <token>`

**示例**: `/weight/record/2026-01-28`

**Response** `200`:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "1001",
    "date": "2026-01-28",
    "weight": 76.0,
    "createdAt": "2026-01-28T17:23:14+08:00",
    "updatedAt": "2026-01-28T17:23:14+08:00"
  }
}
```

---

### 3.4 删除体重记录

**DELETE** `/weight/record/{id}`

**Headers**: `Authorization: Bearer <token>`

**Response** `200`:
```json
{
  "code": 0,
  "message": "success"
}
```

---

## 错误码

| code | 说明 |
|------|------|
| 0 | 成功 |
| 1001 | 参数错误 |
| 1002 | 用户名已存在 |
| 1003 | 用户不存在 |
| 1004 | 密码错误 |
| 1005 | 两次密码不一致 |
| 2001 | Token 无效 |
| 2002 | Token 过期 |
| 3001 | 记录不存在 |
| 5000 | 服务器内部错误 |

**错误响应示例**:
```json
{
  "code": 1004,
  "message": "密码错误"
}
```

---

## 通用 Headers

所有需要认证的接口必须携带以下 Header：

```
Authorization: Bearer <token>
Content-Type: application/json
```

---

## 数据说明

### weightUnit 枚举值

| 值 | 说明 |
|---|------|
| KG | 公斤 |
| JIN | 斤 |

### 体重存储

所有体重数据在后端统一以 **kg** 为单位存储，客户端根据用户设置的 `weightUnit` 进行显示转换。
