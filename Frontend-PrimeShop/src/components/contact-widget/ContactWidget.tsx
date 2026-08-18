import React, { useState, useEffect } from 'react';
import { FaHeadset, FaTimes, FaPhoneAlt, FaTools, FaCommentDots, FaRobot } from 'react-icons/fa';
import { SiZalo } from 'react-icons/si'; 
import './ContactWidget.css';

// Import 2 vũ khí hạng nặng
import ChatbotWindow from './ChatbotWindow';
import PrimeShopChat from '../chat/ChatC2C'; // Hãy đảm bảo đường dẫn đúng tới ChatC2C cũ

type WidgetMode = 'closed' | 'menu' | 'ai' | 'c2c';

const ContactWidget: React.FC = () => {
    const [mode, setMode] = useState<WidgetMode>('closed');

    // --- LOGIC 1: Lắng nghe sự kiện "Chat Ngay" từ trang chi tiết sản phẩm ---
    useEffect(() => {
        const handleOpenSignal = () => {
            // Khi có lệnh chat ngay, lập tức chuyển sang chế độ C2C
            setMode('c2c');
        };
        window.addEventListener('PRIMESHOP_OPEN_CHAT', handleOpenSignal);
        return () => window.removeEventListener('PRIMESHOP_OPEN_CHAT', handleOpenSignal);
    }, []);

    // --- LOGIC 2: Điều khiển ---
    const toggleMenu = () => {
        if (mode === 'closed') setMode('menu');
        else setMode('closed'); // Đóng tất cả (menu, chat, ai)
    };

    const openChatBot = () => setMode('ai');
    const openChatC2C = () => setMode('c2c');
    const closeAll = () => setMode('closed');

    return (
        <div className="contact-widget-container">
            
            {/* 1. CỬA SỔ CHATBOT AI */}
            {mode === 'ai' && (
                <ChatbotWindow onClose={closeAll} />
            )}

            {/* 2. CỬA SỔ CHAT C2C (Nhân viên) */}
            {/* Luôn render nhưng ẩn/hiện bằng props để giữ kết nối socket nếu cần */}
            {mode === 'c2c' && (
                <PrimeShopChat isOpen={true} onClose={closeAll} />
            )}

            {/* 3. POPUP MENU (Chỉ hiện khi mode = menu) */}
            <div className={`contact-menu ${mode === 'menu' ? 'open' : ''}`}>
                <div className="contact-menu-header">
                    <FaHeadset size={20} />
                    <span>Liên hệ để được tư vấn</span>
                </div>

                <div className="contact-list">
                    {/* Chatbot AI */}
                    <div className="contact-item" onClick={openChatBot}>
                        <div className="contact-item-icon icon-ai"><FaRobot /></div>
                        <div className="contact-item-info">
                            <span className="contact-label">Trợ lý ảo AI</span>
                            <span className="contact-sub">Tư vấn sản phẩm thông minh 24/7</span>
                        </div>
                    </div>

                    {/* Chat C2C */}
                    <div className="contact-item" onClick={openChatC2C}>
                        <div className="contact-item-icon icon-chat"><FaCommentDots /></div>
                        <div className="contact-item-info">
                            <span className="contact-label">Chat với Nhân viên</span>
                            <span className="contact-sub">Hỗ trợ trực tuyến (8h - 24h)</span>
                        </div>
                    </div>

                    {/* Zalo */}
                    <a href="https://zalo.me/0123456789" target="_blank" rel="noreferrer" className="contact-item">
                        <div className="contact-item-icon icon-zalo"><SiZalo /></div>
                        <div className="contact-item-info">
                            <span className="contact-label">Liên hệ Zalo</span>
                            <span className="contact-sub">Chat ngay (7h30 - 22h30)</span>
                        </div>
                    </a>

                    {/* Phone */}
                    <a href="tel:19008922" className="contact-item">
                        <div className="contact-item-icon icon-phone"><FaPhoneAlt /></div>
                        <div className="contact-item-info">
                            <span className="contact-label">Gọi mua hàng</span>
                            <span className="contact-sub">1900 8922 (7h30 - 22h30)</span>
                        </div>
                    </a>

                    {/* Warranty */}
                    <div className="contact-item">
                        <div className="contact-item-icon icon-warranty"><FaTools /></div>
                        <div className="contact-item-info">
                            <span className="contact-label">Gọi bảo hành</span>
                            <span className="contact-sub">0789 973 973 (8h - 21h)</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* 4. NÚT CHÍNH (FAB) - Luôn hiển thị */}
            {/* Khi đang chat (ai/c2c), nút này có thể ẩn đi hoặc biến thành nút đóng tùy ý. 
                Hiện tại ta giữ nguyên logic: Bấm vào để đóng/mở menu/chat */}
            <button className={`contact-main-btn ${mode !== 'closed' ? 'active' : ''}`} onClick={toggleMenu}>
                {mode !== 'closed' ? (
                    <FaTimes size={20} />
                ) : (
                    <img 
                        src="/logo_contact.jpg" 
                        alt="Liên hệ" 
                        className="contact-main-img"
                    />
                )}
                <span>LIÊN HỆ</span>
            </button>

        </div>
    );
};

export default ContactWidget;