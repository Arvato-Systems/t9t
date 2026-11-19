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

import { InjectionToken, makeEnvironmentProviders } from '@angular/core';
import type { EnvironmentProviders } from '@angular/core';
import type { LargeLongOptions } from './large-long';

/**
 * Configuration for the t9t typed API.
 */
export interface T9tApiConfig {
  /**
   * Absolute URL of the t9t `/rpc` endpoint, e.g.
   * `https://host/rpc`. Every business request is a single `POST` to this URL
   * with an `{ '@PQON': <requestPqon>, ...body }` envelope.
   */
  rpcUrl: string;
  /**
   * Optional absolute URL of the dedicated, JWT-less `/login` endpoint, e.g.
   * `https://host/login`. Used by {@link RpcClient.loginRpc} for the
   * authentication requests that precede any bearer token. When omitted,
   * {@link RpcClient.loginRpc} falls back to {@link rpcUrl}.
   */
  loginUrl?: string;
  /** Optional field-aware parsing for JSON longs that exceed JavaScript's safe integer range. */
  largeLong?: LargeLongOptions;
}

/**
 * Injection token backing {@link provideT9tApi}.
 */
export const T9T_API_CONFIG = new InjectionToken<T9tApiConfig>('T9T_API_CONFIG');

/**
 * Provides the t9t API configuration to the injector.
 *
 * ```ts
 * bootstrapApplication(App, {
 *   providers: [
 *     provideHttpClient(),
 *     provideT9tApi({ rpcUrl: 'https://host/rpc' }),
 *   ],
 * });
 * ```
 *
 * Returns an {@link EnvironmentProviders} so it can be composed with other
 * environment providers (e.g. the host app's JWT interceptor).
 */
export function provideT9tApi(config: T9tApiConfig): EnvironmentProviders {
  return makeEnvironmentProviders([{ provide: T9T_API_CONFIG, useValue: config }]);
}
