"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.cacheService = void 0;
class CacheService {
    inMemoryStore = new Map();
    async get(key) {
        const entry = this.inMemoryStore.get(key);
        if (!entry)
            return null;
        if (Date.now() > entry.expiry) {
            this.inMemoryStore.delete(key);
            return null;
        }
        return entry.value;
    }
    async set(key, value, ttlSeconds = 300) {
        const expiry = Date.now() + ttlSeconds * 1000;
        this.inMemoryStore.set(key, { value, expiry });
    }
    async del(key) {
        this.inMemoryStore.delete(key);
    }
    async flush() {
        this.inMemoryStore.clear();
    }
}
exports.cacheService = new CacheService();
