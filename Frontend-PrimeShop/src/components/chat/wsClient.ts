import { Client, StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";

class WSChatClient {
  public client: Client | null = null;
  private connected = false;
  private personalSubscription: StompSubscription | null = null;
  private conversationSubscriptions: Map<number, StompSubscription> = new Map();

  connect(onConnected: () => void, onMessageReceived: (msg: any) => void) {
    const token = localStorage.getItem("token");
    this.client = new Client({
      webSocketFactory: () => new SockJS("https://backend-primeshop.onrender.com/ws"),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str) => console.log("%c" + str, "color: #0a7"),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("✅ WebSocket Connected");
        this.connected = true;
        if (!this.personalSubscription) {
          this.personalSubscription = this.subscribePersonal(onMessageReceived);
        }
        this.conversationSubscriptions.forEach((_, id) => {
          this.subscribeConversation(id, onMessageReceived);
        });
        onConnected();
      },
      onStompError: (frame) => {
        console.error("❌ STOMP Error:", frame.headers["message"]);
      },
      onWebSocketClose: () => {
        console.warn("⛔ WebSocket Closed");
        this.connected = false;
        this.personalSubscription = null;
      },
    });
    this.client.activate();
  }

  isConnected() {
    return this.connected;
  }

  subscribePersonal(onMessageReceived: (msg: any) => void) {
    if (!this.client || !this.connected) return;
    console.log("📩 Subscribing personal channel...");
    return this.client.subscribe("/user/queue/messages", (message) => {
      console.log("📥 RAW personal message:", message.body);
      this.safeParse(message.body, onMessageReceived);
    });
  }

  subscribeConversation(conversationId: number, onMessageReceived: (msg: any) => void) {
    if (!this.client || !this.connected) {
      console.error("🚫 Cannot subscribe: WebSocket not connected");
      return;
    }
    const destination = `/topic/conversation/${conversationId}`;
    console.log("💬 Subscribing to conversation:", destination);
    if (this.conversationSubscriptions.has(conversationId)) {
      console.log(`🔄 Unsubscribing old subscription for conversation ${conversationId}`);
      this.conversationSubscriptions.get(conversationId)?.unsubscribe();
      this.conversationSubscriptions.delete(conversationId);
    }
    const subscription = this.client.subscribe(destination, (message) => {
      console.log(`🎯 Received message on ${destination}:`, message.body);
      this.safeParse(message.body, onMessageReceived);
    });
    this.conversationSubscriptions.set(conversationId, subscription);
    console.log(`✅ Subscribed to conversation ${conversationId}`);
    return subscription;
  }

  unsubscribeConversation(conversationId: number) {
    const sub = this.conversationSubscriptions.get(conversationId);
    if (sub) {
      console.log("❌ Unsubscribe conversation:", conversationId);
      sub.unsubscribe();
      this.conversationSubscriptions.delete(conversationId);
    }
  }

  sendMessage(conversationId: number, content: string) {
    if (!this.client || !this.connected) {
      console.error("🚫 WS not connected");
      return;
    }
    this.client.publish({
      destination: "/app/chat.sendMessage",
      body: JSON.stringify({ conversationId, content }),
    });
  }

  private safeParse(body: string, callback: (msg: any) => void) {
    try {
      const obj = JSON.parse(body);
      callback(obj);
    } catch (e) {
      console.error("⚠️ JSON parse error:", e);
    }
  }
}

export const wsChatClient = new WSChatClient();
