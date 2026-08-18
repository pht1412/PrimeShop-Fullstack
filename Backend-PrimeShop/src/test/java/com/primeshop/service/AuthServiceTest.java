package com.primeshop.service;

// Import các thư viện cốt lõi của JUnit 5 và Mockito
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.primeshop.security.JwtUtil;

import com.primeshop.user.AuthService;
import com.primeshop.user.UserRepo;
import com.primeshop.user.User;

import java.util.HashSet;
import java.util.Optional;

// 1. CHUẨN BỊ MÔI TRƯỜNG
// Báo cho JUnit 5 biết: "Này, tui sẽ dùng Mockito trong file test này nhé!"
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // @InjectMocks: Đây là "Nhân vật chính" mà chúng ta mang ra để test
    // (AuthService).
    // Mockito sẽ tự động tạo ra một AuthService và "bơm" (inject) các diễn viên
    // đóng thế vào nó.
    @InjectMocks
    private AuthService authService;

    // @Mock: Đây là các "Diễn viên đóng thế".
    // Vì ta chỉ muốn test logic của AuthService, ta không được phép gọi xuống
    // Database thật.
    // Ta tạo ra các bản giả (Mock) của UserRepo, PasswordEncoder và JwtUtil.
    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    // @Test: Đánh dấu đây là một kịch bản kiểm thử để JUnit 5 có thể chạy nó.
    @Test
    void testLogin_Success() {

        // ==========================================
        // BƯỚC 1: ARRANGE (Sắp xếp / Chuẩn bị kịch bản)
        // Mục tiêu: Cung cấp dữ liệu đầu vào và "dạy" các diễn viên đóng thế cách diễn.
        // ==========================================

        // 1.1 Chuẩn bị dữ liệu đầu vào (Input)
        String inputUsername = "user01";
        String inputPassword = "user01";
        String encodedPasswordInDb = "encoded_password_123";
        String expectedToken = "day-la-chuoi-jwt-token-gia";

        // 1.2 Tạo một User giả như thể nó vừa được lấy lên từ Database
        User mockUser = new User();
        mockUser.setUsername(inputUsername);
        mockUser.setPassword(encodedPasswordInDb);
        mockUser.setRoles(new HashSet<>()); // Giả sử user này chưa có Role nào cho đơn giản

        // 1.3 Lên kịch bản (Mocking hành vi) bằng câu lệnh
        // Mockito.when(...).thenReturn(...)
        // - Dạy UserRepo: Khi ai đó tìm kiếm "testuser", hãy trả về mockUser bọc trong
        // Optional (vì code gốc dùng Optional)
        Mockito.when(userRepo.findByUsername(inputUsername)).thenReturn(Optional.of(mockUser));

        // - Dạy PasswordEncoder: Khi ai đó so sánh "password123" với pass đã mã hóa,
        // hãy luôn gật đầu (trả về true)
        Mockito.when(passwordEncoder.matches(inputPassword, encodedPasswordInDb)).thenReturn(true);

        // - Dạy JwtUtil: Khi được yêu cầu tạo token cho "testuser", hãy phát ra chuỗi
        // token giả đã chuẩn bị
        // Mockito.anyList() nghĩa là danh sách role là gì cũng được, không quan trọng
        // trong test này
        Mockito.when(jwtUtil.generateToken(Mockito.eq(inputUsername), Mockito.anyList())).thenReturn(expectedToken);

        // ==========================================
        // BƯỚC 2: ACT (Thực thi)
        // Mục tiêu: Bấm nút chạy "nhân vật chính" với dữ liệu đã chuẩn bị.
        // ==========================================

        // Gọi hàm login() thực sự trong AuthService
        String actualToken = authService.login(inputUsername, inputPassword);

        // ==========================================
        // BƯỚC 3: ASSERT (Xác nhận kết quả)
        // Mục tiêu: Kiểm tra xem kết quả thực tế có đúng với kỳ vọng không.
        // ==========================================

        // So sánh Token trả về có giống với Token giả ta mong đợi không?
        // Nếu khác nhau, bài test sẽ báo Đỏ (Fail). Nếu giống, bài test báo Xanh
        // (Pass).
        Assertions.assertEquals(expectedToken, actualToken, "Token trả về không khớp với mong đợi!");

        // BƯỚC THÊM: VERIFY (Xác minh hành vi)
        // Để chắc chắn code không chạy sai luồng, ta kiểm tra xem UserRepo có thực sự
        // được gọi hàm findByUsername đúng 1 lần hay không.
        Mockito.verify(userRepo, Mockito.times(1)).findByUsername(inputUsername);
    }
}