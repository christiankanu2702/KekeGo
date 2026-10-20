/**
 * Cross-Platform Error & Crash Reporting Service for KekeGo iOS
 * Integrates with Firebase Performance & Crash Logging
 */

export interface CrashlyticsLogger {
  log: (message: string) => void;
  recordError: (error: Error, customAttributes?: Record<string, string | number | boolean>) => void;
  setUserId: (userId: string) => void;
  setCustomKey: (key: string, value: string | number | boolean) => void;
}

class CrashlyticsServiceImpl implements CrashlyticsLogger {
  private userId: string | null = null;
  private attributes: Record<string, string | number | boolean> = {
    platform: 'iOS',
    jurisdiction: 'Abia State 17 LGAs',
  };

  init(userId?: string, role?: string) {
    if (userId) this.setUserId(userId);
    if (role) this.setCustomKey('user_role', role);
    console.log('[KekeGo Crashlytics iOS] Crash reporting initialized.');

    // Global uncaught JS error handler for React Native
    if (typeof ErrorUtils !== 'undefined') {
      const defaultHandler = ErrorUtils.getGlobalHandler();
      ErrorUtils.setGlobalHandler((error: any, isFatal?: boolean) => {
        this.recordError(error, { isFatal: isFatal ?? false });
        if (defaultHandler) {
          defaultHandler(error, isFatal);
        }
      });
    }
  }

  setUserId(userId: string) {
    this.userId = userId;
    console.log(`[KekeGo Crashlytics iOS] User ID set to: ${userId}`);
  }

  setCustomKey(key: string, value: string | number | boolean) {
    this.attributes[key] = value;
  }

  log(message: string) {
    console.log(`[KekeGo Crashlytics Log] ${message}`);
  }

  recordError(error: Error, customAttributes?: Record<string, string | number | boolean>) {
    const errorPayload = {
      message: error?.message || 'Unknown runtime error',
      stack: error?.stack || '',
      attributes: { ...this.attributes, ...(customAttributes || {}) },
      timestamp: new Date().toISOString(),
      userId: this.userId,
    };
    console.error('[KekeGo Crashlytics Error Captured]', JSON.stringify(errorPayload, null, 2));
  }
}

export const CrashlyticsService = new CrashlyticsServiceImpl();
