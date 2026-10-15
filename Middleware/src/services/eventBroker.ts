export interface BrokerEvent<T = any> {
  eventId: string;
  topic: 'financial.transactions' | 'fraud.alerts' | 'reconciliation.events' | 'ai.agent.telemetry' | 'audit.events';
  timestamp: string;
  correlationId?: string;
  payload: T;
}

class EventBrokerService {
  private recentEvents: BrokerEvent[] = [];
  private maxHistory = 100;

  async publish<T = any>(
    topic: 'financial.transactions' | 'fraud.alerts' | 'reconciliation.events' | 'ai.agent.telemetry' | 'audit.events',
    payload: T,
    correlationId?: string
  ): Promise<BrokerEvent<T>> {
    const event: BrokerEvent<T> = {
      eventId: `evt_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
      topic,
      timestamp: new Date().toISOString(),
      correlationId,
      payload,
    };

    this.recentEvents.unshift(event);
    if (this.recentEvents.length > this.maxHistory) {
      this.recentEvents.pop();
    }

    console.log(`[EventBroker] [Topic: ${topic}] [ID: ${event.eventId}] Event published successfully`);
    return event;
  }

  getRecentEvents(topic?: string): BrokerEvent[] {
    if (!topic) return this.recentEvents;
    return this.recentEvents.filter((e) => e.topic === topic);
  }
}

export const eventBroker = new EventBrokerService();
