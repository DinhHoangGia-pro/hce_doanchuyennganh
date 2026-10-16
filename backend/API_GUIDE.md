# API quản lý sản phẩm

API thao tác trực tiếp với bảng `dbo.Products` trong database `Northwind`. Ứng dụng không tạo database hoặc bảng. SQL Server xác nhận `ProductID` là identity; `ProductName` tối đa 40 ký tự; `QuantityPerUnit` tối đa 20 ký tự; `UnitPrice` là `money`; các trường số lượng là `smallint`; `Discontinued` là `bit NOT NULL`. `SupplierID` và `CategoryID` tham chiếu lần lượt `dbo.Suppliers` và `dbo.Categories`.

Các trường JSON dùng đúng tên cột Northwind. JSON lỗi có dạng:

```json
{
  "timestamp": "2026-10-16T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "ProductName must not be blank",
  "path": "/api/danhsachsanpham"
}
```

## Endpoint

| Method | URL | Kết quả |
| --- | --- | --- |
| `GET` | `/api/danhsachsanpham` | `200 OK`, JSON array; danh sách rỗng trả `[]` |
| `GET` | `/api/danhsachsanpham/{id}` | `200 OK` hoặc `404 Not Found` |
| `POST` | `/api/danhsachsanpham` | `201 Created`, sản phẩm kèm `ProductID` và header `Location` |
| `PUT` | `/api/danhsachsanpham/{id}` | `200 OK` và sản phẩm sau cập nhật, hoặc `404 Not Found` |
| `DELETE` | `/api/danhsachsanpham/{id}` | `204 No Content`, `404 Not Found`, hoặc `409 Conflict` nếu đang được tham chiếu |

`ProductName` bắt buộc và không được trống. `UnitPrice`, `UnitsInStock`, `UnitsOnOrder`, `ReorderLevel` không được âm; các trường chuỗi bị giới hạn theo schema. `SupplierID` và `CategoryID` được kiểm tra tồn tại trước khi ghi. `ProductID` gửi trong request bị bỏ qua; database tự sinh khóa này. Nếu bỏ qua giá/số lượng/trạng thái, API áp dụng giá trị mặc định của bảng (`0`/`false`). Các trường tham chiếu và quy cách có thể để `null`.

## Request mẫu

ID `1` hiện tồn tại trong cả `Suppliers` và `Categories` của database được kiểm tra:

```json
{
  "ProductName": "Vietnam Coffee",
  "SupplierID": 1,
  "CategoryID": 1,
  "QuantityPerUnit": "20 bags x 500 g",
  "UnitPrice": 25.50,
  "UnitsInStock": 100,
  "UnitsOnOrder": 0,
  "ReorderLevel": 10,
  "Discontinued": false
}
```

Response khi tạo thành công là `201 Created`, với JSON sản phẩm có thêm `ProductID`. Khi cập nhật, gửi toàn bộ trường muốn lưu như mẫu trên; `ProductID` trong body không được dùng để đổi khóa.

## Cấu hình và chạy

Các giá trị mặc định không bí mật là `DB_HOST=localhost`, `DB_PORT=1433`, `DB_NAME=Northwind`, `DB_USERNAME=sa`. Mật khẩu không có trong source; nạp `DB_PASSWORD` từ Codespaces Secret hoặc nhập ở terminal:

```bash
read -s -p "SQL Server password: " DB_PASSWORD; echo
export DB_PASSWORD
export DB_HOST=localhost DB_PORT=1433 DB_NAME=Northwind DB_USERNAME=sa
./mvnw spring-boot:run
```

JDBC URL dùng `encrypt=true;trustServerCertificate=true`. `localhost` chỉ phù hợp khi cổng SQL Server được publish trong cùng Codespace. Nếu ứng dụng chạy trong container khác, đặt `DB_HOST` thành hostname/service SQL Server có thể truy cập từ container đó.

## Kiểm thử với REST Client

Lưu các request sau vào file `.http` hoặc gửi tương đương từ Postman. Chạy GET trước; dùng `ProductID` trả về từ POST cho các request tiếp theo.

```http
@baseUrl = http://localhost:8080
@productId = 9999

### Danh sách
GET {{baseUrl}}/api/danhsachsanpham
Accept: application/json

### Chi tiết
GET {{baseUrl}}/api/danhsachsanpham/{{productId}}
Accept: application/json

### Tạo
POST {{baseUrl}}/api/danhsachsanpham
Content-Type: application/json

{
  "ProductName": "Vietnam Coffee",
  "SupplierID": 1,
  "CategoryID": 1,
  "QuantityPerUnit": "20 bags x 500 g",
  "UnitPrice": 25.50,
  "UnitsInStock": 100,
  "UnitsOnOrder": 0,
  "ReorderLevel": 10,
  "Discontinued": false
}

### Cập nhật
PUT {{baseUrl}}/api/danhsachsanpham/{{productId}}
Content-Type: application/json

{
  "ProductName": "Vietnam Coffee Updated",
  "SupplierID": 1,
  "CategoryID": 1,
  "QuantityPerUnit": "20 bags x 500 g",
  "UnitPrice": 26.00,
  "UnitsInStock": 90,
  "UnitsOnOrder": 0,
  "ReorderLevel": 10,
  "Discontinued": false
}

### Dữ liệu không hợp lệ: mong đợi 400
POST {{baseUrl}}/api/danhsachsanpham
Content-Type: application/json

{
  "ProductName": " ",
  "UnitPrice": -1,
  "UnitsInStock": -2
}

### ID không tồn tại: mong đợi 404
GET {{baseUrl}}/api/danhsachsanpham/2147483647

### Xóa
DELETE {{baseUrl}}/api/danhsachsanpham/{{productId}}
```

`DELETE` trả `204` nếu xóa được; nếu sản phẩm đã được bảng khác tham chiếu, API trả `409` với thông báo dễ hiểu. Khi kiểm thử thật, thay `@productId` bằng ID mới từ response POST để tránh xóa nhầm dữ liệu sẵn có.