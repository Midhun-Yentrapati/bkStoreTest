import { Injectable } from '@angular/core';
import { environment } from '../config/environment';

export enum LogLevel {
  ERROR = 0,
  WARN = 1,
  INFO = 2,
  DEBUG = 3
}

@Injectable({
  providedIn: 'root'
})
export class LoggerService {
  private logLevel: LogLevel;

  constructor() {
    this.logLevel = this.getLogLevel(environment.logLevel);
  }

  private getLogLevel(level: string): LogLevel {
    switch (level.toLowerCase()) {
      case 'error': return LogLevel.ERROR;
      case 'warn': return LogLevel.WARN;
      case 'info': return LogLevel.INFO;
      case 'debug': return LogLevel.DEBUG;
      default: return LogLevel.INFO;
    }
  }

  error(message: string, ...args: any[]): void {
    if (this.logLevel >= LogLevel.ERROR && environment.enableLogging) {
      console.error(`[ERROR] ${new Date().toISOString()}: ${this.sanitizeMessage(message)}`, ...this.sanitizeArgs(args));
    }
  }

  warn(message: string, ...args: any[]): void {
    if (this.logLevel >= LogLevel.WARN && environment.enableLogging) {
      console.warn(`[WARN] ${new Date().toISOString()}: ${this.sanitizeMessage(message)}`, ...this.sanitizeArgs(args));
    }
  }

  info(message: string, ...args: any[]): void {
    if (this.logLevel >= LogLevel.INFO && environment.enableLogging) {
      console.info(`[INFO] ${new Date().toISOString()}: ${this.sanitizeMessage(message)}`, ...this.sanitizeArgs(args));
    }
  }

  debug(message: string, ...args: any[]): void {
    if (this.logLevel >= LogLevel.DEBUG && environment.enableLogging) {
      console.debug(`[DEBUG] ${new Date().toISOString()}: ${this.sanitizeMessage(message)}`, ...this.sanitizeArgs(args));
    }
  }

  private sanitizeMessage(message: string): string {
    return message.replace(/[\r\n\t]/g, '').substring(0, 1000);
  }

  private sanitizeArgs(args: any[]): any[] {
    return args.map(arg => {
      if (typeof arg === 'string') {
        return this.sanitizeMessage(arg);
      }
      if (typeof arg === 'object' && arg !== null) {
        return this.sanitizeObject(arg);
      }
      return arg;
    });
  }

  private sanitizeObject(obj: any): any {
    const sanitized: any = {};
    for (const key in obj) {
      if (this.isSensitiveField(key)) {
        sanitized[key] = '[REDACTED]';
      } else if (typeof obj[key] === 'string') {
        sanitized[key] = this.sanitizeMessage(obj[key]);
      } else {
        sanitized[key] = obj[key];
      }
    }
    return sanitized;
  }

  private isSensitiveField(fieldName: string): boolean {
    const sensitiveFields = ['password', 'token', 'secret', 'key', 'auth', 'credential'];
    return sensitiveFields.some(field => fieldName.toLowerCase().includes(field));
  }
}