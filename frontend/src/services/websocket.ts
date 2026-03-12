import { Client } from '@stomp/stompjs';
import type { StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { WebSocketMessage } from '@/types';

/**
 * WebSocket 服务
 * 用于与后端进行实时通信
 */

class WebSocketService {
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;

  /**
   * 连接 WebSocket
   */
  connect(url: string = 'ws://localhost:8080/api/ws/chat'): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.client && this.client.connected) {
        resolve();
        return;
      }

      this.client = new Client({
        webSocketFactory: () => new SockJS(url),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,

        onConnect: () => {
          console.log('WebSocket connected');
          this.reconnectAttempts = 0;
          resolve();
        },

        onDisconnect: () => {
          console.log('WebSocket disconnected');
          this.handleReconnect(url);
        },

        onStompError: (frame) => {
          console.error('WebSocket error:', frame);
          reject(new Error(frame.headers?.message || 'WebSocket connection failed'));
        },
      });

      this.client.activate();
    });
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.subscriptions.clear();
    }
  }

  /**
   * 订阅消息
   */
  subscribe(destination: string, callback: (message: WebSocketMessage) => void): void {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected');
      return;
    }

    const subscription = this.client.subscribe(destination, (message) => {
      try {
        const body = JSON.parse(message.body);
        callback(body);
      } catch (error) {
        console.error('Failed to parse message:', error);
      }
    });

    this.subscriptions.set(destination, subscription);
  }

  /**
   * 取消订阅
   */
  unsubscribe(destination: string): void {
    const subscription = this.subscriptions.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(destination);
    }
  }

  /**
   * 发送消息
   */
  publish(destination: string, body: any): void {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected');
      return;
    }

    this.client.publish({
      destination,
      body: JSON.stringify(body),
    });
  }

  /**
   * 处理重连
   */
  private handleReconnect(url: string): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
      setTimeout(() => {
        this.connect(url).catch((error) => {
          console.error('Reconnection failed:', error);
        });
      }, 5000);
    } else {
      console.error('Max reconnection attempts reached');
    }
  }

  /**
   * 检查连接状态
   */
  isConnected(): boolean {
    return this.client?.connected ?? false;
  }
}

// 导出单例
export const websocketService = new WebSocketService();
export default websocketService;
