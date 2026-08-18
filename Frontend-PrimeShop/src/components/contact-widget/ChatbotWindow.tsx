import React, { useState, useRef, useEffect } from 'react';
import { FaRobot, FaTimes, FaPaperPlane } from 'react-icons/fa';
import api from '../../api/api'; // Đảm bảo đường dẫn api đúng
import './ChatbotWindow.css';
import { useNavigate } from 'react-router-dom';

interface ChatbotWindowProps {
    onClose: () => void;
}

interface Message {
    id: number;
    sender: 'user' | 'bot';
    text?: string;
    products?: any[]; // Nếu bot trả về danh sách sản phẩm
}

const ChatbotWindow: React.FC<ChatbotWindowProps> = ({ onClose }) => {
    const [messages, setMessages] = useState<Message[]>([
        { id: 1, sender: 'bot', text: 'Xin chào! Tôi là trợ lý ảo AI. Bạn cần tìm sản phẩm gì hôm nay?' }
    ]);
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const navigate = useNavigate();

    // Tự động cuộn xuống cuối khi có tin nhắn mới
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    const handleSend = async () => {
        if (!input.trim()) return;

        // 1. Thêm tin nhắn User
        const userMsg: Message = { id: Date.now(), sender: 'user', text: input };
        setMessages(prev => [...prev, userMsg]);
        setInput('');
        setIsLoading(true);

        try {
            // 2. Gọi API Gemini
            const res = await api.post('/gemini/advice', { question: userMsg.text });
            const answerRaw = res.data.answer; // Chuỗi JSON hoặc text từ Backend

            let botMsg: Message = { id: Date.now() + 1, sender: 'bot' };

            try {
                // Bước 1: Làm sạch chuỗi (xóa markdown thừa nếu có)
                let cleanAnswer = answerRaw;
                if (typeof answerRaw === 'string') {
                    cleanAnswer = answerRaw.replace(/```json/g, '').replace(/```/g, '').trim();
                }

                // Bước 2: Thử Parse JSON
                // Nếu cleanAnswer là object sẵn (do axios tự parse) thì dùng luôn
                const parsedData = (typeof cleanAnswer === 'object') ? cleanAnswer : JSON.parse(cleanAnswer);

                // Bước 3: Kiểm tra cấu trúc dữ liệu
                if (Array.isArray(parsedData) && parsedData.length > 0) {
                    // Case A: Là danh sách sản phẩm -> Hiển thị thẻ
                    botMsg.text = "Dưới đây là các sản phẩm phù hợp với nhu cầu của bạn:";
                    botMsg.products = parsedData;
                } else if (parsedData.message) {
                    // Case B: Là thông báo lỗi/từ chối từ Backend
                    botMsg.text = parsedData.message;
                } else if (parsedData.error) {
                    // Case C: Lỗi từ API
                    botMsg.text = "⚠️ " + parsedData.error;
                } else {
                    // Case D: JSON lạ -> Chuyển thành chuỗi để hiện
                    botMsg.text = typeof parsedData === 'string' ? parsedData : JSON.stringify(parsedData);
                }

            } catch (e) {
                // Case E: Không phải JSON (Text thường) -> Hiển thị nguyên văn
                // Đây là chỗ cứu cánh cho trường hợp Gemini trả lời "Chào bạn..."
                console.warn("Non-JSON response from Gemini:", answerRaw);
                botMsg.text = typeof answerRaw === 'string' ? answerRaw : "Xin lỗi, tôi không hiểu câu trả lời.";
            }

            setMessages(prev => [...prev, botMsg]);

        } catch (error) {
            console.error("Gemini Error:", error);
            setMessages(prev => [...prev, { 
                id: Date.now() + 1, 
                sender: 'bot', 
                text: 'Xin lỗi, hệ thống đang bận. Vui lòng thử lại sau.' 
            }]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') handleSend();
    };

    // Hàm format tiền tệ
    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
    };

    // Xử lý khi click vào sản phẩm gợi ý -> Chuyển hướng đến chi tiết
    const handleProductClick = (productId: number) => {
        // Tạm thời chuyển hướng theo ID, sau này nên dùng Slug nếu có
        // navigate(`/product-detail/${productId}`); // Nếu dùng ID
        // Hoặc tìm cách lấy slug từ backend
        window.location.href = `/product-detail/${productId}`; // Redirect cứng tạm thời
    };

    return (
        <div className="chatbot-window">
            {/* Header */}
            <div className="chatbot-header">
                <div className="chatbot-title">
                    <FaRobot size={20} />
                    <span>Trợ lý ảo PrimeShop</span>
                </div>
                <button className="chatbot-close-btn" onClick={onClose}>
                    <FaTimes />
                </button>
            </div>

            {/* Body List Messages */}
            <div className="chatbot-body">
                {messages.map((msg) => (
                    <div key={msg.id} className={`chat-bubble-container ${msg.sender}`}>
                        {/* Text Message */}
                        {msg.text && (
                            <div className={`chat-bubble ${msg.sender}`}>
                                {msg.text}
                            </div>
                        )}

                        {/* Product Cards (Nếu có) */}
                        {msg.products && (
                            <div className="bot-product-list">
                                {msg.products.map((prod: any, idx: number) => (
                                    <div key={idx} className="bot-product-card" onClick={() => handleProductClick(prod.id)}>
                                        <img 
                                            src={prod.image || "https://via.placeholder.com/50"} 
                                            alt={prod.name} 
                                            className="bot-prod-img" 
                                        />
                                        <div className="bot-prod-info">
                                            <h4>{prod.name}</h4>
                                            <div className="bot-prod-price">{formatPrice(prod.price)}</div>
                                            <div className="bot-prod-reason">💡 {prod.reason}</div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                ))}
                
                {isLoading && (
                    <div className="chat-bubble bot typing-indicator">
                        <span></span><span></span><span></span>
                    </div>
                )}
                <div ref={messagesEndRef} />
            </div>

            {/* Footer Input */}
            <div className="chatbot-footer">
                <input 
                    type="text" 
                    className="chatbot-input" 
                    placeholder="Hỏi về laptop, điện thoại..." 
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyDown={handleKeyDown}
                    autoFocus
                />
                <button className="chatbot-send-btn" onClick={handleSend} disabled={isLoading}>
                    <FaPaperPlane size={14} />
                </button>
            </div>
        </div>
    );
};

export default ChatbotWindow;