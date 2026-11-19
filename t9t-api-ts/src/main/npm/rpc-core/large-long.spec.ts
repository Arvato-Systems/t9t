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

import { parseRpcResponse, largeLongReviver } from './large-long';

const LARGE_HASH = '36601872016744006'; // > 2^53, would be rounded by JSON.parse

describe('parseRpcResponse', () => {
  it('should_preserve_a_large_long_field_as_a_string_by_default', () => {
    const text = `{"simpleHash":${LARGE_HASH},"roleId":1004}`;
    const res = parseRpcResponse<{ simpleHash: string; roleId: number }>(text, {
      largeLongFields: ['simpleHash'],
    });

    expect(res.simpleHash).toBe(LARGE_HASH);
    expect(res.roleId).toBe(1004);
  });

  it('should_preserve_a_large_long_field_as_a_bigint_in_bigint_mode', () => {
    const text = `{"simpleHash":${LARGE_HASH}}`;
    const res = parseRpcResponse<{ simpleHash: bigint }>(text, {
      largeLongFields: ['simpleHash'],
      mode: 'bigint',
    });

    expect(res.simpleHash).toBe(BigInt(LARGE_HASH));
  });

  it('should_preserve_multiple_occurrences_of_a_large_long_field', () => {
    const text = `{"a":{"simpleHash":${LARGE_HASH}},"b":{"simpleHash":${LARGE_HASH}}}`;
    const res = parseRpcResponse<{ a: { simpleHash: string }; b: { simpleHash: string } }>(text, {
      largeLongFields: ['simpleHash'],
    });

    expect(res.a.simpleHash).toBe(LARGE_HASH);
    expect(res.b.simpleHash).toBe(LARGE_HASH);
  });

  it('should_preserve_a_negative_large_long_field', () => {
    const text = `{"simpleHash":-${LARGE_HASH}}`;
    const res = parseRpcResponse<{ simpleHash: string }>(text, {
      largeLongFields: ['simpleHash'],
    });

    expect(res.simpleHash).toBe(`-${LARGE_HASH}`);
  });

  it('should_not_quote_a_field_that_is_already_a_string', () => {
    const text = `{"simpleHash":"${LARGE_HASH}","other":123}`;
    const res = parseRpcResponse<{ simpleHash: string; other: number }>(text, {
      largeLongFields: ['simpleHash'],
    });

    expect(res.simpleHash).toBe(LARGE_HASH);
    expect(res.other).toBe(123);
  });

  it('should_not_touch_fields_that_are_not_configured', () => {
    const text = `{"simpleHash":${LARGE_HASH},"otherLong":${LARGE_HASH}}`;
    const res = parseRpcResponse<Record<string, unknown>>(text, {
      largeLongFields: ['simpleHash'],
    });

    expect(res['simpleHash']).toBe(LARGE_HASH);
    // Not configured, so it is parsed as a plain number (rounded).
    expect(typeof res['otherLong']).toBe('number');
  });

  it('should_behave_like_json_parse_when_no_fields_are_configured', () => {
    const text = `{"roleId":1004,"name":"x"}`;
    const res = parseRpcResponse<{ roleId: number; name: string }>(text, { largeLongFields: [] });
    expect(res).toEqual({ roleId: 1004, name: 'x' });
  });

  it('should_handle_a_large_long_field_inside_a_list', () => {
    const text = `{"items":[{"simpleHash":${LARGE_HASH}},{"simpleHash":${LARGE_HASH}}]}`;
    const res = parseRpcResponse<{ items: { simpleHash: string }[] }>(text, {
      largeLongFields: ['simpleHash'],
    });

    expect(res.items[0].simpleHash).toBe(LARGE_HASH);
    expect(res.items[1].simpleHash).toBe(LARGE_HASH);
  });
});

describe('largeLongReviver', () => {
  it('should_convert_configured_string_values_to_bigint_in_bigint_mode', () => {
    const reviver = largeLongReviver(['simpleHash'], 'bigint');
    const res = JSON.parse(`{"simpleHash":"${LARGE_HASH}"}`, reviver) as { simpleHash: bigint };
    expect(res.simpleHash).toBe(BigInt(LARGE_HASH));
  });

  it('should_leave_other_fields_untouched', () => {
    const reviver = largeLongReviver(['simpleHash'], 'string');
    const res = JSON.parse(`{"simpleHash":"${LARGE_HASH}","n":5}`, reviver) as {
      simpleHash: string;
      n: number;
    };
    expect(res.simpleHash).toBe(LARGE_HASH);
    expect(res.n).toBe(5);
  });
});
