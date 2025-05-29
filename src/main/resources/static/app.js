// AI Agent 웹 애플리케이션
class AIAgent {
    constructor() {
        this.conversationId = null;
        this.isConnected = true;
        this.mcpPanelVisible = true;
        this.init();
    }

    init() {
        this.initEventListeners();
        this.loadMcpServers();
        this.loadMcpTools();
        this.setupAutoResize();
    }

    initEventListeners() {
        // 메시지 전송
        document.getElementById('sendButton').addEventListener('click', () => this.sendMessage());
        document.getElementById('messageInput').addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });

        // MCP 패널 토글
        document.getElementById('toggleMcpPanel').addEventListener('click', () => this.toggleMcpPanel());

        // 대화 삭제
        document.getElementById('clearChat').addEventListener('click', () => this.clearChat());

        // MCP 서버 추가
        document.getElementById('addMcpServer').addEventListener('click', () => this.showMcpModal());

        // 모달 관련
        document.getElementById('closeModal').addEventListener('click', () => this.hideMcpModal());
        document.getElementById('cancelModal').addEventListener('click', () => this.hideMcpModal());
        document.getElementById('mcpServerForm').addEventListener('submit', (e) => this.handleMcpServerSubmit(e));

        // 모달 배경 클릭시 닫기
        document.getElementById('mcpModal').addEventListener('click', (e) => {
            if (e.target.id === 'mcpModal') {
                this.hideMcpModal();
            }
        });
    }

    setupAutoResize() {
        const textarea = document.getElementById('messageInput');
        textarea.addEventListener('input', () => {
            textarea.style.height = 'auto';
            textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px';
        });
    }

    async sendMessage() {
        const messageInput = document.getElementById('messageInput');
        const message = messageInput.value.trim();
        
        if (!message) return;

        const useTools = document.getElementById('useTools').checked;
        
        // 사용자 메시지 표시
        this.addMessage('user', message);
        messageInput.value = '';
        messageInput.style.height = 'auto';

        // 전송 버튼 비활성화
        const sendButton = document.getElementById('sendButton');
        sendButton.disabled = true;

        // 타이핑 인디케이터 표시
        this.showTypingIndicator();

        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    message: message,
                    conversationId: this.conversationId,
                    useTools: useTools
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            
            // 대화 ID 저장
            if (!this.conversationId) {
                this.conversationId = data.conversationId;
            }

            // 타이핑 인디케이터 제거
            this.hideTypingIndicator();

            // AI 응답 표시
            this.addMessage('assistant', data.message, data.toolsUsed);

        } catch (error) {
            console.error('Error sending message:', error);
            this.hideTypingIndicator();
            this.addMessage('assistant', '죄송합니다. 오류가 발생했습니다. 다시 시도해 주세요.');
            this.updateConnectionStatus(false);
        } finally {
            sendButton.disabled = false;
        }
    }

    addMessage(role, content, toolsUsed = []) {
        const chatMessages = document.getElementById('chatMessages');
        
        // 웰컴 메시지 제거
        const welcomeMessage = chatMessages.querySelector('.welcome-message');
        if (welcomeMessage) {
            welcomeMessage.remove();
        }

        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${role}`;

        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';
        contentDiv.textContent = content;

        const timeDiv = document.createElement('div');
        timeDiv.className = 'message-time';
        timeDiv.textContent = new Date().toLocaleTimeString();

        messageDiv.appendChild(contentDiv);
        messageDiv.appendChild(timeDiv);

        // 도구 사용 정보 표시
        if (toolsUsed && toolsUsed.length > 0) {
            const toolsDiv = document.createElement('div');
            toolsDiv.className = 'tools-used';
            toolsDiv.textContent = `사용된 도구: ${toolsUsed.join(', ')}`;
            messageDiv.appendChild(toolsDiv);
        }

        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    showTypingIndicator() {
        const chatMessages = document.getElementById('chatMessages');
        const typingDiv = document.createElement('div');
        typingDiv.className = 'message assistant typing-indicator';
        typingDiv.id = 'typing-indicator';

        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';
        contentDiv.innerHTML = `
            <div class="typing-dots">
                <span></span>
                <span></span>
                <span></span>
            </div>
        `;

        typingDiv.appendChild(contentDiv);
        chatMessages.appendChild(typingDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    hideTypingIndicator() {
        const typingIndicator = document.getElementById('typing-indicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
    }

    toggleMcpPanel() {
        const mcpPanel = document.getElementById('mcpPanel');
        this.mcpPanelVisible = !this.mcpPanelVisible;
        
        if (this.mcpPanelVisible) {
            mcpPanel.classList.remove('hidden');
        } else {
            mcpPanel.classList.add('hidden');
        }
    }

    async clearChat() {
        if (this.conversationId) {
            try {
                await fetch(`/api/chat/conversations/${this.conversationId}`, {
                    method: 'DELETE'
                });
            } catch (error) {
                console.error('Error clearing conversation:', error);
            }
        }

        // UI에서 메시지 제거
        const chatMessages = document.getElementById('chatMessages');
        chatMessages.innerHTML = `
            <div class="welcome-message">
                <h2>🌟 AI Agent에 오신 것을 환영합니다!</h2>
                <p>저는 MCP(Model Context Protocol)를 지원하는 AI 어시스턴트입니다.</p>
                <div class="feature-list">
                    <div class="feature-item">
                        <span class="feature-icon">📁</span>
                        <span>파일 시스템 접근</span>
                    </div>
                    <div class="feature-item">
                        <span class="feature-icon">🔍</span>
                        <span>웹 검색</span>
                    </div>
                    <div class="feature-item">
                        <span class="feature-icon">🔧</span>
                        <span>확장 가능한 도구</span>
                    </div>
                </div>
                <p>무엇을 도와드릴까요?</p>
            </div>
        `;

        this.conversationId = null;
    }

    async loadMcpServers() {
        try {
            const response = await fetch('/api/mcp/servers');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const servers = await response.json();
            this.displayMcpServers(servers);
            this.updateConnectionStatus(true);
        } catch (error) {
            console.error('Error loading MCP servers:', error);
            this.updateConnectionStatus(false);
        }
    }

    displayMcpServers(servers) {
        const serversList = document.getElementById('mcpServersList');
        serversList.innerHTML = '';

        if (servers.length === 0) {
            serversList.innerHTML = '<p class="no-servers">MCP 서버가 없습니다.</p>';
            return;
        }

        servers.forEach(server => {
            const serverDiv = document.createElement('div');
            serverDiv.className = 'mcp-server-item';
            serverDiv.innerHTML = `
                <div class="mcp-server-header">
                    <span class="server-name">${server.name}</span>
                    <span class="server-status ${server.enabled ? 'enabled' : 'disabled'}">
                        ${server.enabled ? '활성' : '비활성'}
                    </span>
                </div>
                <div class="server-description">${server.description || '설명 없음'}</div>
                <div class="server-tools">도구 ${server.tools.length}개</div>
                <div class="server-actions">
                    <button class="btn btn-sm btn-outline" onclick="aiAgent.removeServer('${server.id}')">
                        제거
                    </button>
                </div>
            `;
            serversList.appendChild(serverDiv);
        });
    }

    async loadMcpTools() {
        try {
            const response = await fetch('/api/mcp/tools');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const tools = await response.json();
            this.displayMcpTools(tools);
        } catch (error) {
            console.error('Error loading MCP tools:', error);
        }
    }

    displayMcpTools(tools) {
        const toolsList = document.getElementById('mcpToolsList');
        toolsList.innerHTML = '';

        if (tools.length === 0) {
            toolsList.innerHTML = '<p class="no-tools">사용 가능한 도구가 없습니다.</p>';
            return;
        }

        tools.forEach(tool => {
            const toolDiv = document.createElement('div');
            toolDiv.className = 'mcp-tool-item';
            toolDiv.innerHTML = `
                <div class="tool-name">${tool.name}</div>
                <div class="tool-description">${tool.description}</div>
            `;
            toolsList.appendChild(toolDiv);
        });
    }

    showMcpModal() {
        document.getElementById('mcpModal').style.display = 'flex';
        document.getElementById('serverId').focus();
    }

    hideMcpModal() {
        document.getElementById('mcpModal').style.display = 'none';
        document.getElementById('mcpServerForm').reset();
    }

    async handleMcpServerSubmit(e) {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        const serverData = {
            id: formData.get('id'),
            name: formData.get('name'),
            description: formData.get('description') || '',
            command: formData.get('command'),
            args: formData.get('args') ? formData.get('args').split(',').map(s => s.trim()) : [],
            enabled: formData.get('enabled') === 'on',
            tools: []
        };

        try {
            const response = await fetch('/api/mcp/servers', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(serverData)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            this.hideMcpModal();
            this.loadMcpServers();
            this.loadMcpTools();
            
            this.showNotification('MCP 서버가 성공적으로 추가되었습니다.', 'success');
        } catch (error) {
            console.error('Error adding MCP server:', error);
            this.showNotification('MCP 서버 추가 중 오류가 발생했습니다.', 'error');
        }
    }

    async removeServer(serverId) {
        if (!confirm('이 MCP 서버를 제거하시겠습니까?')) {
            return;
        }

        try {
            const response = await fetch(`/api/mcp/servers/${serverId}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            this.loadMcpServers();
            this.loadMcpTools();
            
            this.showNotification('MCP 서버가 성공적으로 제거되었습니다.', 'success');
        } catch (error) {
            console.error('Error removing MCP server:', error);
            this.showNotification('MCP 서버 제거 중 오류가 발생했습니다.', 'error');
        }
    }

    updateConnectionStatus(connected) {
        const statusElement = document.getElementById('connectionStatus');
        this.isConnected = connected;
        
        if (connected) {
            statusElement.textContent = '연결됨';
            statusElement.className = 'status';
        } else {
            statusElement.textContent = '연결 끊김';
            statusElement.className = 'status disconnected';
        }
    }

    showNotification(message, type = 'info') {
        // 간단한 알림 표시
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${type === 'success' ? '#d4edda' : type === 'error' ? '#f8d7da' : '#d1ecf1'};
            color: ${type === 'success' ? '#155724' : type === 'error' ? '#721c24' : '#0c5460'};
            padding: 12px 16px;
            border-radius: 8px;
            z-index: 1001;
            animation: slideIn 0.3s ease;
        `;

        document.body.appendChild(notification);

        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => {
                document.body.removeChild(notification);
            }, 300);
        }, 3000);
    }
}

// CSS 애니메이션 추가
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }

    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }

    .no-servers, .no-tools {
        text-align: center;
        color: #6c757d;
        font-style: italic;
        padding: 20px;
    }

    .server-actions {
        margin-top: 8px;
        display: flex;
        gap: 6px;
    }

    .tools-used {
        font-size: 11px;
        color: #667eea;
        margin-top: 4px;
        padding: 0 8px;
        font-style: italic;
    }
`;
document.head.appendChild(style);

// 애플리케이션 초기화
let aiAgent;
document.addEventListener('DOMContentLoaded', () => {
    aiAgent = new AIAgent();
});
