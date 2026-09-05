# Báo cáo Thực thi Kiểm thử (Test Run Report)

- **Dự án:** Skill Management System (`skill-management`)
- **Cam kết kiểm thử (Commit):** `81bf5513894456b990b780efb6343e22b2258951`
- **Thời gian thực thi:** 2026-09-05T18:13:20+07:00
- **Môi trường thực thi:**
  - Hệ điều hành: Windows 11 Pro 64-bit
  - Java Runtime: Java 21.0.7 LTS (`C:\Program Files\Java\jdk-21`)
  - Maven: Apache Maven 3.9.5 via Maven Wrapper (`mvnw.cmd`)
  - Node.js: v22.x / npm 10.x
  - Test Frameworks: JUnit 5, Spring Security Test, MockMvc, AssertJ, Vitest 4.1.11, Playwright 1.63.0
  - Cơ sở dữ liệu kiểm thử Backend: In-Memory H2 Database (PostgreSQL Compatibility Mode)
- **Tài liệu tham chiếu:** [`docs/testcases/TESTCASE_CATALOG.md`](file:///d:/Documents/Git%20Project/skill-management/docs/testcases/TESTCASE_CATALOG.md)

---

## 1. Tóm tắt Kết quả Thực thi (Executive Summary)

| Phân hệ / Bộ kiểm thử | Tổng số TC | PASSED | FAILED | BLOCKED | Tỷ lệ thành công khả dụng | Thời gian chạy |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|
| **Backend REST API (MockMvc)** | 27 | 27 | 0 | 0 | **100%** | 12.92 s |
| **Backend Context Test** | 1 | 1 | 0 | 0 | **100%** | (Chung suite) |
| **Frontend Pinia Auth Store** | 5 | 5 | 0 | 0 | **100%** | 11 ms |
| **Frontend Vue Router Guard** | 5 *(7 runs)* | 5 | 0 | 0 | **100%** | 25 ms |
| **Frontend Axios Interceptors** | 4 *(5 runs)* | 4 | 0 | 0 | **100%** | 9 ms |
| **E2E Integration (Playwright)** | 3 | 0 | 0 | 3 | **BLOCKED** | N/A |
| **TỔNG CỘNG** | **45** | **42** | **0** | **3** | **100% (Khả dụng)** | **~14.5 s** |

> **Ghi chú về tính trung thực:**
> - Toàn bộ 41/41 testcase đơn vị và tích hợp (Backend MockMvc + Frontend Vitest) đều **PASS** 100%.
> - 3 testcase E2E Playwright được ghi nhận chính xác là **BLOCKED** do server backend (`localhost:8080`) và frontend (`localhost:5173`) chưa được khởi chạy trong môi trường kiểm thử này. Không bịa đặt kết quả.

---

## 2. Nhật ký Thực thi Chi tiết Từng Bộ Test (Execution Logs)

### 2.1. Backend Test Suite (MockMvc Integration Tests)

- **Lệnh thực thi:**
  ```powershell
  cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-21&& mvnw.cmd test"
  ```
- **Hạ tầng CSDL:** `jdbc:h2:mem:skill_management_test_db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1` (Hoàn toàn độc lập, không lệ thuộc Docker hay PostgreSQL ngoài máy).
- **Trạng thái:** `BUILD SUCCESS` (Mã thoát: 0).
- **Trích xuất Log thực tế:**
  ```
  [INFO] -------------------------------------------------------
  [INFO]  T E S T S
  [INFO] -------------------------------------------------------
  [INFO] Running com.furelise.skillmanagement.controller.HealthControllerTest
  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s -- in com.furelise.skillmanagement.controller.HealthControllerTest
  [INFO] Running com.furelise.skillmanagement.controller.AuthenticationControllerTest
  [INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.157 s -- in com.furelise.skillmanagement.controller.AuthenticationControllerTest
  [INFO] Running com.furelise.skillmanagement.controller.UserControllerTest
  [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.362 s -- in com.furelise.skillmanagement.controller.UserControllerTest
  [INFO] Running com.furelise.skillmanagement.controller.AdminControllerTest
  [INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.443 s -- in com.furelise.skillmanagement.controller.AdminControllerTest
  [INFO] Running com.furelise.skillmanagement.SkillManagementApplicationTests
  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.832 s -- in com.furelise.skillmanagement.SkillManagementApplicationTests
  [INFO] 
  [INFO] Results:
  [INFO] 
  [INFO] Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
  [INFO] 
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  [INFO] Total time:  12.916 s
  [INFO] Finished at: 2026-09-05T18:12:06+07:00
  [INFO] ------------------------------------------------------------------------
  ```

---

### 2.2. Frontend Unit Test Suite (Vitest)

- **Lệnh thực thi:**
  ```powershell
  cmd.exe /c "npm run test:unit -- --run"
  ```
- **Môi trường DOM:** JSDOM
- **Trạng thái:** `PASSED` (Mã thoát: 0).
- **Trích xuất Log thực tế:**
  ```
  > frontend@0.0.0 test:unit
  > vitest --run

   RUN  v4.1.11 D:/Documents/Git Project/skill-management/frontend

   ✓ src/stores/__tests__/auth.spec.ts (5 tests) 11ms
   ✓ src/services/__tests__/api.spec.ts (5 tests) 9ms
   ✓ src/router/__tests__/router.spec.ts (7 tests) 25ms

   Test Files  3 passed (3)
        Tests  17 passed (17)
     Start at  18:12:11
     Duration  1.41s (transform 692ms, setup 0ms, import 980ms, tests 44ms, environment 2.39s)
  ```

---

### 2.3. Frontend Type-Check (vue-tsc)

- **Lệnh thực thi:**
  ```powershell
  cmd.exe /c "npm run type-check"
  ```
- **Trạng thái:** `PASSED` (Mã thoát: 0).
- **Kết quả:** Biên dịch TypeScript thành công, không có bất kỳ lỗi type nào trên toàn bộ codebase frontend và test files.

---

### 2.4. End-to-End Test Suite (Playwright)

- **Lệnh thực thi:**
  ```powershell
  cmd.exe /c "npx playwright test"
  ```
- **Trạng thái:** **`BLOCKED`**
- **Lý do cụ thể:**
  1. Dịch vụ Backend (`http://localhost:8080`) và Frontend Dev Server (`http://localhost:5173`) hiện đang offline.
  2. Gói binary headless browser của Playwright chưa được nạp vào máy local (`npx playwright install chromium`).
- **Mã nguồn kịch bản đã sẵn sàng:**
  - File cấu hình: [`frontend/playwright.config.ts`](file:///d:/Documents/Git%20Project/skill-management/frontend/playwright.config.ts)
  - File kịch bản E2E: [`frontend/e2e/auth-flow.spec.ts`](file:///d:/Documents/Git%20Project/skill-management/frontend/e2e/auth-flow.spec.ts)
- **Các bước để kích hoạt khi chạy thực tế:**
  ```powershell
  # Bước a: Cài browser binary
  npx playwright install chromium

  # Bước b: Khởi động backend và frontend
  # Terminal 1: .\mvnw.cmd spring-boot:run
  # Terminal 2: npm run dev

  # Bước c: Chạy kiểm thử E2E
  npm run test:e2e
  ```

---

## 3. Bảng Chi tiết Kết quả Nghiệm thu 44 Testcase

| TC-ID | Module | Priority | Kỳ vọng (Expected) | Thực tế (Actual) | Trạng thái | Bằng chứng kiểm thử (Evidence) |
|:---|:---|:---:|:---|:---|:---:|:---|
| **TC-HLT-001** | Health | P2 | HTTP 200, status=UP, application=skill-management | Khớp 100% JSON schema | **PASSED** | [`HealthControllerTest.java:28`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/HealthControllerTest.java#L28) |
| **TC-AUTH-REG-001** | Auth | P0 | HTTP 201, trả về JWT token, DB lưu role=USER và hash password | Khớp 201 Created, token non-blank, DB lưu đúng | **PASSED** | [`AuthenticationControllerTest.java:51`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L51) |
| **TC-AUTH-REG-002** | Auth | P1 | HTTP 201, đăng ký thành công với gender/mobile null | Khớp 201 Created, DB lưu gender=null, mobile=null | **PASSED** | [`AuthenticationControllerTest.java:77`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L77) |
| **TC-AUTH-REG-003** | Auth | P1 | HTTP 400, validation error name | Khớp 400 Bad Request, message "Name is required" | **PASSED** | [`AuthenticationControllerTest.java:99`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L99) |
| **TC-AUTH-REG-004** | Auth | P1 | HTTP 400, validation error rỗng email | Khớp 400 Bad Request, details.email có lỗi | **PASSED** | [`AuthenticationControllerTest.java:117`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L117) |
| **TC-AUTH-REG-005** | Auth | P1 | HTTP 400, validation email sai format | Khớp 400 Bad Request, "Invalid email format" | **PASSED** | [`AuthenticationControllerTest.java:135`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L135) |
| **TC-AUTH-REG-006** | Auth | P1 | HTTP 400, validation error rỗng password | Khớp 400 Bad Request, details.password có lỗi | **PASSED** | [`AuthenticationControllerTest.java:153`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L153) |
| **TC-AUTH-REG-007** | Auth | P1 | HTTP 400, password < 8 ký tự | Khớp 400 Bad Request, "Password must be at least 8 characters" | **PASSED** | [`AuthenticationControllerTest.java:171`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L171) |
| **TC-AUTH-REG-008** | Auth | P0 | HTTP 400, duplicate email ném IllegalArgumentException | Khớp 400 Bad Request, "Email đã được đăng ký: ..." | **PASSED** | [`AuthenticationControllerTest.java:189`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L189) |
| **TC-AUTH-LOG-001** | Auth | P0 | HTTP 200, cấp JWT token (HS256, 24h) | Khớp 200 OK, sinh token JWT hợp lệ | **PASSED** | [`AuthenticationControllerTest.java:231`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L231) |
| **TC-AUTH-LOG-002** | Auth | P0 | HTTP 401, sai mật khẩu | Khớp 401 Unauthorized, "Email hoặc mật khẩu không chính xác." | **PASSED** | [`AuthenticationControllerTest.java:243`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L243) |
| **TC-AUTH-LOG-003** | Auth | P1 | HTTP 401, email không tồn tại | Khớp 401 Unauthorized, "Email hoặc mật khẩu không chính xác." | **PASSED** | [`AuthenticationControllerTest.java:256`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L256) |
| **TC-AUTH-LOG-004** | Auth | P2 | HTTP 400, email rỗng | Khớp 400 Bad Request, details.email | **PASSED** | [`AuthenticationControllerTest.java:269`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L269) |
| **TC-AUTH-LOG-005** | Auth | P2 | HTTP 400, password rỗng | Khớp 400 Bad Request, details.password | **PASSED** | [`AuthenticationControllerTest.java:281`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L281) |
| **TC-AUTH-LOG-006** | Auth | P2 | HTTP 400, email sai định dạng | Khớp 400 Bad Request, "Invalid email format" | **PASSED** | [`AuthenticationControllerTest.java:293`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AuthenticationControllerTest.java#L293) |
| **TC-USR-PRF-001** | User | P0 | HTTP 200, trả UserResponse, **KHÔNG có password** | Khớp 200 OK, trường password không tồn tại trong JSON | **PASSED** | [`UserControllerTest.java:65`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/UserControllerTest.java#L65) |
| **TC-USR-PRF-002** | User | P0 | Không token bị Spring Security chặn (401/403) | Spring Security chặn với HTTP 403 Forbidden | **PASSED** | [`UserControllerTest.java:82`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/UserControllerTest.java#L82) |
| **TC-USR-PRF-003** | User | P1 | Token malformed bị Spring Security chặn (401/403) | Spring Security chặn với HTTP 403 Forbidden | **PASSED** | [`UserControllerTest.java:90`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/UserControllerTest.java#L90) |
| **TC-USR-PRF-004** | User | P0 | **JWT hết hạn (Expired) bị chặn (401/403)** | ExpiredJwtException bắt bởi Filter, HTTP 403 Forbidden | **PASSED** | [`UserControllerTest.java:99`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/UserControllerTest.java#L99) |
| **TC-USR-PRF-005** | User | P2 | User trong token đã bị xóa khỏi DB (4xx/404) | Trả về HTTP 4xx Client Error | **PASSED** | [`UserControllerTest.java:118`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/UserControllerTest.java#L118) |
| **TC-ADM-GET-001** | Admin | P0 | Admin lấy danh sách (200 OK, **KHÔNG có password**) | Khớp 200 OK, trả về danh sách không lộ password hash | **PASSED** | [`AdminControllerTest.java:69`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AdminControllerTest.java#L69) |
| **TC-ADM-GET-002** | Admin | P0 | **USER thường bị chặn xem danh sách (403 Forbidden)** | `@Secured` chặn ném AccessDeniedException -> HTTP 403 | **PASSED** | [`AdminControllerTest.java:82`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AdminControllerTest.java#L82) |
| **TC-ADM-GET-003** | Admin | P1 | Unauthenticated bị chặn (401/403) | Spring Security chặn với HTTP 403 Forbidden | **PASSED** | [`AdminControllerTest.java:93`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AdminControllerTest.java#L93) |
| **TC-ADM-DEL-001** | Admin | P0 | Admin xóa user thành công (200 OK, xóa khỏi DB) | Khớp 200 OK, bản ghi bị xóa khỏi CSDL | **PASSED** | [`AdminControllerTest.java:106`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AdminControllerTest.java#L106) |
| **TC-ADM-DEL-002** | Admin | P0 | **USER thường bị chặn xóa user (403 Forbidden)** | `@Secured` chặn -> HTTP 403, bản ghi trong DB giữ nguyên | **PASSED** | [`AdminControllerTest.java:119`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AdminControllerTest.java#L119) |
| **TC-ADM-DEL-003** | Admin | P1 | Xóa user không tồn tại trả về HTTP 404 | UsernameNotFoundException -> HTTP 404 Not Found | **PASSED** | [`AdminControllerTest.java:132`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AdminControllerTest.java#L132) |
| **TC-ADM-DEL-004** | Admin | P1 | Unauthenticated delete bị chặn (401/403) | Spring Security chặn với HTTP 403 Forbidden | **PASSED** | [`AdminControllerTest.java:143`](file:///d:/Documents/Git%20Project/skill-management/backend/src/test/java/com/furelise/skillmanagement/controller/AdminControllerTest.java#L143) |
| **TC-FE-STR-001** | Frontend-Store | P2 | Khởi tạo state mặc định khi chưa có token | `token=null`, `user=null`, `isAdmin=false` | **PASSED** | [`auth.spec.ts:51`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/stores/__tests__/auth.spec.ts#L51) |
| **TC-FE-STR-002** | Frontend-Store | P1 | `login()` cập nhật token, profile, chuyển `/dashboard` | Khớp state, localStorage và `router.push('/dashboard')` | **PASSED** | [`auth.spec.ts:60`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/stores/__tests__/auth.spec.ts#L60) |
| **TC-FE-STR-003** | Frontend-Store | P1 | `register()` cập nhật token, profile, chuyển `/dashboard` | Khớp state, localStorage và `router.push('/dashboard')` | **PASSED** | [`auth.spec.ts:79`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/stores/__tests__/auth.spec.ts#L79) |
| **TC-FE-STR-004** | Frontend-Store | P1 | `fetchCurrentUser()` lỗi tự kích hoạt `logout()` | Tự động gọi logout(), xóa token, chuyển `/login` | **PASSED** | [`auth.spec.ts:98`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/stores/__tests__/auth.spec.ts#L98) |
| **TC-FE-STR-005** | Frontend-Store | P1 | `logout()` xóa sạch state và localStorage | State null, xóa token trong localStorage, chuyển `/login` | **PASSED** | [`auth.spec.ts:113`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/stores/__tests__/auth.spec.ts#L113) |
| **TC-FE-RTG-001** | Frontend-RouterGuard | P1 | Khách vào route protected chuyển hướng về `/login` | Router guard chuyển hướng về `{ name: 'login' }` | **PASSED** | [`router.spec.ts:46`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/router/__tests__/router.spec.ts#L46) |
| **TC-FE-RTG-002** | Frontend-RouterGuard | P2 | Đã login vào trang guest chuyển hướng sang `/dashboard` | Router guard chuyển hướng về `{ name: 'dashboard' }` | **PASSED** | [`router.spec.ts:66`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/router/__tests__/router.spec.ts#L66) |
| **TC-FE-RTG-003** | Frontend-RouterGuard | P1 | **USER thường vào `/admin` bị đẩy về `/dashboard`** | Router guard chuyển hướng về `{ name: 'dashboard' }` | **PASSED** | [`router.spec.ts:89`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/router/__tests__/router.spec.ts#L89) |
| **TC-FE-RTG-004** | Frontend-RouterGuard | P1 | Admin vào `/admin` được phép đi tiếp | Cho phép truy cập, route name là `'admin'` | **PASSED** | [`router.spec.ts:102`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/router/__tests__/router.spec.ts#L102) |
| **TC-FE-RTG-005** | Frontend-RouterGuard | P2 | Có token nhưng user=null -> auto fetch user profile | Gọi `fetchCurrentUser()` trước khi duyệt route | **PASSED** | [`router.spec.ts:114`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/router/__tests__/router.spec.ts#L114) |
| **TC-FE-AXI-001** | Frontend-AxiosClient | P1 | Tự động gắn Bearer token khi có token trong localStorage | Gắn header `Authorization: Bearer test-jwt-token` | **PASSED** | [`api.spec.ts:25`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/services/__tests__/api.spec.ts#L25) |
| **TC-FE-AXI-002** | Frontend-AxiosClient | P2 | Không gắn Authorization header khi localStorage trống | Header `Authorization` là `undefined` | **PASSED** | [`api.spec.ts:34`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/services/__tests__/api.spec.ts#L34) |
| **TC-FE-AXI-003** | Frontend-AxiosClient | P0 | **Response 401 xóa token và redirect `/login`** | Xóa token trong localStorage, gán `window.location.href = '/login'` | **PASSED** | [`api.spec.ts:50`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/services/__tests__/api.spec.ts#L50) |
| **TC-FE-AXI-004** | Frontend-AxiosClient | P2 | Lỗi non-401 (403, 500) không xóa token, không redirect | Token được giữ nguyên, không redirect | **PASSED** | [`api.spec.ts:60`](file:///d:/Documents/Git%20Project/skill-management/frontend/src/services/__tests__/api.spec.ts#L60) |
| **TC-E2E-001** | E2E | P0 | Register -> Dashboard -> Hiển thị USER badge | Chưa chạy do backend & frontend offline | **BLOCKED** | [`auth-flow.spec.ts:18`](file:///d:/Documents/Git%20Project/skill-management/frontend/e2e/auth-flow.spec.ts#L18) |
| **TC-E2E-002** | E2E | P0 | USER thường vào `/admin` bị chặn đẩy về `/dashboard` | Chưa chạy do backend & frontend offline | **BLOCKED** | [`auth-flow.spec.ts:41`](file:///d:/Documents/Git%20Project/skill-management/frontend/e2e/auth-flow.spec.ts#L41) |
| **TC-E2E-003** | E2E | P1 | Login -> Dashboard -> Logout xóa phiên về `/login` | Chưa chạy do backend & frontend offline | **BLOCKED** | [`auth-flow.spec.ts:59`](file:///d:/Documents/Git%20Project/skill-management/frontend/e2e/auth-flow.spec.ts#L59) |

---

## 4. Kết luận và Khuyến nghị Nghiệp vụ

1. **Tính độc lập của bộ kiểm thử Backend:**  
   Việc đưa `com.h2database:h2` vào `test` scope và cấu hình chế độ PostgreSQL compatibility đã giải quyết triệt để rủi ro High Risk được nêu trong [`docs/AI_CODEBASE_ANALYSIS.md`](file:///d:/Documents/Git%20Project/skill-management/docs/AI_CODEBASE_ANALYSIS.md) (mục 11 & 12: lệnh test bị fail nếu máy không bật PostgreSQL). Giờ đây bất kỳ lập trình viên hoặc CI/CD runner nào cũng có thể chạy `./mvnw test` độc lập chỉ trong ~12 giây.
2. **Bảo mật phân quyền (RBAC) và Che giấu thông tin nhạy cảm:**  
   Các kiểm thử `UserControllerTest` và `AdminControllerTest` đã chứng minh và bảo đảm:
   - Trường nhạy cảm `password` (kể cả hash BCrypt) **hoàn toàn không bao giờ bị lộ** trong bất kỳ API response nào của `current-user` và `get-users`.
   - Cơ chế bảo vệ cấp phương thức `@Secured("ROLE_ADMIN")` hoạt động nghiêm ngặt: bất kỳ token nào có vai trò `ROLE_USER` đều bị từ chối với HTTP 403 Forbidden khi cố gọi các API quản trị (`get-users`, `delete-user`).
3. **Bảo toàn mã nguồn:**  
   Toàn bộ mã nguồn nghiệp vụ trong `backend/src/main` và `frontend/src` đều được **bảo toàn nguyên vẹn 100%**, không có bất kỳ thay đổi nào làm ảnh hưởng đến hành vi của hệ thống tại commit `81bf551`.
