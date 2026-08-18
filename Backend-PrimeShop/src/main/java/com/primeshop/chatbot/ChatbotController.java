package com.primeshop.chatbot;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;
    
    private final List<FAQ> faqs = List.of(
        new FAQ("Xin chào", "Xin chào! Tôi là chatbot của PrimeShop. Bạn có thể hỏi tôi về các câu hỏi thường gặp về PrimeShop."),
        new FAQ("Làm thế nào để đặt hàng trên PrimeShop?", "Bạn chỉ cần chọn sản phẩm, nhấn vào nút 'Mua' hoặc 'Thêm vào giỏ hàng', sau đó truy cập trang giỏ hàng để tiến hành thanh toán."),
        new FAQ("PrimeShop có giao hàng toàn quốc không?", "Dạ có ạ! PrimeShop hỗ trợ giao hàng toàn quốc, kể cả các tỉnh thành xa, qua các đối tác vận chuyển uy tín như Giao Hàng Nhanh, Viettel Post,..."),
        new FAQ("Thời gian giao hàng là bao lâu?", "Thông thường đơn hàng sẽ đến tay bạn trong vòng 2 - 5 ngày làm việc tuỳ vào khu vực và hình thức vận chuyển bạn chọn."),
        new FAQ("Tôi có thể đổi/trả hàng không?", "Bạn được quyền đổi/trả hàng trong vòng 7 ngày nếu sản phẩm bị lỗi do nhà sản xuất hoặc không đúng mô tả. Vui lòng giữ nguyên tem/nhãn và hộp."),
        new FAQ("Tôi có thể nhập hàng số lượng lớn để kinh doanh không?", "Dĩ nhiên là được rồi ạ! PrimeShop hỗ trợ nhập hàng sỉ, đại lý. Bạn có thể liên hệ trực tiếp với chúng tôi qua mục 'Liên hệ' để được hỗ trợ báo giá sỉ tốt nhất.")
    );

    // @GetMapping("/ask")
    // public String ask(@RequestParam String question) {
    //     for (FAQ faq : faqs) {
    //         if (question.toLowerCase().contains(faq.getQuestion().toLowerCase().split(" ")[0])) {
    //             return faq.getAnswer();
    //         }
    //     }
    //     return "Xin lỗi, tôi chưa có câu trả lời cho câu hỏi này.";
    // }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return chatbotService.handleQuestion(question);
    }

    static class FAQ {
        private String question;
        private String answer;
        
        public FAQ(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }
}
