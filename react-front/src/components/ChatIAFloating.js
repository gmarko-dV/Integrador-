import React, { useState } from 'react';
import ChatIA from './ChatIA';
import './ChatIA.css';

const ChatIAFloating = () => {
  const [isOpen, setIsOpen] = useState(false);

  const toggleChat = () => {
    setIsOpen(!isOpen);
  };

  const closeChat = () => {
    setIsOpen(false);
  };

  const handleToggleChat = (e) => {
    e.preventDefault();
    e.stopPropagation();
    toggleChat();
  };

  return (
    <div className="chat-ia-floating-wrapper">
      {isOpen ? (
        <>
          <ChatIA isFloating={true} onClose={closeChat} />
          {/* Botón flotante para cerrar - visible cuando el chat está abierto */}
          <button
            className="chat-ia-floating-button chat-ia-floating-button-close"
            onClick={closeChat}
            aria-label="Cerrar chat IA"
            title="Cerrar chat"
          >
            ✕
          </button>
        </>
      ) : (
        <button
          className="chat-ia-floating-button"
          onClick={handleToggleChat}
          aria-label="Abrir chat IA"
          title="Chat IA"
          type="button"
        >
          <img 
            src="/chatbot-web.png" 
            alt="Chat IA" 
            className="chat-ia-logo"
            onError={(e) => {
              // Fallback al emoji si la imagen no se carga
              e.target.style.display = 'none';
              e.target.parentElement.textContent = '🤖';
            }}
          />
        </button>
      )}
    </div>
  );
};

export default ChatIAFloating;


