import { useState, useRef, useEffect } from "react";
import api from "../../api/api";
import "./styles/chatbox.css";
import "./styles/prime-chat.css";
// [MODIFIED] Thay icon Toggle bằng icon Close (FaTimes)
import { FaPaperPlane, FaSpinner, FaFacebookMessenger, FaTimes } from "react-icons/fa";
import dayjs from "../../config/dayjsConfig";
import { wsChatClient } from "./wsClient";
import ConversationItem from "./ConversationItem";

const userData = localStorage.getItem("user");
const userObject = userData ? JSON.parse(userData) : null;
const CURRENT_USER_ID = userObject?.id || 0;

interface Conversation {
  id: number;
  otherUserId: number;
  otherUsername: string;
  otherAvatar: string;
  lastMessage: string;
  lastMessageAt: string;
  unreadCount: number;
}

interface Message {
  id: number;
  conversationId: number;
  content: string;
  senderId: number;
  createdAt: string;
}

// [UPDATED] Thêm Props để nhận lệnh từ ContactWidget
interface PrimeShopChatProps {
    isOpen?: boolean;      // Trạng thái mở/đóng do cha quản lý
    onClose?: () => void;  // Hàm để báo cho cha biết muốn đóng
}

export default function PrimeShopChat({ isOpen = false, onClose }: PrimeShopChatProps) {
  // [MODIFIED] Bỏ state isCollapsed nội bộ, dùng prop isOpen
  // Tuy nhiên, để tương thích ngược (nếu dùng độc lập), ta vẫn giữ biến local nếu isOpen không được truyền
  const [internalOpen, setInternalOpen] = useState(false);
  
  // Biến quyết định thực sự có hiện hay không
  const isVisible = onClose ? isOpen : internalOpen; 

  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversationId, setSelectedConversationId] = useState<number | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [isLoadingConversations, setIsLoadingConversations] = useState(false);
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);
  const [isWsConnected, setIsWsConnected] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const chatMessagesRef = useRef<HTMLDivElement>(null);

  // --- 1. OPTIMISTIC UPDATE KHI BẤM "CHAT NGAY" ---
  useEffect(() => {
    const handleOpenChatSignal = (event: Event) => {
      const customEvent = event as CustomEvent;
      const conversationData = customEvent.detail;

      if (conversationData) {
        // [MODIFIED] Nếu có onClose (đang dùng trong Widget), thì không set state nội bộ
        if (!onClose) setInternalOpen(true); 
        
        setConversations((prev) => {
          const exists = prev.find((c) => c.id === conversationData.id);
          if (exists) {
              const others = prev.filter(c => c.id !== conversationData.id);
              return [exists, ...others];
          }
          return [{...conversationData, unreadCount: 0}, ...prev];
        });
        handleSelectConversation(conversationData.id);
      }
    };
    window.addEventListener('PRIMESHOP_OPEN_CHAT', handleOpenChatSignal);
    return () => {
      window.removeEventListener('PRIMESHOP_OPEN_CHAT', handleOpenChatSignal);
    };
  }, [onClose]);

  // ... (Giữ nguyên toàn bộ Logic chọn hội thoại, fetch, socket, infinite scroll không đổi) ...
  // --- 2. HÀM CHỌN HỘI THOẠI (XỬ LÝ ĐỌC TIN) ---
  const handleSelectConversation = (id: number) => {
      setSelectedConversationId(id);
      setConversations(prev => prev.map(c => 
          c.id === id ? { ...c, unreadCount: 0 } : c
      ));
  };

  const fetchConversations = async () => {
    setIsLoadingConversations(true);
    try {
      const res = await api.get("/chat/conversations");
      setConversations(res.data);
    } catch (err) {
      console.error("Lỗi fetchConversations:", err);
    } finally {
      setIsLoadingConversations(false);
    }
  };

  const loadMessages = async (conversationId: number | null, pageToLoad: number = 0) => {
    if (!conversationId) return;
    setIsLoadingMessages(pageToLoad === 0);
    setIsLoadingMore(pageToLoad > 0);
    try {
      const res = await api.get(`/chat/messages`, {
        params: { conversationId, page: pageToLoad, size: 30 },
      });
      if (res.data && res.data.content) {
        const newMessages = res.data.content.reverse();
        setMessages((prev) => (pageToLoad === 0 ? newMessages : [...newMessages, ...prev]));
        setHasMore(res.data.content.length === 30);
        setPage(pageToLoad);
      } else {
        setMessages([]);
        setHasMore(false);
      }
    } catch (err) {
      console.error("Lỗi loadMessages:", err);
    } finally {
      setIsLoadingMessages(false);
      setIsLoadingMore(false);
    }
  };

  const sendMessage = () => {
    if (!input.trim() || !selectedConversationId) return;
    if (!wsChatClient.isConnected()) return;
    setIsSending(true);
    wsChatClient.sendMessage(selectedConversationId, input);
    setInput("");
  };

  useEffect(() => {
    // Chỉ fetch khi mở cửa sổ chat
    if (isVisible) {
        fetchConversations();
        if (!wsChatClient.isConnected() && !wsChatClient.client) {
            wsChatClient.connect(
                () => setIsWsConnected(true),
                () => console.error("Lỗi kết nối WebSocket")
            );
        } else if (wsChatClient.isConnected()) {
            setIsWsConnected(true);
        }
    }
  }, [isVisible]); // [MODIFIED] Thêm dependency isVisible

  // Xử lý Infinite Scroll
  useEffect(() => {
    if (!chatMessagesRef.current || !hasMore || isLoadingMore) return;
    const handleScroll = () => {
      if (chatMessagesRef.current && chatMessagesRef.current.scrollTop < 50 && !isLoadingMore) {
        loadMessages(selectedConversationId, page + 1);
      }
    };
    chatMessagesRef.current.addEventListener("scroll", handleScroll);
    return () => chatMessagesRef.current?.removeEventListener("scroll", handleScroll);
  }, [selectedConversationId, page, hasMore, isLoadingMore]);

  // Subscribe conversation
  useEffect(() => {
    if (!selectedConversationId) return;
    setPage(0);
    setHasMore(true);
    loadMessages(selectedConversationId, 0);

    if (!isWsConnected) return;

    const sub = wsChatClient.subscribeConversation(
      selectedConversationId,
      (msg: Message) => {
        setMessages((prev) => {
          if (msg.id && prev.some((m) => m.id === msg.id)) return prev;
          if (msg.senderId === CURRENT_USER_ID) setIsSending(false);
          return [...prev, msg];
        });
        
        setConversations(prev => prev.map(c => 
            c.id === selectedConversationId 
            ? { ...c, lastMessage: msg.content, lastMessageAt: msg.createdAt } 
            : c
        ));
      }
    );

    return () => {
      wsChatClient.unsubscribeConversation(selectedConversationId);
      setIsSending(false);
    };
  }, [isWsConnected, selectedConversationId]);

  // Global listener
  useEffect(() => {
    if (!isWsConnected) return;
    
    const sub = wsChatClient.subscribePersonal((msg: Message) => {
      console.log("🔔 Có tin nhắn mới từ:", msg.senderId);

      setConversations((prev) => {
          const targetConv = prev.find(c => c.id === msg.conversationId);
          if (targetConv) {
              const isLookingAtIt = selectedConversationId === msg.conversationId;
              const updatedConv = {
                  ...targetConv,
                  lastMessage: msg.content,
                  lastMessageAt: msg.createdAt,
                  unreadCount: isLookingAtIt ? 0 : (targetConv.unreadCount || 0) + 1
              };
              const others = prev.filter(c => c.id !== msg.conversationId);
              return [updatedConv, ...others];
          } else {
              fetchConversations();
              return prev; 
          }
      });
    });

    return () => sub?.unsubscribe();
  }, [isWsConnected, selectedConversationId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const getHeaderText = () => {
    // [MODIFIED] Khi đóng thì không cần header text vì component sẽ ẩn luôn
    const selected = conversations.find((c) => c.id === selectedConversationId);
    return selected ? selected.otherUsername : "Tin nhắn";
  };

  // [MODIFIED] Logic hiển thị: Nếu không visible -> Ẩn luôn (trả về null)
  if (!isVisible) return null;

  return (
    // [MODIFIED] Bỏ class 'collapsed' vì giờ đây ta ẩn hiện bằng 'isVisible'
    <div className={`chatbox-container open`}>
      {/* HEADER */}
      <div className="chatbox-header">
        <div className="header-title">
             <FaFacebookMessenger size={20} />
             <span>{getHeaderText()}</span>
        </div>
        
        {/* [MODIFIED] Nút Close thay vì Toggle */}
        <button className="chatbox-toggle" onClick={() => onClose ? onClose() : setInternalOpen(false)}>
          <FaTimes /> 
        </button>
      </div>

      {/* CONTENT (Luôn hiện nếu isVisible = true) */}
      <div className="chatbox-content">
          <div className="primeshop-chat-layout">
            
            {/* LEFT SIDEBAR */}
            <div className="chat-sidebar">
              <div className="sidebar-search">
                <input type="text" placeholder="Tìm kiếm người dùng..." />
              </div>
              <div className="conversation-list">
                {isLoadingConversations ? (
                  <div className="chat-loading"><FaSpinner className="spin" /></div>
                ) : (
                  conversations.map((convo) => (
                    <ConversationItem
                      key={convo.id}
                      otherAvatar={convo.otherAvatar}
                      otherUsername={convo.otherUsername}
                      lastMessage={convo.lastMessage}
                      lastMessageAt={convo.lastMessageAt}
                      unreadCount={convo.unreadCount}
                      isActive={convo.id === selectedConversationId}
                      onClick={() => handleSelectConversation(convo.id)}
                    />
                  ))
                )}
              </div>
            </div>

            {/* RIGHT MAIN CHAT */}
            <div className="chat-main">
              {!selectedConversationId ? (
                <div className="chat-empty-main">
                  <img src="/images/icon/chatbot.png" alt="Welcome" style={{width: 80, opacity: 0.8, marginBottom: 15}}/>
                  <h4>Chào mừng đến PrimeShop Chat</h4>
                  <p>Chọn một cuộc trò chuyện để bắt đầu.</p>
                </div>
              ) : (
                <>
                  <div className="chat-messages" ref={chatMessagesRef}>
                    {isLoadingMore && <div className="chat-loading"><FaSpinner className="spin" /></div>}
                    {messages.map((msg) => (
                        <div key={msg.id} className={`chat-message ${msg.senderId === CURRENT_USER_ID ? "me" : "other"}`}>
                            {msg.senderId !== CURRENT_USER_ID && (
                                <div className="message-avatar-spacer"></div>
                            )}
                            <div className="message-bubble">
                                <p>{msg.content}</p>
                                <span className="msg-time">{dayjs(msg.createdAt).format("HH:mm")}</span>
                            </div>
                        </div>
                    ))}
                    <div ref={messagesEndRef} />
                  </div>
                  
                  <div className="chat-input-container">
                    <input
                      type="text"
                      className="chat-input"
                      placeholder="Nhập tin nhắn..."
                      value={input}
                      onChange={(e) => setInput(e.target.value)}
                      onKeyDown={(e) => e.key === "Enter" && !isSending && sendMessage()}
                    />
                    <button className="chat-send-btn" onClick={sendMessage} disabled={isSending}>
                      {isSending ? <FaSpinner className="spin" /> : <FaPaperPlane />}
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
    </div>
  );
}