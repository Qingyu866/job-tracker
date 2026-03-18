import { API_CONFIG } from '@/utils/constants';
import type { WebSocketMessage } from '@/types/chat';

export enum WebSocketMessageType {
  CHAT = 'CHAT',
  HEARTBEAT = 'HEARTBEAT',
  ERROR = 'ERROR',
}

type WebSocketEventHandler = (data: any) => void;
type WebSocketErrorHandler = (error: Event) => void;
type WebSocketCloseHandler = (event: CloseEvent) => void;

const TOKEN_KEY = 'authorization';

class WebSocketManager {
  private ws: WebSocket | null = null;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private heartbeatTimer: ReturnType<typeof setTimeout> | null = null;
  private isManualClose: boolean = false;
  private reconnectAttempts: number = 0;
  private maxReconnectAttempts: number = 5;

  private onMessageHandler: WebSocketEventHandler | null = null;
  private onErrorHandler: WebSocketErrorHandler | null = null;
  private onCloseHandler: WebSocketCloseHandler | null = null;
  private onOpenHandler: WebSocketEventHandler | null = null;

  private getWsUrl(): string {
    const token = localStorage.getItem(TOKEN_KEY);
    const baseUrl = API_CONFIG.wsURL;
    
    if (token) {
      const separator = baseUrl.includes('?') ? '&' : '?';
      return `${baseUrl}${separator}token=${encodeURIComponent(token)}`;
    }
    
    return baseUrl;
  }

  connect(): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('[WebSocket] 已经连接');
      return;
    }

    try {
      const wsUrl = this.getWsUrl();
      console.log('[WebSocket] 正在连接...', wsUrl.replace(/token=[^&]+/, 'token=***'));
      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = (event) => {
        console.log('[WebSocket] 连接成功');
        this.isManualClose = false;
        this.reconnectAttempts = 0;

        this.startHeartbeat();

        if (this.onOpenHandler) {
          this.onOpenHandler(event);
        }
      };

      this.ws.onmessage = (event) => {
        console.log('[WebSocket] 收到消息:', event.data);

        try {
          const message = JSON.parse(event.data);

          if (message.type === WebSocketMessageType.HEARTBEAT) {
            return;
          }

          if (this.onMessageHandler) {
            this.onMessageHandler(message);
          }
        } catch (error) {
          console.error('[WebSocket] 消息解析失败:', error);
        }
      };

      this.ws.onerror = (event) => {
        console.error('[WebSocket] 连接错误:', event);

        if (this.onErrorHandler) {
          this.onErrorHandler(event);
        }
      };

      this.ws.onclose = (event) => {
        console.log('[WebSocket] 连接关闭:', event.code, event.reason);

        this.stopHeartbeat();

        if (this.onCloseHandler) {
          this.onCloseHandler(event);
        }

        if (!this.isManualClose) {
          if (event.code === 1008) {
            console.error('[WebSocket] 认证失败，跳转登录页');
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem('userinfo');
            
            const currentPath = window.location.pathname;
            if (currentPath !== '/login' && currentPath !== '/register') {
              sessionStorage.setItem('redirect_after_login', currentPath);
              window.location.href = '/login';
            }
            return;
          }

          this.scheduleReconnect();
        }
      };
    } catch (error) {
      console.error('[WebSocket] 连接失败:', error);
      this.scheduleReconnect();
    }
  }

  disconnect(): void {
    console.log('[WebSocket] 手动断开连接');
    this.isManualClose = true;

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    this.stopHeartbeat();

    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  send(message: string | WebSocketMessage): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const messageStr = typeof message === 'string' ? message : JSON.stringify(message);
      this.ws.send(messageStr);
      console.log('[WebSocket] 发送消息:', messageStr);
    } else {
      console.error('[WebSocket] 未连接，无法发送消息');
      throw new Error('WebSocket 未连接');
    }
  }

  private startHeartbeat(): void {
    this.stopHeartbeat();

    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        const heartbeat: WebSocketMessage = {
          type: 'HEARTBEAT',
          content: 'ping',
          timestamp: Date.now(),
        };

        this.ws.send(JSON.stringify(heartbeat));
      }
    }, 30000);
  }

  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  private scheduleReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[WebSocket] 达到最大重连次数，停止重连');
      return;
    }

    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
    this.reconnectAttempts++;

    console.log(`[WebSocket] ${delay}ms 后进行第 ${this.reconnectAttempts} 次重连...`);

    this.reconnectTimer = setTimeout(() => {
      console.log('[WebSocket] 开始重连...');
      this.connect();
    }, delay);
  }

  onMessage(handler: WebSocketEventHandler): void {
    this.onMessageHandler = handler;
  }

  onError(handler: WebSocketErrorHandler): void {
    this.onErrorHandler = handler;
  }

  onClose(handler: WebSocketCloseHandler): void {
    this.onCloseHandler = handler;
  }

  onOpen(handler: WebSocketEventHandler): void {
    this.onOpenHandler = handler;
  }

  get isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }
}

export const webSocketManager = new WebSocketManager();
