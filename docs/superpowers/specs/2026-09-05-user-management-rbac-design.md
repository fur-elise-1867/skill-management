# Đặc Tả Kỹ Thuật: Nâng Cấp Quản Trị Người Dùng & Phân Quyền (RBAC)

- **Ngày tạo:** 2026-09-05
- **Trạng thái:** Chờ phê duyệt (Pending Approval)
- **Dự án:** Skill Management System
- **Kiến trúc:** Spring Boot 3 + Spring Security + JPA (Backend) / Vue 3 + Pinia + Element Plus (Frontend)

---

## 1. Bối Cảnh & Mục Tiêu

Hệ thống quản lý kỹ năng (Skill Management System) cần nâng cấp cơ chế phân quyền người dùng từ mô hình Enum cố định sang mô hình Role trong Database (RBAC linh hoạt), đồng thời mở rộng chức năng quản trị tài khoản và cung cấp trang tự quản lý thông tin cá nhân cho người dùng.

### Các mục tiêu chính:
1. **Hỗ trợ 3 vai trò ban đầu:** `ADMIN`, `EDITOR`, `USER` với khả năng mở rộng thêm các vai trò mới trực tiếp trong cơ sở dữ liệu.
2. **Quản trị viên (Admin):**
   - Thêm tài khoản mới trực tiếp với vai trò chỉ định.
   - Thay đổi vai trò của người dùng (chặn tuyệt đối việc hạ cấp tài khoản đang có vai trò `ADMIN`).
   - Khóa / Kích hoạt tài khoản (Active/Inactive) — tài khoản Inactive không thể đăng nhập; chặn Admin tự khóa chính mình.
   - Reset mật khẩu người dùng với mật khẩu ngẫu nhiên an toàn, hiển thị để sao chép.
   - Đổi mật khẩu tài khoản của chính mình.
3. **Trang Thông tin tài khoản (Account Information):**
   - Cho phép người dùng (`USER`, `EDITOR`, `ADMIN`) xem và chỉnh sửa thông tin cá nhân.
   - Cho phép tải lên và cập nhật ảnh đại diện (Avatar) lưu trên server.
   - Cho phép tự đổi mật khẩu (yêu cầu xác thực mật khẩu cũ).
   - Cho phép `USER` và `EDITOR` tự xóa tài khoản của chính mình (chặn tài khoản `ADMIN` tự xóa).
4. **Luồng Đăng ký:**
   - Sau khi đăng ký thành công, không tự động đăng nhập mà chuyển hướng về trang `/login` kèm thông báo.

---

## 2. Thiết Kế Cơ Sở Dữ Liệu & Thực Thể (Database & Entity Model)

### 2.1. Bảng `role` (Bảng mới)
Lưu danh sách các vai trò trong hệ thống:

```sql
CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Dữ liệu khởi tạo (Data Seeding):**
Hệ thống tự động chèn 3 bản ghi nếu bảng `role` rỗng khi ứng dụng khởi động:
- `(name: 'ADMIN', description: 'Quản trị viên hệ thống - toàn quyền cấu hình và quản trị')`
- `(name: 'EDITOR', description: 'Biên tập viên - quản lý và chỉnh sửa nội dung kỹ năng')`
- `(name: 'USER', description: 'Người dùng tiêu chuẩn - tra cứu và theo dõi lộ trình kỹ năng')`

### 2.2. Bảng `_user` (Cập nhật)
Cập nhật bảng `_user` để chuyển từ enum sang liên kết khóa ngoại với bảng `role`, bổ sung trường trạng thái và ảnh đại diện:

```sql
ALTER TABLE _user ADD COLUMN role_id BIGINT;
ALTER TABLE _user ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE _user ADD COLUMN avatar_url VARCHAR(500);

