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

/**
 * Large-long (JSON `long` beyond 2^53) parsing for t9t RPC responses.
 *
 * The t9t server writes `long` fields as unquoted JSON numbers. Values above
 * 2^53 (e.g. `simpleHash` on address/customer DTOs, verified live as
 * `36601872016744006`) are silently rounded by `JSON.parse`, losing precision.
 *
 * A plain `JSON.parse` *reviver* cannot recover that precision — the number is
 * already rounded before the reviver runs. So {@link parseRpcResponse} first
 * rewrites the raw text to quote the configured fields' numeric values, then
 * parses with a reviver that converts those quoted values to the requested
 * runtime representation (`string` by default, or `bigint`).
 *
 * This is a self-contained, dependency-free stand-in for `json-bigint`, scoped
 * to the configured field names.
 */
export interface LargeLongOptions {
  /**
   * Field names whose numeric values must be preserved beyond 2^53. Known
   * today: `simpleHash` (address/customer DTOs).
   */
  largeLongFields: string[];
  /**
   * Runtime representation for preserved values. `'string'` (default) is safe
   * for re-serialisation and display; `'bigint'` allows arithmetic.
   */
  mode?: 'string' | 'bigint';
}

/**
 * Parses raw RPC response JSON, preserving precision for the configured
 * large-long fields. With no configured fields this is equivalent to
 * `JSON.parse`.
 */
export function parseRpcResponse<T>(text: string, options: LargeLongOptions): T {
  if (options.largeLongFields.length === 0) {
    return JSON.parse(text) as T;
  }
  const mode = options.mode ?? 'string';
  const quoted = quoteLargeLongs(text, options.largeLongFields);
  return JSON.parse(quoted, largeLongReviver(options.largeLongFields, mode)) as T;
}

/**
 * Builds a `JSON.parse` reviver that converts the configured large-long fields
 * (already quoted by {@link quoteLargeLongs}) into the requested mode.
 */
export function largeLongReviver(fields: string[], mode: 'string' | 'bigint') {
  const set = new Set(fields);
  return (_key: string, value: unknown): unknown => {
    if (set.has(_key) && typeof value === 'string') {
      return mode === 'bigint' ? BigInt(value) : value;
    }
    return value;
  };
}

/**
 * Rewrites `"field":<number>` occurrences for each configured field into
 * `"field":"<number>"` so `JSON.parse` keeps the full digit sequence.
 */
function quoteLargeLongs(text: string, fields: string[]): string {
  let result = text;
  for (const field of fields) {
    const re = new RegExp(`("(${escapeRegExp(field)}")\\s*:\\s*)(-?\\d+)`, 'g');
    result = result.replace(re, (_full, prefix: string, _field: string, num: string) => {
      return `${prefix}"${num}"`;
    });
  }
  return result;
}

function escapeRegExp(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
