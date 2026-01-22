package com.example.ExpenseTracker.controller;

import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthControllerUnitTest {

    @Mock AuthService authService;
    @Mock Model model;
    @Mock BindingResult br;
    @Mock HttpSession session;

    @InjectMocks AuthController controller;

    @BeforeEach
    void init() throws Exception {
        MockitoAnnotations.openMocks(this);

        // set private @Value baseUrl field without Spring
        var f = AuthController.class.getDeclaredField("baseUrl");
        f.setAccessible(true);
        f.set(controller, "http://localhost:8080");
    }

    @Test
    void registerPage_returnsView_andAddsDto() {
        String view = controller.registerPage(model);
        assertThat(view).isEqualTo("auth/register");
        verify(model).addAttribute(eq("registerDto"), any(RegisterDto.class));
    }

    @Test
    void register_whenBindingErrors_returnsRegisterView() {
        when(br.hasErrors()).thenReturn(true);

        String view = controller.register(new RegisterDto(), br, model);

        assertThat(view).isEqualTo("auth/register");
        verifyNoInteractions(authService);
    }

    @Test
    void register_success_returnsVerifyOtpView_andAddsAttributes() {
        when(br.hasErrors()).thenReturn(false);
        RegisterDto dto = new RegisterDto();
        dto.setEmail("a@test.com");

        doNothing().when(authService).register(dto);

        String view = controller.register(dto, br, model);

        assertThat(view).isEqualTo("auth/verify-otp");
        verify(model).addAttribute("email", "a@test.com");
        verify(model).addAttribute(eq("otpDto"), any(OtpDto.class));
    }

    @Test
    void register_failure_returnsRegisterView_withError() {
        when(br.hasErrors()).thenReturn(false);
        RegisterDto dto = new RegisterDto();
        dto.setEmail("a@test.com");

        doThrow(new RuntimeException("Email already exists")).when(authService).register(dto);

        String view = controller.register(dto, br, model);

        assertThat(view).isEqualTo("auth/register");
        verify(model).addAttribute("error", "Email already exists");
    }

    @Test
    void verifyOtpPage_setsEmailInOtpDto() {
        String view = controller.verifyOtpPage("a@test.com", model);

        assertThat(view).isEqualTo("auth/verify-otp");
        verify(model).addAttribute(eq("otpDto"), argThat((OtpDto d) ->
                "a@test.com".equals(d.getEmail())
        ));
    }

    @Test
    void verifyOtp_whenErrors_returnsVerifyOtpView() {
        when(br.hasErrors()).thenReturn(true);

        String view = controller.verifyOtp(new OtpDto(), br, model);

        assertThat(view).isEqualTo("auth/verify-otp");
        verifyNoInteractions(authService);
    }

    @Test
    void verifyOtp_success_redirectsLogin() {
        when(br.hasErrors()).thenReturn(false);
        OtpDto dto = new OtpDto();
        dto.setEmail("a@test.com");
        dto.setOtp("123456");

        doNothing().when(authService).verifyOtp("a@test.com", "123456");

        String view = controller.verifyOtp(dto, br, model);

        assertThat(view).isEqualTo("redirect:/login?verified");
    }

    @Test
    void loginPage_returnsView_andAddsDto() {
        String view = controller.loginPage(model);

        assertThat(view).isEqualTo("auth/login");
        verify(model).addAttribute(eq("loginDto"), any(LoginDto.class));
    }

    @Test
    void login_whenErrors_returnsLoginView() {
        when(br.hasErrors()).thenReturn(true);

        String view = controller.login(new LoginDto(), br, session, model);

        assertThat(view).isEqualTo("auth/login");
        verifyNoInteractions(authService);
    }

    @Test
    void login_success_setsSession_andRedirectsDashboard() {
        when(br.hasErrors()).thenReturn(false);
        LoginDto dto = new LoginDto();
        dto.setEmail("a@test.com");
        dto.setPassword("Password@123");

        User u = new User();
        u.setId(10L);
        u.setRole("USER");

        when(authService.login("a@test.com", "Password@123")).thenReturn(u);

        String view = controller.login(dto, br, session, model);

        assertThat(view).isEqualTo("redirect:/dashboard");
        verify(session).setAttribute("USER_ID", 10L);
        verify(session).setAttribute("ROLE", "USER");
    }

    @Test
    void logout_invalidatesSession_andRedirects() {
        String view = controller.logout(session);

        assertThat(view).isEqualTo("redirect:/login?logout");
        verify(session).invalidate();
    }

    @Test
    void forgotPage_addsDto_andReturnsView() {
        String view = controller.forgotPage(model);

        assertThat(view).isEqualTo("auth/forgot-password");
        verify(model).addAttribute(eq("forgotPasswordDto"), any(ForgotPasswordDto.class));
    }

    @Test
    void forgot_success_setsSuccessMessage() {
        when(br.hasErrors()).thenReturn(false);

        ForgotPasswordDto dto = new ForgotPasswordDto();
        dto.setEmail("a@test.com");

        doNothing().when(authService).forgotPassword("a@test.com", "http://localhost:8080");

        String view = controller.forgot(dto, br, model);

        assertThat(view).isEqualTo("auth/forgot-password");
        verify(model).addAttribute("success", "Reset link sent to email");
    }

    @Test
    void resetPage_setsTokenAndReturnsView() {
        String view = controller.resetPage("token123", model);

        assertThat(view).isEqualTo("auth/reset-password");
        verify(model).addAttribute(eq("resetPasswordDto"), argThat((ResetPasswordDto d) ->
                "token123".equals(d.getToken())
        ));
    }

    @Test
    void reset_success_redirectsLogin() {
        when(br.hasErrors()).thenReturn(false);

        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setToken("t");
        dto.setNewPassword("NewPass@123");

        doNothing().when(authService).resetPassword("t", "NewPass@123");

        String view = controller.reset(dto, br, model);

        assertThat(view).isEqualTo("redirect:/login?resetSuccess");
    }
}
