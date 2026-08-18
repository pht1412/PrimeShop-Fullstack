import React from "react";
import dayjs from "dayjs";

type ConversationItemProps = {
  otherAvatar?: string;
  otherUsername: string;
  lastMessage?: string;
  lastMessageAt?: string | Date;
  unreadCount?: number;
  isActive?: boolean;
  onClick?: () => void;
};

const ConversationItem: React.FC<ConversationItemProps> = ({
  otherAvatar,
  otherUsername,
  lastMessage,
  unreadCount = 0,
  lastMessageAt,
  isActive = false,
  onClick,
}) => {
  return (
    <div
      className={`conversation-item ${isActive ? "active" : ""} ${
        unreadCount > 0 ? "unread" : ""
      }`}
      onClick={onClick}
    >
      <img
        src={otherAvatar || "/images/icon/chatbot.png"}
        alt={otherUsername}
        className="convo-avatar"
      />

      <div className="convo-details">
        <span className="convo-name">{otherUsername}</span>
        <p className="convo-snippet">{lastMessage}</p>
      </div>

      <div className="convo-meta">
        {lastMessageAt && (
          <span className="convo-timestamp">
            {dayjs(lastMessageAt).fromNow()}
          </span>
        )}

        {unreadCount > 0 && (
          <span className="convo-unread-badge">{unreadCount}</span>
        )}
      </div>
    </div>
  );
};

export default ConversationItem;
