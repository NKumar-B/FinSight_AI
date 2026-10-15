"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.eventBroker = void 0;
class EventBrokerService {
    recentEvents = [];
    maxHistory = 100;
    async publish(topic, payload, correlationId) {
        const event = {
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
    getRecentEvents(topic) {
        if (!topic)
            return this.recentEvents;
        return this.recentEvents.filter((e) => e.topic === topic);
    }
}
exports.eventBroker = new EventBrokerService();
