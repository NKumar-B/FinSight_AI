interface CacheEntry<T> {
  value: T;
  expiry: number;
}

class CacheService {
  private inMemoryStore = new Map<string, CacheEntry<any>>();

  async get<T>(key: string): Promise<T | null> {
    const entry = this.inMemoryStore.get(key);
    if (!entry) return null;

    if (Date.now() > entry.expiry) {
      this.inMemoryStore.delete(key);
      return null;
    }

    return entry.value as T;
  }

  async set<T>(key: string, value: T, ttlSeconds: number = 300): Promise<void> {
    const expiry = Date.now() + ttlSeconds * 1000;
    this.inMemoryStore.set(key, { value, expiry });
  }

  async del(key: string): Promise<void> {
    this.inMemoryStore.delete(key);
  }

  async flush(): Promise<void> {
    this.inMemoryStore.clear();
  }
}

export const cacheService = new CacheService();
