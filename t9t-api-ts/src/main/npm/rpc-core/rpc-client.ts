///
/// Copyright (c) 2012 - 2026 Arvato Systems GmbH
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, throwError } from 'rxjs';
import { T9T_API_CONFIG } from './provide-t9t-api';
import { mapHttpErrorToRpcError } from './rpc-error';
import { parseRpcResponse } from './large-long';

/**
 * Thin transport for t9t `/rpc` envelope calls.
 *
 * Every business request is a single `POST <rpcUrl>` with an
 * `{ '@PQON': <requestPqon>, ...body }` envelope. The host app attaches the
 * bearer token via an `HttpInterceptor`; this client never reads or writes
 * tokens itself.
 *
 * All HTTP failures are normalised to {@link RpcError} (see `rpc-error.ts`) so
 * generated API services and consumers never depend on Angular's
 * `HttpErrorResponse`.
 */
@Injectable({ providedIn: 'root' })
export class RpcClient {
  private readonly http = inject(HttpClient);
  private readonly config = inject(T9T_API_CONFIG);

  /**
   * Generic t9t `/rpc` envelope call.
   *
   * @param pqon Partially-qualified class name of the request, e.g.
   *   `t9t.auth.request.RoleSearchRequest`.
   * @param body Request fields (without `'@PQON'`, which is added here).
   * @returns The raw response body. Callers should pipe through an
   *   `unwrap*` function (see `unwrap.ts`) to check `returnCode` and extract
   *   the payload.
   */
  call<TResponse>(pqon: string, body: object = {}): Observable<TResponse> {
    return this.postEnvelope<TResponse>(this.config.rpcUrl, pqon, body);
  }

  /**
   * Envelope call against the dedicated, JWT-less login endpoint
   * (`T9tApiConfig.loginUrl`). Authentication requests (login, change
   * password, technical login) precede any bearer token, so they must not go
   * through {@link call}; the host app's interceptor excludes this URL from
   * bearer-token attachment. Falls back to `rpcUrl` when `loginUrl` is not
   * configured.
   */
  loginRpc<TResponse>(pqon: string, body: object = {}): Observable<TResponse> {
    return this.postEnvelope<TResponse>(this.config.loginUrl ?? this.config.rpcUrl, pqon, body);
  }

  /**
   * Envelope call carrying an explicit, one-off bearer token set directly on
   * the outgoing request. Use this when there is no real user session (e.g.
   * the anonymous password-reset flow's technical JWT) and the token must not
   * be written into any session store.
   */
  callWithToken<TResponse>(
    pqon: string,
    token: string,
    body: object = {},
  ): Observable<TResponse> {
    return this.postEnvelope<TResponse>(
      this.config.rpcUrl,
      pqon,
      body,
      new HttpHeaders({ Authorization: `Bearer ${token}` }),
    );
  }

  private postEnvelope<TResponse>(
    url: string,
    pqon: string,
    body: object,
    headers?: HttpHeaders,
  ): Observable<TResponse> {
    const envelope = { '@PQON': pqon, ...body };
    const longOptions = this.config.largeLong;
    const response = longOptions?.largeLongFields.length
      ? this.http
          .post(url, envelope, { headers, responseType: 'text' })
          .pipe(map((text) => parseRpcResponse<TResponse>(text, longOptions)))
      : this.http.post<TResponse>(url, envelope, { headers });

    return response.pipe(catchError((error) => throwError(() => mapHttpErrorToRpcError(error))));
  }
}
