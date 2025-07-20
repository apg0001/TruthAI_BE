package jpabasic.truthaiserver.controller;

import jpabasic.truthaiserver.config.auth.dto.SessionUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 홈(메인) 페이지 컨트롤러
 * - 로그인 사용자 정보 세션에서 꺼내서 뷰에 전달
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HttpSession httpSession; // 세션에서 사용자 정보 관리

    /**
     * 메인 페이지
     * - 로그인 시 사용자 정보 전달
     */
    @GetMapping("/")
    public String home(Model model) {
        // 세션에서 로그인 사용자 정보 꺼내기
        SessionUser user = (SessionUser) httpSession.getAttribute("user");

        if (user != null) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("userPicture", user.getPicture());
            System.out.println(user.getName());
        }
        return "home";
    }
}