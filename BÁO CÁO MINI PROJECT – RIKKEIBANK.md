# BÁO CÁO MINI PROJECT – RIKKEIBANK API

## 1. Mục tiêu và phạm vi

Xây dựng hệ thống Backend **RikkeiBank API** theo kiến trúc Microservice bằng Java Spring Boot, phục vụ quản lý khách hàng, tài khoản và giao dịch chuyển khoản.

Hệ thống hướng đến các yêu cầu chính: **bảo mật, phân quyền, khả năng mở rộng, xử lý lỗi, giao tiếp bất đồng bộ và đảm bảo nhất quán dữ liệu giữa các service**.

## 2. Kiến trúc hệ thống

Hệ thống được chuyển đổi từ mô hình Monolithic sang **Microservice Architecture (MSA)**.

Các thành phần chính:

* **Config Server:** Quản lý cấu hình tập trung.
* **Eureka Server:** Đăng ký và khám phá các service.
* **API Gateway:** Điểm truy cập duy nhất từ bên ngoài và định tuyến request.
* **Identity Service:** Xác thực người dùng và phát hành JWT.
* **Customer Service:** Quản lý thông tin khách hàng.
* **Account Service:** Quản lý tài khoản và số dư.
* **Transaction Service:** Quản lý lịch sử giao dịch.
* **Notification Service:** Nhận sự kiện và gửi thông báo.
* **Kafka:** Giao tiếp bất đồng bộ giữa các service.
* **Redis:** Caching dữ liệu thường xuyên được truy vấn.

Mỗi service sử dụng **Database riêng (Database-per-service)** và không truy cập trực tiếp database của service khác.

## 3. Chức năng chính

### 3.1. Quản lý dữ liệu

ADMIN có quyền quản lý:

* Khách hàng.
* Nhân viên/Giao dịch viên.
* Loại tài khoản.
* Tài khoản.

Các chức năng chính gồm:

* Thêm.
* Sửa.
* Xóa.
* Xem danh sách.

### 3.2. Chuyển khoản

CUSTOMER có thể thực hiện chuyển tiền đến tài khoản đích.

Luồng cơ bản:

```text
CUSTOMER
   ↓
API Gateway
   ↓
Account Service
   ↓
Saga
   ↓
Trừ tiền tài khoản nguồn
   ↓
Cộng tiền tài khoản đích
   ↓
Transaction Service ghi nhận giao dịch
   ↓
Kafka Event
   ↓
Notification Service
```

Saga được sử dụng để đảm bảo **nhất quán cuối cùng** giữa các service.

Nếu một bước thất bại, hệ thống thực hiện **Compensating Transaction** để hoàn tác các bước đã thực hiện trước đó.

## 4. Phân quyền và bảo mật

Hệ thống sử dụng **JWT** để xác thực và phân quyền.

Ba vai trò chính:

| Vai trò  | Quyền chính                                                     |
| -------- | --------------------------------------------------------------- |
| ADMIN    | Quản lý hệ thống và dữ liệu                                     |
| TELLER   | Xem và xử lý giao dịch trong phạm vi được phân công             |
| CUSTOMER | Xem tài khoản, lịch sử giao dịch và chuyển khoản của chính mình |

JWT giúp người dùng duy trì trạng thái đăng nhập mà không cần nhập lại mật khẩu liên tục.

Các request không có quyền truy cập sẽ trả về HTTP Status phù hợp như **401 Unauthorized** hoặc **403 Forbidden**.

## 5. Giao tiếp giữa các service

Hệ thống sử dụng hai hình thức giao tiếp:

### Synchronous

Sử dụng **OpenFeign/RestTemplate** kết hợp Eureka và Spring Cloud LoadBalancer để gọi service theo tên.

```text
Service A
   ↓
Eureka
   ↓
Service B
```

### Asynchronous

Sử dụng **Apache Kafka** để phát và nhận các sự kiện nghiệp vụ.

Ví dụ:

```text
Account Service
      ↓
  Kafka Topic
      ↓
Notification Service
```

Cách này giúp giảm sự phụ thuộc trực tiếp giữa các service.

## 6. Fault Tolerance

Hệ thống sử dụng **Resilience4j Circuit Breaker** để hạn chế Cascading Failure.

Circuit Breaker gồm 3 trạng thái:

```text
CLOSED → OPEN → HALF_OPEN → CLOSED
```

Khi service phụ thuộc liên tục gặp lỗi, Circuit Breaker chuyển sang **OPEN** và ngăn các request tiếp tục gọi đến service lỗi.

Khi hệ thống được khôi phục, Circuit Breaker chuyển sang **HALF_OPEN** để kiểm tra trước khi quay lại **CLOSED**.

## 7. Distributed Cache

Sử dụng **Spring Cache + Redis** theo chiến lược **Cache-Aside**.

Các annotation chính:

* `@Cacheable`: Lấy dữ liệu từ cache nếu đã tồn tại.
* `@CachePut`: Cập nhật dữ liệu vào cache.
* `@CacheEvict`: Xóa dữ liệu khỏi cache.

Redis giúp giảm số lượng truy vấn trực tiếp xuống database và cải thiện tốc độ phản hồi.

## 8. Xử lý lỗi và kiểm thử

Hệ thống sử dụng cơ chế **Exception Handling tập trung** kết hợp AOP để xử lý lỗi thống nhất.

Response lỗi được chuẩn hóa theo JSON.

Ngoài ra:

* Viết Unit Test cho Service/Controller.
* Sử dụng JaCoCo để đo độ bao phủ kiểm thử.
* Ghi log các lỗi phục vụ việc theo dõi và bảo trì hệ thống.

## 9. Kịch bản kiểm thử chính

### Kịch bản 1: Chuyển khoản thành công

```text
Tài khoản A: 10.000.000
Tài khoản B: 5.000.000

A chuyển B: 2.000.000

Kết quả:
A: 8.000.000
B: 7.000.000
Transaction được tạo thành công.
```

### Kịch bản 2: Saga Rollback

```text
A trừ tiền
   ↓
B cộng tiền thất bại
   ↓
Compensating Event
   ↓
Hoàn tiền cho A
```

Kết quả cuối cùng đảm bảo dữ liệu không bị sai lệch.

### Kịch bản 3: Kafka

Sau khi giao dịch thành công:

```text
Transaction Event
      ↓
Kafka
      ↓
Notification Service
```

Notification Service nhận sự kiện và xử lý thông báo biến động.

### Kịch bản 4: Circuit Breaker

Khi service phụ thuộc bị dừng:

```text
Request
   ↓
Service lỗi
   ↓
Circuit Breaker
   ↓
OPEN
   ↓
Fallback
```

Hệ thống không tiếp tục tạo thêm request đến service đang gặp sự cố.

## 10. Kết luận

RikkeiBank API được thiết kế theo kiến trúc **Microservice**, sử dụng Spring Cloud, Eureka, API Gateway, Kafka, Redis, JWT, Resilience4j và Saga.

Kiến trúc giúp các nghiệp vụ được tách thành các service độc lập, hỗ trợ **mở rộng riêng từng service, giao tiếp đồng bộ/bất đồng bộ, caching, xử lý lỗi và đảm bảo nhất quán dữ liệu trong giao dịch chuyển khoản**.
