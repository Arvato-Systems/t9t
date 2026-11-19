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

import { toRpcError } from './rpc-error';

/**
 * Unwrap helpers for the t9t response families.
 *
 * Every t9t response is a `ServiceResponse` (or a subclass) and therefore
 * carries a `returnCode`. The server may return a `ServiceResponse` with
 * `returnCode != 0` *even when a different response type was expected*, so
 * every response type is effectively `T | ServiceResponse` and the
 * `returnCode` must be checked first.
 *
 * These helpers are pure functions (no RxJS) so they can be used with
 * `firstValueFrom`, `toSignal`, or piped through `map` in generated API
 * services.
 */

/**
 * Base shape every t9t response carries (the `ServiceResponse` fields).
 */
export interface ServiceResponseShape {
  returnCode: number;
  errorMessage?: string | null;
  errorDetails?: string | null;
}

/**
 * Unwraps a `SearchResponse` / `ReadAllResponse`: takes `dataList[].data`.
 *
 * Search entries are `DataWithTrackingS<DATA, TRACKING>` objects
 * (`{ data, tracking, tenantId }`); this helper returns only the `data`
 * payload. Use {@link unwrapSearchWithTracking} when the tracking is needed.
 */
export function unwrapSearch<T>(res: ServiceResponseShape & {
  dataList?: ({ data?: T | null } | null)[] | null;
}): T[] {
  if (res.returnCode !== 0) throw toRpcError(res);
  return (res.dataList ?? [])
    .map((e) => e?.data)
    .filter((d): d is T => d != null);
}

/**
 * Unwraps a `SearchResponse` / `ReadAllResponse` keeping the tracking payload.
 */
export function unwrapSearchWithTracking<T, TRACKING>(res: ServiceResponseShape & {
  dataList?: ({ data?: T | null; tracking?: TRACKING | null } | null)[] | null;
}): { data: T; tracking: TRACKING }[] {
  if (res.returnCode !== 0) throw toRpcError(res);
  return (res.dataList ?? [])
    .map((e) => ({ data: e?.data, tracking: e?.tracking }))
    .filter((e): e is { data: T; tracking: TRACKING } => e.data != null && e.tracking != null);
}

/**
 * Unwraps a `CrudAnyKeyResponse`: takes `data`.
 *
 * Throws if the server returned no `data` (e.g. a read of a missing key that
 * the server signals with `returnCode 0` and `data: null`).
 */
export function unwrapCrud<T>(res: ServiceResponseShape & { data?: T | null }): T {
  if (res.returnCode !== 0) throw toRpcError(res);
  if (res.data == null) {
    throw new Error('CRUD response missing data');
  }
  return res.data;
}

/**
 * Unwraps a `CrudAnyKeyResponse` that may legitimately return no `data`
 * (e.g. a read of a missing key). Returns `null` instead of throwing.
 */
export function unwrapCrudNullable<T>(res: ServiceResponseShape & { data?: T | null }): T | null {
  if (res.returnCode !== 0) throw toRpcError(res);
  return res.data ?? null;
}

/**
 * Unwraps a void response (effective return is exactly `ServiceResponse`).
 * Throws on a non-zero `returnCode`; returns `undefined` on success.
 */
export function unwrapVoid(res: ServiceResponseShape): void {
  if (res.returnCode !== 0) throw toRpcError(res);
}

/**
 * Unwraps a service response that adds fields beyond `ServiceResponse`.
 * Returns the response with `returnCode` stripped.
 */
export function unwrapService<T extends ServiceResponseShape>(res: T): Omit<T, 'returnCode'> {
  if (res.returnCode !== 0) throw toRpcError(res);
  const { returnCode: _returnCode, ...rest } = res;
  return rest;
}

/**
 * Unwraps a `LeanSearchResponse`: takes `descriptions`.
 */
export function unwrapLean<T>(res: ServiceResponseShape & {
  descriptions?: (T | null)[] | null;
}): T[] {
  if (res.returnCode !== 0) throw toRpcError(res);
  return (res.descriptions ?? []).filter((d): d is T => d != null);
}

/**
 * Unwraps a `RefResolverResponse`: takes `key`.
 */
export function unwrapResolve(res: ServiceResponseShape & { key?: number | null }): number {
  if (res.returnCode !== 0) throw toRpcError(res);
  if (res.key == null) {
    throw new Error('Resolver response missing key');
  }
  return res.key;
}

/**
 * Unwraps a `MassResolverResponse`: takes `refs`.
 */
export function unwrapResolveMany(res: ServiceResponseShape & {
  refs?: (number | null)[] | null;
}): number[] {
  if (res.returnCode !== 0) throw toRpcError(res);
  return (res.refs ?? []).filter((r): r is number => r != null);
}
