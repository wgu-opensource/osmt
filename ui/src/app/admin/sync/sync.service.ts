import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppConfig } from '../../app.config';
import { Observable } from 'rxjs';
import { AuthService } from '../../auth/auth-service';

export interface SyncIntegrationDto {
  syncKey: string;
  recordType: string;
  syncWatermark: string | null;
  statusJson?: string | null;
  pendingCount?: number | null;
}

export interface SyncStateResponse {
  integrations: SyncIntegrationDto[];
  allowUnpublishAll?: boolean;
}

@Injectable({ providedIn: 'root' })
export class SyncService {
  private get base(): string {
    return `${AppConfig.settings.baseApiUrl}/api/sync`;
  }

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private headers(): HttpHeaders {
    const token = this.authService.currentAuthToken();
    let h = new HttpHeaders();
    if (token) {
      h = h.set('Authorization', `Bearer ${token}`);
    }
    return h;
  }

  getState(): Observable<SyncStateResponse> {
    const cacheBuster = `t=${Date.now()}`;
    return this.http.get<SyncStateResponse>(
      `${this.base}/state?${cacheBuster}`,
      {
        headers: this.headers(),
      }
    );
  }

  syncAll(): Observable<string> {
    return this.http.post(`${this.base}/all`, null, {
      headers: this.headers(),
      responseType: 'text',
    });
  }

  resyncAll(): Observable<string> {
    return this.http.post(`${this.base}/resync`, null, {
      headers: this.headers(),
      responseType: 'text',
    });
  }

  unpublishAll(): Observable<string> {
    return this.http.post(`${this.base}/unpublish-all`, null, {
      headers: this.headers(),
      responseType: 'text',
    });
  }

  syncSkill(uuid: string): Observable<void> {
    return this.http.post<void>(`${this.base}/skill/${uuid}`, null, {
      headers: this.headers(),
    });
  }

  syncCollection(uuid: string): Observable<void> {
    return this.http.post<void>(`${this.base}/collection/${uuid}`, null, {
      headers: this.headers(),
    });
  }
}