ALTER TABLE _user ADD CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role(id);
```

*Lưu ý di chuyển dữ liệu cũ:* Nếu có dữ liệu người dùng cũ, map role enum cũ sang `role_id` tương ứng trong bảng `role`.

### 2.3. Entity Java
- **`Role.java`**:
  ```java
  @Entity
  @Table(name = "role")
  public class Role {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @Column(nullable = false, unique = true, length = 50)
      private String name;

      private String description;
  }
  ```
- **`User.java`**:
  ```java
  @Entity
  @Table(name = "_user")
  public class User implements UserDetails {
      // id, name, gender, email, mobile, password ...
      @ManyToOne(fetch = FetchType.EAGER)
      @JoinColumn(name = "role_id", nullable = false)
      private Role role;

      @Column(nullable = false)
      private boolean enabled = true;

      @Column(name = "avatar_url")
      private String avatarUrl;

      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() {
          String authority = role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName();
          return List.of(new SimpleGrantedAuthority(authority));
      }

      @Override
      public boolean isEnabled() {
          return this.enabled;
      }
  }
  ```

---

## 3. Thiết Kế Lưu Trữ & Phục Vụ Tệp Tĩnh (Avatar Storage)

1. **Thư mục lưu trữ:** `uploads/avatars/` nằm tại thư mục gốc của backend (hỗ trợ cấu hình qua file `application.yml`: `app.upload.dir=uploads/avatars/`).
2. **Quy tắc đặt tên file:** `avatar_{userId}_{timestamp}.{extension}` để tránh trùng lặp và tránh xung đột cache trình duyệt.
3. **Giới hạn tệp:**
   - Định dạng hợp lệ: `.jpg`, `.jpeg`, `.png`, `.webp` (kiểm tra MIME type `image/jpeg`, `image/png`, `image/webp`).
   - Dung lượng tối đa: 5MB.
4. **Dọn dẹp file cũ:** Khi người dùng upload avatar mới hoặc khi tài khoản bị xóa, file avatar cũ tương ứng trên đĩa sẽ được dọn dẹp để tiết kiệm dung lượng.
5. **Cấu hình Static Resource WebMvc:**
   ```java
   @Configuration
   public class WebMvcConfig implements WebMvcConfigurer {
       @Value("${app.upload.dir:uploads/avatars/}")
       private String uploadDir;

       @Override
       public void addResourceHandlers(ResourceHandlerRegistry registry) {
           Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
           registry.addResourceHandler("/uploads/avatars/**")
                   .addResourceLocations("file:" + uploadPath.toString() + "/");
       }
   }
   ```
6. **Bảo mật Spring Security:** Cho phép truy cập công khai endpoint phục vụ ảnh:
   ```java
   .requestMatchers("/uploads/**").permitAll()
   ```

---

## 4. Đặc Tả Chi Tiết API (RESTful Endpoints)

### 4.1. Nhóm Xác Thực (`/api/v1/auth/**`)
- `POST /api/v1/auth/register`: Đăng ký tài khoản mới (gán mặc định vai trò `USER`, `enabled = true`).
- `POST /api/v1/auth/authenticate`: Đăng nhập. Nếu `user.isEnabled() == false`, ném `DisabledException`. `GlobalExceptionHandler` bắt và trả về HTTP 403:
  ```json
  {
    "status": 403,
    "message": "Tài khoản của bạn đã bị khóa hoặc chưa kích hoạt. Vui lòng liên hệ Quản trị viên."
  }
  ```

### 4.2. Nhóm Quản Trị (`/api/v1/admin/**`) — Yêu cầu `ROLE_ADMIN`
1. `GET /api/v1/admin/roles`: Lấy danh sách toàn bộ roles trong hệ thống để phục vụ dropdown chọn vai trò.
   - **Response 200:** `[ { "id": 1, "name": "ADMIN", "description": "..." }, ... ]`
2. `GET /api/v1/admin/get-users`: Lấy danh sách người dùng kèm vai trò, avatarUrl, enabled.
3. `POST /api/v1/admin/create-user`: Admin tạo tài khoản mới.
   - **Body:** `{ "name": "...", "email": "...", "password": "...", "roleId": 2, "gender": "...", "mobile": "...", "enabled": true }`
   - **Validation:** Email chưa tồn tại, password >= 8 ký tự, roleId hợp lệ.
4. `PUT /api/v1/admin/users/{id}/role`: Đổi vai trò tài khoản.
   - **Body:** `{ "roleId": 2 }`
   - **Quy tắc bảo vệ Admin:** Nếu người dùng có `id` mục tiêu hiện đang giữ vai trò `ADMIN`, hệ thống từ chối và trả về lỗi:
     - **Status 400 Bad Request:** `{ "message": "Không thể thay đổi hoặc hạ cấp vai trò của tài khoản Quản trị viên (Admin)." }`
5. `PUT /api/v1/admin/users/{id}/status`: Kích hoạt / Vô hiệu hóa tài khoản.
   - **Body:** `{ "enabled": false }`
   - **Quy tắc bảo vệ:** Admin không được phép tự vô hiệu hóa tài khoản của chính mình.
     - **Status 400 Bad Request:** `{ "message": "Bạn không thể tự khóa tài khoản của chính mình." }`
6. `POST /api/v1/admin/users/{id}/reset-password`: Reset mật khẩu người dùng.
   - **Xử lý:** Tạo chuỗi mật khẩu ngẫu nhiên (10 ký tự gồm chữ hoa, thường, số, ký tự đặc biệt). Hash BCrypt lưu DB.
   - **Response 200:** `{ "newPassword": "k8!Pq#92Lx" }`
7. `DELETE /api/v1/admin/delete-user/{id}`: Xóa tài khoản người dùng (chặn Admin tự xóa chính mình).

### 4.3. Nhóm Người Dùng (`/api/v1/user/**`) — Mọi tài khoản đã đăng nhập
1. `GET /api/v1/user/current-user`: Lấy thông tin tài khoản hiện tại.
2. `PUT /api/v1/user/profile`: Cập nhật thông tin cá nhân (`name`, `gender`, `mobile`).
3. `PUT /api/v1/user/change-password`: Đổi mật khẩu của chính mình.
   - **Body:** `{ "oldPassword": "...", "newPassword": "..." }`
   - **Xử lý:** Kiểm tra `passwordEncoder.matches(oldPassword, user.getPassword())`. Nếu không khớp, trả về HTTP 400: *"Mật khẩu hiện tại không chính xác"*.
4. `POST /api/v1/user/avatar`: Tải lên ảnh đại diện (`multipart/form-data`, file param: `avatar`).
   - **Xử lý:** Kiểm tra định dạng & dung lượng, lưu vào `uploads/avatars/`, xóa file avatar cũ, lưu `avatar_url` vào DB.
   - **Response 200:** `{ "avatarUrl": "/uploads/avatars/avatar_1_1725500000.png" }`
5. `DELETE /api/v1/user/account`: Tự xóa tài khoản của chính mình.
   - **Quy tắc bảo vệ:** Nếu tài khoản hiện tại có vai trò là `ADMIN`, hệ thống từ chối:
     - **Status 400 Bad Request:** `{ "message": "Tài khoản Quản trị viên (Admin) không được phép tự xóa tài khoản." }`
   - Đối với `USER` và `EDITOR`: Xóa tài khoản trong DB, xóa avatar trên đĩa và trả về HTTP 200.

---

## 5. Thiết Kế Giao Diện Frontend (Vue 3 + Element Plus)

### 5.1. Trang Thông Tin Tài Khoản Mới (`src/views/AccountInfoView.vue`)
- **Route:** `/account` (yêu cầu `requiresAuth: true`).
- **Bố cục:**
  - **Header:** Tiêu đề "Thông tin tài khoản", nút "← Dashboard".
  - **Thẻ 1: Thông tin cá nhân & Ảnh đại diện:**
    - Khu vực avatar tròn lớn (`el-avatar`), nút tải ảnh (`el-upload`), preview trước khi tải.
    - Form chỉnh sửa: Họ và tên, Giới tính (Select Nam / Nữ / Khác), Số điện thoại.
    - Tag hiển thị vai trò hiện tại (Admin: `danger`, Editor: `warning`, User: `primary`).
    - Nút "Lưu thay đổi".
  - **Thẻ 2: Đổi mật khẩu:**
    - Trường Mật khẩu hiện tại, Mật khẩu mới, Xác nhận mật khẩu mới.
    - Nút "Cập nhật mật khẩu".
  - **Thẻ 3: Vùng nguy hiểm (Danger Zone):**
    - Nếu là `ADMIN`: Hiển thị thông báo bảo vệ tài khoản quản trị.
    - Nếu là `EDITOR` hoặc `USER`: Nút "Xoá tài khoản của tôi" (`type="danger"`). Nhấp vào sẽ mở `ElMessageBox.confirm` yêu cầu người dùng xác nhận. Khi xác nhận, gọi API xóa, đăng xuất và điều hướng về `/login`.

### 5.2. Nâng Cấp Trang Quản Trị (`src/views/AdminView.vue`)
- **Thêm nút "+ Thêm tài khoản"**: Mở `el-dialog` với form thêm tài khoản đầy đủ (Họ tên, Email, Mật khẩu, Chọn Role từ API, SĐT, Giới tính, Switch Kích hoạt).
- **Cập nhật `el-table`**:
  - *Cột Avatar*: `el-avatar` nhỏ hiển thị ảnh đại diện hoặc chữ cái đầu.
  - *Cột Vai trò*: `el-select` chọn vai trò trực tiếp trên từng hàng.
    - Nếu `row.role.name === 'ADMIN'`: thuộc tính `disabled = true` kèm tooltip: *"Không thể thay đổi vai trò tài khoản Quản trị viên"*.
    - Khi thay đổi role khác: bật hộp thoại xác nhận trước khi gọi API cập nhật.
  - *Cột Trạng thái*: `el-switch` (Active/Inactive).
    - Disable switch nếu `row.email === auth.user?.email` (ngăn Admin tự khóa mình).
  - *Cột Hành động*:
    - Nút "Reset MK": Gọi API reset mật khẩu -> Mở `el-dialog` hiển thị mật khẩu mới và nút "Sao chép mật khẩu" vào clipboard.
    - Nút "Xóa": Chặn nếu là tài khoản của chính mình.

### 5.3. Cập Nhật Luồng Đăng Ký (`src/views/RegisterView.vue` & `src/stores/auth.ts`)
- Sửa hàm `register` trong `auth.ts`:
  - Gọi API đăng ký.
  - Không lưu token vào localStorage và không set `token.value`.
  - Hiển thị thông báo thành công: *"Đăng ký tài khoản thành công! Vui lòng đăng nhập bằng tài khoản vừa tạo."*
  - Điều hướng sang `/login`.

### 5.4. Cập Nhật Thanh Điều Hướng Header (`DashboardView.vue` & `AdminView.vue`)
- Hiển thị Avatar người dùng thu nhỏ cạnh tên người dùng.
- Thêm nút "Thông tin tài khoản" (`/account`).
- Nút "Quản trị" chỉ hiển thị khi `auth.user?.role?.name === 'ADMIN'`.

---

## 6. Kế Hoạch Kiểm Thử & Xác Minh (Verification Plan)

### 6.1. Kiểm thử Tự Động (Automated Tests)
1. **Backend Tests (`mvn test`):**
   - Unit test `RoleRepository` và `UserRepository`.
   - Unit test `AdminService`:
     - Test tạo tài khoản thành công với các role khác nhau.
     - Test chặn hạ cấp tài khoản có role `ADMIN` (phải ném ngoại lệ hoặc trả về lỗi).
     - Test reset mật khẩu sinh đúng chuỗi mới và mã hóa lưu DB.
     - Test chặn Admin tự khóa tài khoản của chính mình.
   - Unit test `UserService`:
     - Test đổi thông tin cá nhân.
     - Test đổi mật khẩu (mật khẩu cũ đúng vs mật khẩu cũ sai).
     - Test chặn tài khoản Admin tự xóa tài khoản của mình.
     - Test tài khoản User/Editor tự xóa thành công.
   - Test `AuthenticationService`:
     - Test tài khoản `enabled = false` bị từ chối đăng nhập.

2. **Frontend Build & Linter Check:**
   - Chạy `npm run build` hoặc `npm run test` để đảm bảo type checking và build không lỗi.

### 6.2. Kiểm thử Thủ Công (Manual Verification)
1. **Đăng ký tài khoản mới:** Đăng ký -> xác nhận chuyển sang `/login` mà không tự động vào Dashboard -> Đăng nhập bằng tài khoản vừa đăng ký.
2. **Trang Thông tin tài khoản:**
   - Cập nhật Họ tên, SĐT -> Lưu -> Reload trang thấy dữ liệu mới.
   - Upload avatar -> Xác nhận ảnh hiển thị tức thì và hiển thị đúng trên header.
   - Đổi mật khẩu với mật khẩu cũ sai -> Báo lỗi. Đổi với mật khẩu cũ đúng -> Thành công.
   - Kiểm tra vùng nguy hiểm: Tài khoản Admin bị vô hiệu hóa nút xóa; tài khoản User xóa thành công và logout.
3. **Trang Quản trị:**
   - Bấm "+ Thêm tài khoản": Tạo thành công tài khoản mới với các vai trò `EDITOR`, `USER`.
   - Kiểm tra bảo vệ Admin: Dropdown role của tài khoản Admin bị disable.
   - Đổi role của Editor thành User và ngược lại -> Thành công.
   - Bật/Tắt switch Trạng thái: Tắt một tài khoản User -> Đăng nhập bằng tài khoản đó -> Bị từ chối với thông báo tài khoản bị khóa.
   - Reset mật khẩu: Bấm "Reset MK" -> Dialog hiện mật khẩu mới -> Bấm copy -> Đăng nhập thành công bằng mật khẩu mới đó.
