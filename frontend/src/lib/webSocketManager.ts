import { API_CONFIG } from '@/utils/constants';

/**
 * WebSocket 消息类型（与后端保持一致，使用大写）
 */
export enum WebSocketMessageType {
  CHAT = 'CHAT',
  HEARTBEAT = 'HEARTBEAT',
  ERROR = 'ERROR',
}

/**
 * WebSocket 消息格式
 */
export interface WebSocketMessage {
  type: WebSocketMessageType;
  content: string;
  timestamp: number;
}

/**
 * WebSocket 事件类型
 */
type WebSocketEventHandler = (data: any) => void;
type WebSocketErrorHandler = (error: Event) => void;
type WebSocketCloseHandler = (event: CloseEvent) => void;

/**
 * WebSocket 管理器
 * 负责建立、维护和关闭 WebSocket 连接
 */
class WebSocketManager {
  private ws: WebSocket | null = null;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private heartbeatTimer: ReturnType<typeof setTimeout> | null = null;
  private isManualClose: boolean = false;
  private reconnectAttempts: number = 0;
  private maxReconnectAttempts: number = 5;

  // 事件处理器
  private onMessageHandler: WebSocketEventHandler | null = null;
  private onErrorHandler: WebSocketErrorHandler | null = null;
  private onCloseHandler: WebSocketCloseHandler | null = null;
  private onOpenHandler: WebSocketEventHandler | null = null;

  /**
   * 连接 WebSocket
   */
  connect(): void {
    // 如果已经连接，则不重复连接
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('[WebSocket] 已经连接');
      return;
    }

    try {
      console.log('[WebSocket] 正在连接...', API_CONFIG.wsURL);
      this.ws = new WebSocket(API_CONFIG.wsURL);

      // 连接成功
      this.ws.onopen = (event) => {
        console.log('[WebSocket] 连接成功');
        this.isManualClose = false;
        this.reconnectAttempts = 0;

        // 开始心跳
        this.startHeartbeat();

        // 触发连接成功回调
        if (this.onOpenHandler) {
          this.onOpenHandler(event);
        }
      };

      // 接收消息
      this.ws.onmessage = (event) => {
        console.log('[WebSocket] 收到消息:', event.data);

        try {
          const message = JSON.parse(event.data);

          // 处理心跳响应
          if (message.type === WebSocketMessageType.HEARTBEAT) {
            return;
          }

          // 触发消息回调
          if (this.onMessageHandler) {
            this.onMessageHandler(message);
          }
        } catch (error) {
          console.error('[WebSocket] 消息解析失败:', error);
        }
      };

      // 连接错误
      this.ws.onerror = (event) => {
        console.error('[WebSocket] 连接错误:', event);

        if (this.onErrorHandler) {
          this.onErrorHandler(event);
        }
      };

      // 连接关闭
      this.ws.onclose = (event) => {
        console.log('[WebSocket] 连接关闭:', event.code, event.reason);

        // 停止心跳
        this.stopHeartbeat();

        // 触发关闭回调
        if (this.onCloseHandler) {
          this.onCloseHandler(event);
        }

        // 如果不是手动关闭，则尝试重连
        if (!this.isManualClose) {
          this.scheduleReconnect();
        }
      };
    } catch (error) {
      console.error('[WebSocket] 连接失败:', error);
      this.scheduleReconnect();
    }
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    console.log('[WebSocket] 手动断开连接');
    this.isManualClose = true;

    // 清除重连定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    // 停止心跳
    this.stopHeartbeat();

    // 关闭连接
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  /**
   * 发送消息
   */
  send(message: string | object): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const messageStr = typeof message === 'string' ? message : JSON.stringify(message);
      this.ws.send(messageStr);
      console.log('[WebSocket] 发送消息:', messageStr);
    } else {
      console.error('[WebSocket] 未连接，无法发送消息');
      throw new Error('WebSocket 未连接');
    }
  }

  /**
   * 开始心跳
   */
  private startHeartbeat(): void {
    this.stopHeartbeat();

    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        const heartbeat: WebSocketMessage = {
          type: WebSocketMessageType.HEARTBEAT,
          content: 'ping',
          timestamp: Date.now(),
        };

        this.ws.send(JSON.stringify(heartbeat));
      }
    }, 30000); // 每 30 秒发送一次心跳
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 安排重连
   */
  private scheduleReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[WebSocket] 达到最大重连次数，停止重连');
      return;
    }

    // 指数退避策略
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
    this.reconnectAttempts++;

    console.log(`[WebSocket] ${delay}ms 后进行第 ${this.reconnectAttempts} 次重连...`);

    this.reconnectTimer = setTimeout(() => {
      console.log('[WebSocket] 开始重连...');
      this.connect();
    }, delay);
  }

  /**
   * 注册消息回调
   */
  onMessage(handler: WebSocketEventHandler): void {
    this.onMessageHandler = handler;
  }

  /**
   * 注册错误回调
   */
  onError(handler: WebSocketErrorHandler): void {
    this.onErrorHandler = handler;
  }

  /**
   * 注册关闭回调
   */
  onClose(handler: WebSocketCloseHandler): void {
    this.onCloseHandler = handler;
  }

  /**
   * 注册连接成功回调
   */
  onOpen(handler: WebSocketEventHandler): void {
    this.onOpenHandler = handler;
  }

  /**
   * 获取连接状态
   */
  get isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }
}

// 导出单例
export const webSocketManager = new WebSocketManager();
