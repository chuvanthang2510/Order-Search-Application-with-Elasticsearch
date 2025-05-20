# API Đặc Tả - Tìm Kiếm Đơn Hàng

## Endpoint: `/api/orders/search`

### Mô tả
API này cho phép tìm kiếm đơn hàng với nhiều tiêu chí khác nhau như từ khóa tìm kiếm, khoảng thời gian, và phân trang.

### Phương thức
`GET`

### Tham số Query

| Tham số | Kiểu dữ liệu | Bắt buộc | Mô tả |
|---------|--------------|----------|--------|
| searchTerm | String | Không | Từ khóa tìm kiếm. Có thể là email, mã đơn hàng, mã booking, số điện thoại hoặc tên khách hàng |
| fromDate | Date | Không | Ngày bắt đầu tìm kiếm (định dạng: dd/MM/yyyy HH:mm:ss) |
| toDate | Date | Không | Ngày kết thúc tìm kiếm (định dạng: dd/MM/yyyy HH:mm:ss) |
| page | Integer | Không | Số trang (mặc định: 0) |
| size | Integer | Không | Số bản ghi trên mỗi trang (mặc định: 10) |

### Logic Tìm Kiếm

1. **Tìm kiếm theo email**:
   - Nếu searchTerm chứa ký tự "@", hệ thống sẽ tìm kiếm chính xác theo email
   - Email được chuyển về chữ thường trước khi so sánh

2. **Tìm kiếm theo các trường khác**:
   - **Tên khách hàng**: Tìm kiếm chính xác cụm từ
   - **Mã đơn hàng (code)**: Tìm kiếm chính xác hoặc một phần
   - **Mã booking (bookingCode)**: Tìm kiếm chính xác hoặc một phần
   - **Số điện thoại (phoneNumber)**: Tìm kiếm chính xác hoặc một phần

3. **Lọc theo thời gian**:
   - Nếu có fromDate: lọc các đơn hàng từ ngày này trở đi
   - Nếu có toDate: lọc các đơn hàng đến ngày này
   - Có thể kết hợp cả hai để tìm trong khoảng thời gian

### Kết quả trả về

```json
{
    "content": [
        {
            "id": "string",
            "code": "string",
            "bookingCode": "string",
            "phoneNumber": "string",
            "customerName": "string",
            "customerEmail": "string",
            "orderDate": "string",
            "totalAmount": "number",
            "status": "string"
        }
    ],
    "totalElements": "number",
    "totalPages": "number",
    "size": "number",
    "number": "number"
}
```

### Sắp xếp
- Kết quả được sắp xếp theo:
  1. Điểm phù hợp (relevance score) giảm dần
  2. Ngày đơn hàng (orderDate) giảm dần

### Ví dụ

1. Tìm kiếm đơn hàng theo email:
```
GET /api/orders/search?searchTerm=nguyenvana@gmail.com
```

2. Tìm kiếm đơn hàng trong khoảng thời gian:
```
GET /api/orders/search?fromDate=01/01/2024 00:00:00&toDate=31/01/2024 23:59:59
```

3. Tìm kiếm với phân trang:
```
GET /api/orders/search?page=0&size=20
```

### Lưu ý
- API hỗ trợ tìm kiếm tiếng Việt có dấu và không dấu
- Kết quả tìm kiếm được phân trang để tối ưu hiệu suất
- Thời gian tìm kiếm được định dạng theo chuẩn "dd/MM/yyyy HH:mm:ss" 